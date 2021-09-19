package com.example.tutortracking.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentProfileBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageString
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: TutorViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        setCurrentTutorInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        setHasOptionsMenu(true)

    }

    private fun setCurrentTutorInfo() = lifecycleScope.launch {
        viewModel.currentTutor.collect { tutor ->
            binding.profileImageView.setImageBitmap(decode(getImageString(tutor[0].profilePic!!)))
            binding.profileNameEt.setText(tutor[0].name)
            binding.profileEmailEt.setText(tutor[0].email)
            binding.profileModulesEt.setText(tutor[0].modules.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    private fun hideProgressBar() {
        binding.profileProgressbar.isVisible = false
    }

    private fun showProgressBar() {
        binding.profileProgressbar.isVisible = true
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}