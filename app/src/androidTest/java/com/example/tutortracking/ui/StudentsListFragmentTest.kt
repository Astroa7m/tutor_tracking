package com.example.tutortracking.ui

import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.tutortracking.R
import com.example.tutortracking.adapters.StudentsAdapter
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.launchFragmentInHiltContainer
import com.example.tutortracking.util.EspressoIdlingResource
import com.example.tutortracking.viewmodels.StudentViewModel
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

@ExperimentalCoroutinesApi
@HiltAndroidTest
@MediumTest
class StudentsListFragmentTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: TutorTrackingFragmentsFactoryTest

    @Before
    fun setup(){
        hiltRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun test_recyclerViewIsVisible(){
        launchFragmentInHiltContainer<StudentsListFragment>()
        onView(withId(R.id.main_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun clickOnStudent_navigateToStudentBottomSheetFragment(){
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        launchFragmentInHiltContainer<StudentsListFragment>(fragmentFactory = fragmentFactory){
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), navController)
            studentsViewModel.addStudent("test", "1", "Math", null)
        }

        onView(withId(R.id.main_recycler_view)).perform(RecyclerViewActions.scrollToPosition<StudentsAdapter.StudentHolder>(0))
        onView(withId(R.id.main_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition<StudentsAdapter.StudentHolder>(0, click()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.addStudentBottomSheetFragment)
    }

    @Test
    fun clickAddStudentMenuItem_navigateToStudentBottomSheetFragment(){
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        launchFragmentInHiltContainer<StudentsListFragment>(fragmentFactory = fragmentFactory){
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), navController)
        }

        onView(withId(R.id.add_student)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.addStudentBottomSheetFragment)
    }

    @Test
    fun swipeLeftStudent_deleteStudent() {
        val listOfStudentBefore = mutableListOf<LocalStudent>()
        var studentsViewModelTest : StudentViewModel? = null
        launchFragmentInHiltContainer<StudentsListFragment>(fragmentFactory = fragmentFactory){
            studentsViewModel.addStudent("test", "1", "Math", null)
            runBlockingTest { studentsViewModel.repository.getAllStudentsAsList().forEach { listOfStudentBefore.add(it) } }
            studentsViewModelTest = studentsViewModel
        }

        assertThat(listOfStudentBefore).isNotEmpty()
        Log.d("TEST_TAG", "swipeLeftStudent_deleteStudent: $listOfStudentBefore")

        val listOfStudentAfter = mutableListOf<LocalStudent>()
        onView(withId(R.id.main_recycler_view)).perform(RecyclerViewActions.scrollToPosition<StudentsAdapter.StudentHolder>(0))
        onView(withId(R.id.main_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition<StudentsAdapter.StudentHolder>(0, swipeLeft()))
        runBlockingTest { studentsViewModelTest?.repository?.getAllStudentsAsList()?.forEach { listOfStudentAfter.add(it) } }

        Log.d("TEST_TAG", "swipeLeftStudent_deleteStudent: $listOfStudentAfter")
        assertThat(listOfStudentAfter).isEmpty()

    }


    @After
    fun teardown(){
       IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

}