package com.example.tutortracking.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.FragmentFactory
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tutortracking.DrawableMatcher.Companion.withDrawable
import com.example.tutortracking.R
import com.example.tutortracking.launchFragmentInHiltContainer
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@MediumTest
class RegisterFragmentTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: TutorTrackingFragmentsFactoryTest

    @Before
    fun setup() {
        hiltRule.inject()
        init()
    }

    @Test
    fun testCameraIntent(){
        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory)

        val expectedIntent = CoreMatchers.allOf(
            IntentMatchers.hasAction(Intent.ACTION_PICK),
            IntentMatchers.hasType("image/*")
        )

        val activityResult = createGalleryPickActivityResultStub()

        intending(expectedIntent).respondWith(activityResult)

        onView(withId(R.id.register_image_view)).perform(click())

        intended(expectedIntent)

    }

    @Test
    fun registeringSuccessfully_navigateToStudentListFragment(){
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInHiltContainer<RegisterFragment>(fragmentFactory = fragmentFactory){
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(requireView(), navController)
            navController.setCurrentDestination(R.id.registerFragment)
        }

        onView(withId(R.id.register_name_et)).perform(replaceText("someTutor"))
        onView(withId(R.id.register_email_et)).perform(replaceText("someTutor@teach.com"))
        onView(withId(R.id.register_password_et)).perform(replaceText("tutor12345"))
        onView(withId(R.id.register_modules_et)).perform(replaceText("math"))
        onView(withId(R.id.register_add_chips_button)).perform(click())
        onView(withId(R.id.register_register_button)).perform(scrollTo())
            .perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.studentsListFragment)

    }

    private fun createGalleryPickActivityResultStub(): Instrumentation.ActivityResult {
        val resources: Resources = InstrumentationRegistry.getInstrumentation().context.resources
        val imageUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    resources.getResourcePackageName(R.drawable.ic_launcher_background) + '/' +
                    resources.getResourceTypeName(R.drawable.ic_launcher_background) + '/' +
                    resources.getResourceEntryName(R.drawable.ic_launcher_background)
        )
        val resultIntent = Intent()
        resultIntent.data = imageUri
        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
    }

    @After
    fun teardown(){
        release()
    }

}