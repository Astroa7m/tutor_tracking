package com.example.tutortracking.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentRegisterBinding
import com.example.tutortracking.util.*
import com.example.tutortracking.viewmodels.TutorViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding?=null
    private val binding get() = _binding!!
    private val viewModel : TutorViewModel by activityViewModels()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private var tutorModules = mutableListOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUpLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        subscribeToTutorRegisterEvents()

        (activity as MainActivity).hasSessionStarted = true

        binding.registerRegisterButton.setOnClickListener{
            sendUserInput()
        }

        binding.registerAddChipsButton.setOnClickListener{
            if(binding.registerModulesEt.text.toString().isNotEmpty())
                addChip(binding.registerModulesEt.text.toString())
        }

        binding.registerImageView.setOnClickListener {
            Intent(Intent.ACTION_PICK).also {
                it.type = "image/*"
                launcher.launch(it)
            }
        }
        return view
    }

    private fun addChip(chipText: String) {
        if(!binding.registerChipGroup.isVisible)
            binding.registerChipGroup.isVisible = true
        val chip = Chip(requireContext())
        chip.text = chipText.capitalize()
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            binding.registerChipGroup.removeView(chip)
            tutorModules.remove(chip.text)
            if(tutorModules.size==0)
                binding.registerChipGroup.isVisible = false
        }
        binding.registerChipGroup.addView(chip)
        tutorModules.add(chipText.capitalize())
    }

    private fun subscribeToTutorRegisterEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.tutorRegisterState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                    findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToStudentsListFragment())
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.registerProgressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding.registerProgressBar.isVisible = true
    }

    private fun setUpLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode==RESULT_OK){
               imageUri = it.data?.data
                binding.registerImageView.setImageURI(imageUri)
                binding.registerOnImageText.isVisible = false
            }else{
                Toast.makeText(requireContext(), "no image was selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendUserInput() {
        val email = binding.registerEmailEt.text.toString()
        val name = binding.registerNameEt.text.toString()
        val password = binding.registerPasswordEt.text.toString()
        val byteArrayPic = if(imageUri!=null) getImageBytes(imageUri, requireContext()) else null
        viewModel.register(email, password, name, tutorModules, byteArrayPic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}