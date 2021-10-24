package com.example.tutortracking

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.core.internal.deps.guava.base.Preconditions

inline fun <reified T: Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle?=null,
    @StyleRes styleRes: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    fragmentFactory: FragmentFactory?=null,
    crossinline action: T.() -> Unit = {}
){
    //making an intent to start the main activity or root of the tasks in the application
    val mainActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            HiltTestActivity::class.java
        )// attaching a style to the activity
    ).putExtra("androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY", styleRes)

    //launches the intent of the activity for testing and returns an instance of it
    ActivityScenario.launch<HiltTestActivity>(mainActivityIntent).onActivity { activity->
        //every activity has fragManager and every fragManger has fragment factory
        //which helps it to create fragments the same as viewModel factory
        //here we are attaching the passed fragment factory
        //to the test activity's fragManager factory
        fragmentFactory?.let {
            activity.supportFragmentManager.fragmentFactory = it
        }
        //creating a new fragment instance
        val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            Preconditions.checkNotNull(T::class.java.classLoader),
            T::class.java.name
        )

        //attaching passed args to this fragments args
        fragment.arguments = fragmentArgs

        activity.supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commitNow()

        (fragment as T).action()
    }


}