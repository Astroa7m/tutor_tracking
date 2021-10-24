package com.example.tutortracking.data.localdata

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.test.filters.SmallTest
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.getOrAwaitValue
import com.example.tutortracking.util.SortOrder
import com.example.tutortracking.util.getLocallyAddedFromStudent
import com.example.tutortracking.util.getLocallyDeletedFromStudent
import com.example.tutortracking.util.getLocallyUpdatedFromStudent
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@SmallTest
@ExperimentalCoroutinesApi
class StudentDaoTest {

    @get:Rule
    val hiltTestRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    @Named("db_test")
    @Inject
    lateinit var database: StudentDatabase

    private lateinit var dao: StudentDao

    private var isOnline = false

    @Before
    fun setup(){
        hiltTestRule.inject()
        dao = database.getStudentDao()
    }

    @Test
    fun test_addNewStudent() = runBlockingTest {

        val student = LocalStudent(
            "name",
            1,
            "math",
            "1",
            null,
            false,
            "id"
        )
        dao.upsertStudent(student)

        val allStudents = dao.getStudents("", SortOrder.BY_NAME).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_addNewStudent: $allStudents")

        assertThat(allStudents).contains(student)

    }

    @Test
    fun test_deleteStudent() = runBlockingTest {

        val student = LocalStudent(
            "name",
            1,
            "math",
            "1",
            null,
            false,
            "id"
        )
        dao.upsertStudent(student)

        dao.deleteStudent(student)
        val allStudents = dao.getStudents("", SortOrder.BY_NAME).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_deleteStudent: $allStudents")

        assertThat(allStudents).doesNotContain(student)

    }

    @Test
    fun test_deleteStudentById() = runBlockingTest {

        val student = LocalStudent(
            "name",
            1,
            "math",
            "1",
            null,
            false,
            "id"
        )
        dao.upsertStudent(student)

        dao.deleteStudentById("id")
        val allStudents = dao.getStudents("", SortOrder.BY_NAME).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_deleteStudentById: $allStudents")

        assertThat(allStudents).doesNotContain(student)

    }

    @Test
    fun test_deleteAllLocalStudents() = runBlockingTest {

        val student1 = LocalStudent(
            "name1",
            1,
            "math1",
            "1",
            null,
            false,
            "id1"
        )
        val student2 = LocalStudent(
            "name2",
            2,
            "math2",
            "2",
            null,
            false,
            "id2"
        )
        val student3 = LocalStudent(
            "name3",
            3,
            "math3",
            "3",
            null,
            false,
            "id3"
        )
        dao.upsertStudent(student1)
        dao.upsertStudent(student2)
        dao.upsertStudent(student3)

        dao.deleteAllLocalStudents()
        val allStudents = dao.getStudents("", SortOrder.BY_NAME).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_deleteAllLocalStudents: $allStudents")

        assertThat(allStudents).isEmpty()

    }

    @Test
    fun test_getAllStudentsAsList() = runBlockingTest {

        val student1 = LocalStudent(
            "name1",
            1,
            "math1",
            "1",
            null,
            false,
            "id1"
        )
        val student2 = LocalStudent(
            "name2",
            2,
            "math2",
            "2",
            null,
            false,
            "id2"
        )
        val student3 = LocalStudent(
            "name3",
            3,
            "math3",
            "3",
            null,
            false,
            "id3"
        )
        dao.upsertStudent(student1)
        dao.upsertStudent(student2)
        dao.upsertStudent(student3)

        val allStudents = dao.getAllStudentsForAsList()

        Log.d("MYTAG", "test_getAllStudentsAsLists: $allStudents")

        assertThat(allStudents).isNotEmpty()

    }

    @Test
    fun test_getAllStudentsSortedByYear() = runBlockingTest {
        val studentsOrderedByYear = mutableListOf<LocalStudent>()
        repeat(3){
            studentsOrderedByYear.add(
                LocalStudent(
                    "name$it",
                    it,
                    "math$it",
                    "$it",
                    null,
                    false,
                    "id$it"
                )
            )

            dao.upsertStudent(
                LocalStudent(
                "name$it",
                it,
                "math$it",
                "$it",
                null,
                false,
                "id$it"
                )
            )
        }

        val allStudents = dao.getStudents("", SortOrder.BY_YEAR).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_getAllStudentsSortedByYear: $allStudents")

        assertThat(allStudents).containsExactly(studentsOrderedByYear[0], studentsOrderedByYear[1],
            studentsOrderedByYear[2]).inOrder()


    }

    @Test
    fun test_getAllStudentsSortedBySubject() = runBlockingTest {
        val s1 =  LocalStudent(
            "mickey",
            1,
            "acrobat",
            "1",
            null,
            false,
            "1"
        )

        val s2 =  LocalStudent(
            "mickey",
            1,
            "math",
            "1",
            null,
            false,
            "2"
        )

        val s3 =  LocalStudent(
            "mickey",
            1,
            "physics",
            "1",
            null,
            false,
            "3"
        )
        dao.upsertStudent(s2)
        dao.upsertStudent(s1)
        dao.upsertStudent(s3)

        val allStudents = dao.getStudents("", SortOrder.BY_SUBJECT).asLiveData().getOrAwaitValue()

        Log.d("MYTAG", "test_getAllStudentsSortedBySubject: $allStudents")

        assertThat(allStudents).containsExactly(s1, s2,
            s3).inOrder()


    }

    @Test
    fun test_addTutor() = runBlockingTest {

        val tutor = Tutor(
            "tutor@tutor.com",
            "f2uh2F&@FN&@F2*&Fg28f",
            "mynameis tutor",
            listOf("asd"),
            null,
            "1"
        )
        dao.upsertTutor(tutor)

        val currentTutor = dao.getTutor().asLiveData().getOrAwaitValue()[0]

        Log.d("MYTAG", "test_addTutor: $currentTutor")

        assertThat(currentTutor).isEqualTo(tutor)

    }

    @Test
    fun test_getTutorModules() = runBlockingTest {

        val tutor = Tutor(
            "tutor@tutor.com",
            "f2uh2F&@FN&@F2*&Fg28f",
            "mynameis tutor",
            listOf("asd", "math", "some other stuff"),
            null,
            "1"
        )
        dao.upsertTutor(tutor)

        val currentTutorModules = dao.getTutorModules().split(",")

        Log.d("MYTAG", "test_getTutorModules: $currentTutorModules")

        assertThat(currentTutorModules).isEqualTo(tutor.modules)

    }

    @Test
    fun test_deleteTutor() = runBlockingTest {

        val tutor = Tutor(
            "tutor@tutor.com",
            "f2uh2F&@FN&@F2*&Fg28f",
            "mynameis tutor",
            listOf("asd", "math", "some other stuff"),
            null,
            "1"
        )
        dao.upsertTutor(tutor)

        dao.deleteTutor()
        val currentTutor = dao.getTutor().asLiveData().getOrAwaitValue()

        assertThat(currentTutor).isEmpty()

    }

    @Test
    fun test_getAllLocallyAddedStudents_deletedWhenOnline() = runBlockingTest {
        val offlineAddedStudent = LocallyAddedStudent(
            "name",
            1,
            "math",
            "1",
            null,
            "id"
        )

        dao.insertLocallyAddedStudent(offlineAddedStudent)

        val locallyAddedStudent = dao.getAllLocallyAdded()

        Log.d("MYTAG", "getAllLocallyAddedStudents_deletedWhenOnline before: $locallyAddedStudent")

        if(!isOnline) {
            assertThat(locallyAddedStudent).contains(offlineAddedStudent)
            isOnline = true
        }

        if(isOnline){
            dao.deleteLocallyAddedStudent(offlineAddedStudent)
        }

        val locallyAddedStudentAfterDeletion = dao.getAllLocallyAdded()

        Log.d("MYTAG", "getAllLocallyAddedStudents_deletedWhenOnline after: $locallyAddedStudentAfterDeletion")


        assertThat(locallyAddedStudentAfterDeletion).isEmpty()

    }

    @Test
    fun test_getAllLocallyUpdatedStudents_deletedWhenOnline() = runBlockingTest {
        val locallyUpdatedStudent = LocallyUpdatedStudent(
            "name",
            1,
            "math",
            "1",
            null,
            "id"
        )

        dao.insertLocallyUpdatedStudent(locallyUpdatedStudent)

        val allLocallyUpdatedStudents = dao.getAllLocallyUpdated()

        Log.d("MYTAG", "getAllLocallyUpdatedStudents_deletedWhenOnline before: $allLocallyUpdatedStudents")


        if(!isOnline) {
            assertThat(allLocallyUpdatedStudents).contains(locallyUpdatedStudent)
            isOnline = true
        }

        if(isOnline)
            dao.deleteLocallyUpdatedStudent(locallyUpdatedStudent)

        val allLocallyUpdatedStudentsAfterDeletion = dao.getAllLocallyAdded()

        Log.d("MYTAG", "getAllLocallyUpdatedStudents_deletedWhenOnline after: $allLocallyUpdatedStudentsAfterDeletion")


        assertThat(allLocallyUpdatedStudentsAfterDeletion).isEmpty()

    }

    @Test
    fun test_getAllLocallyDeletedStudents_deletedWhenOnline() = runBlockingTest {
        val offlineDeletedStudent = LocallyDeletedStudent(
            "name",
            1,
            "math",
            "1",
            null,
            "id"
        )

        dao.insertLocallyDeletedStudent(offlineDeletedStudent)

        val locallyDeletedStudent = dao.getAllLocallyDeleted()

        Log.d("MYTAG", "getAllLocallyDeletedStudents_deletedWhenOnline before: $locallyDeletedStudent")

        if(!isOnline) {
            assertThat(locallyDeletedStudent).contains(offlineDeletedStudent)
            isOnline = true
        }

        if(isOnline)
            dao.deleteLocallyDeletedStudent(offlineDeletedStudent)

        val locallyDeletedStudentAfterDeletion = dao.getAllLocallyAdded()

        Log.d("MYTAG", "getAllLocallyDeletedStudents_deletedWhenOnline after: $locallyDeletedStudentAfterDeletion")


        assertThat(locallyDeletedStudentAfterDeletion).isEmpty()

    }

    @Test
    fun test_deleteAll_from_LocallyAdded_LocallyUpdated_LocallyDeleted_tables_whenOnline() = runBlockingTest{
        repeat(3){
            val student = LocalStudent(
                "name$it",
                it,
                "math$it",
                "$it",
                null,
                false,
                "id$it"
            )
            val locallyAddedStudent = getLocallyAddedFromStudent(student)
            val locallyUpdatedStudent = getLocallyUpdatedFromStudent(student)
            val locallyDeletedStudent = getLocallyDeletedFromStudent(student)

            dao.insertLocallyAddedStudent(locallyAddedStudent)
            dao.insertLocallyUpdatedStudent(locallyUpdatedStudent)
            dao.insertLocallyDeletedStudent(locallyDeletedStudent)
        }

        assertThat(dao.getAllLocallyAdded()).isNotEmpty()
        assertThat(dao.getAllLocallyUpdated()).isNotEmpty()
        assertThat(dao.getAllLocallyDeleted()).isNotEmpty()

        dao.deleteRecordsFromLocallyAddedStudent()
        dao.deleteRecordsFromLocallyUpdatedStudent()
        dao.deleteRecordsFromLocallyDeletedStudent()

        assertThat(dao.getAllLocallyAdded()).isEmpty()
        assertThat(dao.getAllLocallyUpdated()).isEmpty()
        assertThat(dao.getAllLocallyDeleted()).isEmpty()

    }

    @After
    fun teardown(){
        database.close()
    }
}