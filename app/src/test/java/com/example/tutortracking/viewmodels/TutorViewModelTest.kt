package com.example.tutortracking.viewmodels

import app.cash.turbine.test
import com.example.tutortracking.data.repository.TutorRepositoryFake
import com.example.tutortracking.util.Result
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class TutorViewModelTest {

    private lateinit var viewModel: TutorViewModel

    @Before
    fun setup(){
        viewModel = TutorViewModel(TutorRepositoryFake())
    }

    @ExperimentalTime
    @Test
    fun `emitting loading before checking result`() =runBlocking{
        viewModel.tutorRegisterState.test {
            viewModel.register("", "", "", emptyList(), null)
            assertThat(awaitItem().log().logMessage()).isInstanceOf(Result.Loading::class.java)
            assertThat(awaitItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `register tutor with empty fields`() = runBlockingTest{
        viewModel.tutorRegisterState.test {
            viewModel.register("", "", "", emptyList(), null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
        }
    }


    @ExperimentalTime
    @Test
    fun `register tutor without internet connection`() = runBlockingTest{
        viewModel = TutorViewModel(TutorRepositoryFake(false))
        viewModel.tutorRegisterState.test {
            viewModel.register("coolTutor@tut.com", "mistletoe123", "tutMe", listOf("da"), null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }
    @ExperimentalTime
    @Test
    fun `register tutor with valid fields`() = runBlockingTest{
        viewModel.tutorRegisterState.test {
            viewModel.register("test@test.com", "issaCPolypW", "Sami", listOf("da"), null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging tutor with empty fields`() = runBlockingTest{
        viewModel.tutorLoginState.test {
            viewModel.login("","")
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `testing error message while logging in`() = runBlockingTest{
        viewModel.tutorLoginState.test {
            viewModel.login("","")
            assertThat(expectMostRecentItem().message).isEqualTo("Some fields might be empty")
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging tutor with valid fields`() = runBlockingTest{
        viewModel.tutorLoginState.test {
            viewModel.login("test@test.com","123123123")
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `updating tutor info with empty fields`() = runBlockingTest{
        viewModel.tutorUpdateState.test {
            viewModel.update("","","", emptyList(),null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Error::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `updating tutor info with at least 3 entered fields or more`() = runBlockingTest{
        viewModel = TutorViewModel(TutorRepositoryFake(true, "token"))
        viewModel.tutorUpdateState.test {
            viewModel.update("123","1234567","sami", listOf("math", "dada"),null)
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging user tutor out removes current tutor from session`() = runBlocking{
        viewModel = TutorViewModel(TutorRepositoryFake(true, "token"))
        viewModel.currentTutor.test {
            viewModel.logout()
            assertThat(awaitItem().log()).isEmpty()
            cancelAndConsumeRemainingEvents()
        }
    }

    @ExperimentalTime
    @Test
    fun `logging user tutor out removes current tutor's token from session`() = runBlocking{
        viewModel = TutorViewModel(TutorRepositoryFake(true, "token"))
        viewModel.tutorLogoutState.test {
            viewModel.logout()
            assertThat(expectMostRecentItem().log().logMessage()).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `return current user students count`() = runBlockingTest {
        val studentCount = viewModel.getStudentsCount()
        println("current tutor student count = $studentCount")
        assertThat(studentCount).isEqualTo(0)
    }

}

private fun <E> Collection<E>.log(): Collection<E> {
    print("list size = ${ this.size }")
    return this
}

private fun <T> Result<T>.log(msg: String?=null): Result<T> {
    println(if(msg!=null) "In $msg: ${this::class.java.simpleName}" else this::class.java.simpleName)
    return this
}

private fun <T> Result<T>.logMessage(): Result<T> {
    println("\tmessage = ${this.message}")
    return this
}

