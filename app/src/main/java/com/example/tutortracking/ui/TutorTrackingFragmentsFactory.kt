package com.example.tutortracking.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class TutorTrackingFragmentsFactory @Inject constructor() : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            StudentsListFragment::class.java.name -> StudentsListFragment(null)
            LoginFragment::class.java.name -> LoginFragment(null)
            RegisterFragment::class.java.name -> RegisterFragment(null)
            else -> return super.instantiate(classLoader, className)
        }
    }
}