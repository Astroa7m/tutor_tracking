package com.example.tutortracking.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class TutorTrackingFragmentsFactory @Inject constructor() : FragmentFactory() {

    @ExperimentalCoroutinesApi
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            StudentsListFragment::class.java.name -> StudentsListFragment(null)
            else -> return super.instantiate(classLoader, className)
        }
    }
}