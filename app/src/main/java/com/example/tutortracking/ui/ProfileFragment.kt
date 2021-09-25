package com.example.tutortracking.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentProfileBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageBytes
import com.example.tutortracking.util.getImageString
import com.example.tutortracking.viewmodels.StudentViewModel
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
    private var hasBeenUpdated = false
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUpLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        setHasOptionsMenu(true)
        subscribeToTutorLogoutEvents()
        subscribeToTutorUpdateEvents()
        updateInfoIfNeeded()
        setCurrentTutorInfo()
        (activity as MainActivity).hasSessionStarted = false
    }

    private fun updateInfoIfNeeded() {
        binding.profileUpdateChip.setOnClickListener {
            if(!hasBeenUpdated){
                binding.apply {
                    profileUpdateChip.text = "Done"
                    disableOrEnableViews(!hasBeenUpdated,profileEmailEt, profileNameEt, profileModulesEt)
                    profilePasswordEt.isVisible = true
                    profileUpdateChip.setChipBackgroundColorResource(R.color.synced)
                    profileImageView.setOnClickListener {
                        Intent(Intent.ACTION_PICK).also {
                            it.type = "image/*"
                            launcher.launch(it)
                        }
                    }
                }
                hasBeenUpdated = true
            }else{
                binding.apply {
                    profileUpdateChip.text = "Update"
                    profileUpdateChip.setChipBackgroundColorResource(R.color.my_greish)
                    disableOrEnableViews(!hasBeenUpdated,profileEmailEt, profileNameEt, profileModulesEt)
                    profileImageView.isClickable = false
                    profilePasswordEt.isVisible = false
                    viewModel.update(profileEmailEt.text.toString(),
                    profilePasswordEt.text.toString(),
                    profileNameEt.text.toString(),
                    profileModulesEt.text.toString(),
                        if(imageUri!=null) getImageBytes(imageUri, requireContext()) else null)
                }
                hasBeenUpdated = false
            }
        }
    }

    private fun setCurrentTutorInfo() = viewLifecycleOwner.lifecycleScope.launch{
        viewModel.currentTutor.collect { currentTutor->
            if(currentTutor.isNotEmpty()) {
                binding.profileImageView.apply {
                    when{
                        currentTutor[0].profilePic != null-> setImageBitmap(decode(getImageString(currentTutor[0].profilePic)))
                        imageUri!=null -> setImageURI(imageUri)
                        else -> setImageResource(R.drawable.ic_user)
                    }
                }
                binding.profileNameEt.setText(currentTutor[0].name)
                binding.profileEmailEt.setText(currentTutor[0].email)
                binding.profileStudentCount.text = getString(R.string.student_count, viewModel.getStudentsCount())
                binding.profileModulesEt.setText(currentTutor[0].modules.joinToString(separator = ","))
            }
        }
    }

    private fun disableOrEnableViews(shouldDisable: Boolean,vararg views: View){
        for(view in views){
            view.isEnabled = shouldDisable
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            viewModel.logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeToTutorLogoutEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.tutorLogoutState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.data.toString(), Toast.LENGTH_LONG).show()
                    findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
                }
            }
        }
    }

    private fun subscribeToTutorUpdateEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.tutorUpdateState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(),"Successfully updated", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setUpLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode== Activity.RESULT_OK){
                imageUri = it.data?.data
                binding.profileImageView.setImageURI(imageUri)
            }else{
                Toast.makeText(requireContext(), "no image was selected", Toast.LENGTH_SHORT).show()
            }
        }
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