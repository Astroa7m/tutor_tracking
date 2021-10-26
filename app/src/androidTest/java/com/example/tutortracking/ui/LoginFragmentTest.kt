package com.example.tutortracking.ui

import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import com.example.tutortracking.R
import com.example.tutortracking.data.repository.TutorRepositoryFakeAndroid
import com.example.tutortracking.launchFragmentInHiltContainer
import com.example.tutortracking.viewmodels.TutorViewModel
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltAndroidTest
@MediumTest
class LoginFragmentTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: TutorTrackingFragmentsFactoryTest

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loggingInSuccessfully_navigateToStudentListFragment(){
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<LoginFragment>(fragmentFactory = fragmentFactory) {
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), navController)
            navController.setCurrentDestination(R.id.loginFragment)
        }

        onView(ViewMatchers.withId(R.id.login_email_et)).perform(replaceText("asd@asd.com"))
        onView(ViewMatchers.withId(R.id.login_password_et)).perform(replaceText("12345678"))
        onView(ViewMatchers.withId(R.id.login_login_chip)).perform(click())
        Truth.assertThat(navController.currentDestination?.id).isEqualTo(R.id.studentsListFragment)
    }

}