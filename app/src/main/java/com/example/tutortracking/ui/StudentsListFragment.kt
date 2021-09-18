package com.example.tutortracking.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import com.example.tutortracking.util.SessionManager
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class StudentsListFragment : Fragment() {
    private var _binding: FragmentStudentsListBinding? =null
    private val binding
        get() = _binding!!
    @Inject
    lateinit var sessionManager: SessionManager
    private val viewModel : TutorViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsListBinding.bind(view)

        validateUser()

    }

    private fun validateUser() = lifecycleScope.launch {
        viewModel.shouldNavigateToRegister.collect{ shouldNavigate->
            if(shouldNavigate)
                navigateToLoginScreen()
        }
    }


    private fun navigateToLoginScreen() {
        findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToLoginFragment())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
