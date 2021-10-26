package com.example.tutortracking.ui

import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import app.cash.turbine.test
import com.example.tutortracking.DrawableMatcher.Companion.withDrawable
import com.example.tutortracking.R
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.launchFragmentInHiltContainer
import com.example.tutortracking.ui.ImageBitmapMatcher.Companion.withBitmap
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import kotlin.time.ExperimentalTime

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExperimentalCoroutinesApi
@HiltAndroidTest
@LargeTest
@ExperimentalTime
class ProfileFragmentTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun test_tutorInfoIsCorrect(){
        // user must be logged in in order to test default user (asd@asd.com, 12345678)
        var tutor: Tutor? = null
        launchFragmentInHiltContainer<ProfileFragment> {
            runBlocking{
                viewModel.currentTutor.test {
                    tutor = awaitItem()[0]
                }
            }
        }

        Log.d("TEST_TAG", "test_tutorInfoIsCorrect: $tutor")

        // photos may vary between users as it might be a bitmap from byte array or a drawable if null
        when{
            tutor?.profilePic!= null -> onView(withId(R.id.profile_image_view)).check(matches(withBitmap(tutor?.profilePic!!)))
            else -> onView(withId(R.id.profile_image_view)).check(matches(withDrawable(R.drawable.ic_user)))
        }

        onView((withId(R.id.profile_chip_group))).check(matches(hasChildCount(tutor?.modules!!.size)))
        onView(withId(R.id.profile_name_et)).check(matches(withText(tutor?.name)))
        onView(withId(R.id.profile_email_et)).check(matches(withText(tutor?.email)))
    }

    @Test
    fun zClickLogoutMenuItem_logoutAndNavigateToLoginScreen(){
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<ProfileFragment> {
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), navController)
            navController.setCurrentDestination(R.id.profileFragment)
        }

        onView(withId(R.id.logout)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.loginFragment)
    }

    @Test
    fun xClickOnEdit_enableFieldsAndShowHiddenFields(){
        launchFragmentInHiltContainer<ProfileFragment>()

        onView(withId(R.id.edit)).perform(click())
        // fields must be enabled and pw and modules et must be visible
        onView(withId(R.id.profile_name_et)).check(matches(isEnabled()))
        onView(withId(R.id.profile_image_view)).check(matches(isEnabled()))
        onView(withId(R.id.profile_email_et)).check(matches(isEnabled()))
        onView(withId(R.id.profile_modules_et)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.profile_add_chips_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.profile_password_et)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        // also done menu item should be visible and edit menu item should not
        onView(withId(R.id.edit)).check(doesNotExist())
        onView(withId(R.id.done)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

}