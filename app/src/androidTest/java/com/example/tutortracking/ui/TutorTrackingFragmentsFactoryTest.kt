package com.example.tutortracking.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.test.core.app.ApplicationProvider
import com.example.tutortracking.data.repository.TutorRepositoryFakeAndroid
import com.example.tutortracking.util.SessionManager
import com.example.tutortracking.viewmodels.StudentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class TutorTrackingFragmentsFactoryTest @Inject constructor() : FragmentFactory() {

    @ExperimentalCoroutinesApi
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            StudentsListFragment::class.java.name -> StudentsListFragment(StudentViewModel(TutorRepositoryFakeAndroid(), SessionManager(ApplicationProvider.getApplicationContext())))
            else -> return super.instantiate(classLoader, className)
        }
    }
}