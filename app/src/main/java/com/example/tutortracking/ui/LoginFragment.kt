package com.example.tutortracking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentLoginBinding
import com.example.tutortracking.util.EspressoIdlingResource
import com.example.tutortracking.util.Result
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment @Inject constructor(val tutorViewModel: TutorViewModel?) : Fragment() {
    private var _binding: FragmentLoginBinding?=null
    private val binding get() = _binding!!
    lateinit var viewModel : TutorViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = tutorViewModel ?: ViewModelProvider(requireActivity()).get(TutorViewModel::class.java)

        subscribeToTutorLoginEvents()

        try { (activity as MainActivity).hasSessionStarted = true } catch (e: Exception) { }

        binding.haveAccountTv.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        binding.loginLoginChip.setOnClickListener { sendUserInput() }
        return view
    }

    private fun sendUserInput() {
        val email = binding.loginEmailEt.text.toString()
        val password = binding.loginPasswordEt.text.toString()
        viewModel.login(email, password)
    }

    private fun subscribeToTutorLoginEvents() = viewLifecycleOwner.lifecycleScope.launch {
       repeatOnLifecycle(Lifecycle.State.STARTED){
           viewModel.tutorLoginState.collect { response->
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
    }

    private fun hideProgressBar() {
        binding.loginProgressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding.loginProgressBar.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}