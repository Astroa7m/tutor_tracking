package com.example.tutortracking.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.tutortracking.CoroutinesTestRule
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.Student
import com.example.tutortracking.data.repository.TutorRepositoryFake
import com.example.tutortracking.log
import com.example.tutortracking.logMessage
import com.example.tutortracking.util.Result
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class TutorViewModelTest {

    private lateinit var viewModelIfNotLoggedIn: TutorViewModel
    private lateinit var viewModelIfLoggedIn: TutorViewModel

    @get:Rule
    var coroutineRule = CoroutinesTestRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup(){
        viewModelIfNotLoggedIn = TutorViewModel(TutorRepositoryFake())
        viewModelIfLoggedIn = TutorViewModel(TutorRepositoryFake(tutorToken = "token"))
    }

    @ExperimentalTime
    @Test
    fun `emitting loading before checking result`() =runBlocking{
        viewModelIfNotLoggedIn.tutorRegisterState.test {
            viewModelIfNotLoggedIn.register("", "", "", emptyList(), null)
            assertThat(awaitItem().log().logMessage()).isInstanceOf(Result.Loading::class.java)
            assertThat(awaitItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `register tutor with empty fields`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn.tutorRegisterState.test {
            viewModelIfNotLoggedIn.register("", "", "", emptyList(), null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
        }
    }


    @ExperimentalTime
    @Test
    fun `register tutor without internet connection`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn = TutorViewModel(TutorRepositoryFake(false))
        viewModelIfNotLoggedIn.tutorRegisterState.test {
            viewModelIfNotLoggedIn.register("coolTutor@tut.com", "mistletoe123", "tutMe", listOf("da"), null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }
    @ExperimentalTime
    @Test
    fun `register tutor with valid fields`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn.tutorRegisterState.test {
            viewModelIfNotLoggedIn.register("test@test.com", "issaCPolypW", "Sami", listOf("da"), null)
            assertThat(expectMostRecentItem().data?.token).isEqualTo("justToken")
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging tutor with empty fields`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn.tutorLoginState.test {
            viewModelIfNotLoggedIn.login("","")
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `testing error message while logging in`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn.tutorLoginState.test {
            viewModelIfNotLoggedIn.login("","")
            assertThat(expectMostRecentItem().message).isEqualTo("Some fields might be empty")
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging tutor with valid fields`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfNotLoggedIn.tutorLoginState.test {
            viewModelIfNotLoggedIn.login("test@test.com","123123123")
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `updating tutor info with empty fields`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfLoggedIn.tutorUpdateState.test {
            viewModelIfLoggedIn.update("","","", emptyList(),null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `updating tutor info with at least 3 entered fields or more`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfLoggedIn.tutorUpdateState.test {
            viewModelIfLoggedIn.update("123","1234567","sami", listOf("math", "dada"),null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `ensures tutor is logged in`() = coroutineRule.testDispatcher.runBlockingTest{
        viewModelIfLoggedIn.currentTutor.test {
            viewModelIfLoggedIn.login("test@test.com", "dadadadad")
            assertThat(expectMostRecentItem().log()).hasSize(1)
        }
    }

    @ExperimentalTime
    @Test
    fun `logging user tutor out removes current tutor from session`() = runBlocking{
        viewModelIfLoggedIn.currentTutor.test {
            viewModelIfLoggedIn.login("test@test.com", "dadadadad")
            viewModelIfLoggedIn.logout()
            assertThat(awaitItem().log()).isEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging user tutor out removes current tutor's token from session`() = runBlocking{
        viewModelIfLoggedIn = TutorViewModel(TutorRepositoryFake(true, "token"))
        viewModelIfLoggedIn.tutorLogoutState.test {
            viewModelIfLoggedIn.login("test@test.com", "dadadadad")
            viewModelIfLoggedIn.logout()
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `return current user students count`() = coroutineRule.testDispatcher.runBlockingTest {
        viewModelIfLoggedIn.repository.addStudent(LocalStudent("da", 1, "da", "da", null, _id = "da"))
        val studentCount = viewModelIfLoggedIn.getStudentsCount()
        println("current tutor student count = $studentCount")
        assertThat(studentCount).isEqualTo(1)
    }

}


