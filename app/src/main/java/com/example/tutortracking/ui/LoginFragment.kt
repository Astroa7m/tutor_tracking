package com.example.tutortracking.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentLoginBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private var _binding: FragmentLoginBinding?=null
    private val binding
        get() = _binding!!
    private val viewModel : TutorViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        subscribeToTutorEvents()

        binding.haveAccountTv.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.loginLoginChip.setOnClickListener { sendUserInput() }

    }

    private fun sendUserInput() {
        val email = binding.loginEmailEt.text.toString()
        val password = binding.loginPasswordEt.text.toString()
        viewModel.login(email, password)
    }

    private fun subscribeToTutorEvents() = lifecycleScope.launch {
        viewModel.tutorState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToStudentsListFragment())
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.loginProgressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding.loginProgressBar.isVisible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}