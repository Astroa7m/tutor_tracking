package com.example.tutortracking.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ListAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.adapters.StudentsAdapter
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class StudentsListFragment : Fragment(R.layout.fragment_students_list) {
    private var _binding: FragmentStudentsListBinding? =null
    private val binding:FragmentStudentsListBinding?
        get() = _binding!!
    private val viewModel : TutorViewModel by activityViewModels()
    val adapter: StudentsAdapter by lazy { StudentsAdapter{

    }}

    override fun onStart() {
        super.onStart()
        validateUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsListBinding.bind(view)
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.add_student){
            findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment())
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
