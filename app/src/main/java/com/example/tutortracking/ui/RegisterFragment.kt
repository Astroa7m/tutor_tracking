package com.example.tutortracking.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageBytes
import com.example.tutortracking.util.getImageString
import com.example.tutortracking.viewmodels.TutorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private var _binding: FragmentRegisterBinding?=null
    private val binding
        get() = _binding!!
    private val viewModel : TutorViewModel by activityViewModels()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        subscribeToTutorEvents()
        setUpLauncher()

        binding.registerRegisterChip.setOnClickListener{sendUserInput()}

        binding.registerImageView.setOnClickListener {
            Intent(Intent.ACTION_PICK).also {
                it.type = "image/*"
                launcher.launch(it)
            }
        }

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
                val byteArrImageString = getImageString(getImageBytes(imageUri, requireContext()))
                val getImageBitmap = decode(byteArrImageString)
                binding.registerImageView.setImageBitmap(getImageBitmap)
                binding.registerOnImageText.isVisible = false
            }else{
                Toast.makeText(requireContext(), "no image was selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendUserInput()  {
        val email = binding.registerEmailEt.text.toString()
        val name = binding.registerNameEt.text.toString()
        val password = binding.registerPasswordEt.text.toString()
        val modules = binding.registerModulesEt.text.toString()
        val byteArrayPic = if(imageUri!=null) getImageBytes(imageUri, requireContext()) else null
        viewModel.register(email, password, name, modules, byteArrayPic)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}