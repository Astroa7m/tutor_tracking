package com.example.tutortracking.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.tutortracking.CoroutinesTestRule
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.repository.TutorRepositoryFake
import com.example.tutortracking.log
import com.example.tutortracking.logMessage
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SessionManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import kotlin.time.ExperimentalTime


@ExperimentalTime
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StudentViewModelTest{

    private lateinit var studentViewModelOnline: StudentViewModel
    private lateinit var studentViewModelOffline: StudentViewModel


    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutineRule = CoroutinesTestRule()

    @Mock
    private lateinit var mockContext: Context

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()



    @Before
    fun setup(){
        `when`(mockContext.applicationContext).thenReturn(mockContext)
        val sessionManager = SessionManager(mockContext)
        studentViewModelOnline = StudentViewModel(TutorRepositoryFake(tutorToken = "token"), sessionManager)
        studentViewModelOffline = StudentViewModel(TutorRepositoryFake(false,"token"), sessionManager)
    }

    @Test
    fun `add student with empty fields`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOnline.addStudentState.test{
            studentViewModelOnline.addStudent("","","", null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
        }
    }

    @Test
    fun `add student with valid fields`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOnline.addStudentState.test{
            studentViewModelOnline.addStudent("coolName","1","Coolness", null)
            assertThat(expectMostRecentItem().data?.success).isTrue()
        }
    }

    @Test
    fun `add student with valid fields offline`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOffline.addStudentState.test{
            studentViewModelOffline.addStudent("coolName","1","Coolness", null)
            assertThat(expectMostRecentItem().message).isEqualTo("inserted locally")
        }
    }

    @Test
    fun `update student with empty fields`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOnline.updateStudentState.test{
            studentViewModelOnline.updateStudent("","","", null, "id")
            val lastEmission = expectMostRecentItem()
            assertThat(lastEmission.log().logMessage()).isInstanceOf(Result.Error::class.java)
            assertThat(lastEmission.message).isEqualTo("Fields cannot be left empty")
        }
    }

    @Test
    fun `update student with valid fields`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOnline.updateStudentState.test{
            studentViewModelOnline.updateStudent("asd","1","asd", null, "id")
            val lastEmission = expectMostRecentItem()
            assertThat(lastEmission.log()).isInstanceOf(Result.Success::class.java)
            assertThat(lastEmission.data?.success).isTrue()
        }
    }

    @Test
    fun `update student with valid fields offline`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOffline.updateStudentState.test{
            studentViewModelOffline.addStudent("asd","1","asd", null)
            studentViewModelOffline.updateStudent("asd","1","damnn", null, "")
            val lastEmission = expectMostRecentItem()
            assertThat(lastEmission.log()).isInstanceOf(Result.Success::class.java)
            assertThat(lastEmission.data?.message).isEqualTo("updated locally")
        }
    }

    @Test
    fun `delete student`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOnline.deleteStudentState.test {
            studentViewModelOnline.addStudent("asd","1","asd", null)
            assertThat(studentViewModelOnline.repository.getAllStudentsAsList()).isNotEmpty()
            studentViewModelOnline.deleteStudent(LocalStudent("Asd",1,"asd", null, _id = ""))
            val lastEmission = expectMostRecentItem()
            assertThat(lastEmission.log()).isInstanceOf(Result.Success::class.java)
            assertThat(studentViewModelOnline.repository.getAllStudentsAsList()).isEmpty()

        }
    }

    @Test
    fun `sync data removes locally added student table`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOffline.addStudent("asd","1","asd", null)
        assertThat(studentViewModelOffline.repository.getAllLocallyAdded()).isNotEmpty()
        println("before ${studentViewModelOffline.repository.getAllLocallyAdded()}")
        studentViewModelOffline.syncData{}
        println("after ${studentViewModelOffline.repository.getAllLocallyAdded()}")
        assertThat(studentViewModelOffline.repository.getAllLocallyAdded()).isEmpty()
    }

    @Test
    fun `sync data removes locally updated student table`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOffline.addStudent("asd","1","asd", null)
        studentViewModelOffline.updateStudent("Asd","1","asd", null, "")
        assertThat(studentViewModelOffline.repository.getAllLocallyUpdated()).isNotEmpty()
        studentViewModelOffline.syncData{}
        assertThat(studentViewModelOffline.repository.getAllLocallyUpdated()).isEmpty()
    }

    @Test
    fun `sync data removes locally deleted student table`() = coroutineRule.testDispatcher.runBlockingTest {
        studentViewModelOffline.addStudent("asd","1","asd", null)
        studentViewModelOffline.deleteStudent(LocalStudent("Asd",1,"asd", null, _id = ""))
        assertThat(studentViewModelOffline.repository.getAllLocallyDelete()).isNotEmpty()
        studentViewModelOffline.syncData{}
        assertThat(studentViewModelOffline.repository.getAllLocallyDelete()).isEmpty()
    }

    @Test
    fun `sync data removes locally deleted item from server`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOnline.addStudent("i wont be deleted","10101","some",null)
        studentViewModelOnline.addStudent("i will be deleted","99","dangIt",null)
        val remoteStudents = studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList
        println("list of remote students before $remoteStudents")
        //user is offline and deletes a student
        val deletionTarget = remoteStudents!![1]
        studentViewModelOnline.repository.addLocallyDeletedStudent(LocallyDeletedStudent(deletionTarget.studentName, deletionTarget.studentYear, deletionTarget.studentSubject, deletionTarget.studentTutorId,deletionTarget.studentPic,deletionTarget._id?:""))
        studentViewModelOnline.syncData{}
        println("list of remote students after ${studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList}")
        // the deleted student shouldn't be in the remoteStudentsList
        assertThat(studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList).doesNotContain(deletionTarget)
    }

    @Test
    fun `sync data adds locally added item to server`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOnline.repository.addLocallyAddedStudent(LocallyAddedStudent("test", 1, _id ="da"))
        val remoteStudentsBefore = studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList
        println("list of remote students before $remoteStudentsBefore")
        assertThat(remoteStudentsBefore).isEmpty()
        //user gets online and refresh
        studentViewModelOnline.syncData{}
        val remoteStudentsAfter = studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList
        println("list of remote students after $remoteStudentsAfter")
        assertThat(remoteStudentsAfter).contains(remoteStudentsAfter?.get(0))
    }

    @Test
    fun `sync data updates locally updated item to server`() = coroutineRule.testDispatcher.runBlockingTest{
        studentViewModelOnline.addStudent("test", "1", "1", null)
        val remoteStudentsBefore = studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList
        println("list of remote students before $remoteStudentsBefore")
        assertThat(remoteStudentsBefore).isNotEmpty()
        studentViewModelOnline.repository.addLocallyUpdatedStudent(LocallyUpdatedStudent("someotherTests", 123, "nothing", _id = remoteStudentsBefore!![0]._id ?:""))
        studentViewModelOnline.syncData{}
        val remoteStudentsAfter = studentViewModelOnline.repository.getAllStudentsFromServer().data?.studentsList
        println("list of remote students after $remoteStudentsAfter")
        assertThat(remoteStudentsAfter).contains(remoteStudentsAfter?.get(0))
    }

    @Test
    fun `getting tutor modules`() = coroutineRule.testDispatcher.runBlockingTest {
        val listOfModules = listOf("Math", "Coolness")
        studentViewModelOnline.repository.register(Register("some@some.com", "dadada", "someTutor", listOfModules, null, ""))
        val receivedList = studentViewModelOnline.getStudentsTutorModules()
        assertThat(receivedList).isEqualTo(listOfModules)
    }

}