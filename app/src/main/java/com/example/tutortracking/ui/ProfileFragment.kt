package com.example.tutortracking.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentProfileBinding
import com.example.tutortracking.util.*
import com.example.tutortracking.viewmodels.TutorViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var viewModel: TutorViewModel
    private var hasBeenUpdated = false
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private lateinit var done : MenuItem
    private lateinit var edit : MenuItem
    var tutorModules = mutableListOf<String>()
    private var hasChipsBeenSet = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUpLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(requireActivity()).get(TutorViewModel::class.java)

        setHasOptionsMenu(true)
        subscribeToTutorLogoutEvents()
        subscribeToTutorUpdateEvents()
        setCurrentTutorInfo()
        try{ (activity as MainActivity).hasSessionStarted = false }catch (e: Exception){ }

        binding.profileAddChipsButton.setOnClickListener {
            if(binding.profileModulesEt.text.toString().isNotEmpty())
                addChip(binding.profileModulesEt.text.toString())
        }

        return view
    }

    private fun addChip(chipText: String) {
        binding.profileModulesEt.setText("")
        Chip(requireContext()).apply {
            text = chipText.capitalize()
            isCloseIconVisible = true
            isClickable = false
            isFocusable = false
            setOnCloseIconClickListener {
                binding.profileChipGroup.removeView(this)
                tutorModules.remove(chipText.capitalize())
            }
            binding.profileChipGroup.addView(this)
            tutorModules.add(chipText.capitalize())
        }
    }

    private fun updateProfile() {
            if(!hasBeenUpdated){

                binding.apply {
                    val chipsCount = binding.profileChipGroup.childCount
                    repeat(chipsCount) { index ->
                        (profileChipGroup[index] as Chip).apply {
                            isClickable = false
                            isCloseIconVisible = true
                            isFocusable = false
                            setOnCloseIconClickListener {
                                binding.profileChipGroup.removeView(this)
                                tutorModules.remove(this.text)
                            }
                        }
                    }
                    disableOrEnableViews(!hasBeenUpdated,profileEmailEt, profileNameEt)
                    profilePasswordEt.isVisible = true
                    profileModulesEt.isVisible = true
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
                    val chipsCount = binding.profileChipGroup.childCount
                    repeat(chipsCount) { index ->
                        (profileChipGroup[index] as Chip).apply {
                            isClickable = false
                            isCloseIconVisible = false
                            isFocusable = false
                        }
                    }
                    disableOrEnableViews(!hasBeenUpdated,profileEmailEt, profileNameEt)
                    profileImageView.isClickable = false
                    profilePasswordEt.isVisible = false
                    profileModulesEt.isVisible = false
                    val email = profileEmailEt.text.toString()
                    val password = profilePasswordEt.text.toString()
                    val name = profileNameEt.text.toString()
                    viewModel.update(email, password, name, tutorModules, if(imageUri!=null) getImageBytes(imageUri, requireContext()) else null)
                }
                hasBeenUpdated = false
            }
    }

    private fun setCurrentTutorInfo() = viewLifecycleOwner.lifecycleScope.launch{
            val currentTutor = viewModel.currentTutor
            if(currentTutor!=null) {
                binding.profileImageView.apply {
                    when{
                        currentTutor.profilePic != null-> setImageBitmap(decode(getImageString(currentTutor.profilePic)))
                        imageUri!=null -> setImageURI(imageUri)
                        else -> setImageResource(R.drawable.ic_user)
                    }
                }
                binding.profileNameEt.setText(currentTutor.name)
                binding.profileEmailEt.setText(currentTutor.email)
                binding.profileStudentCount.text = getString(R.string.student_count, viewModel.getStudentsCount())
                currentTutor.modules.forEach { module->
                    tutorModules.add(module.capitalize())
                }
                if(!hasChipsBeenSet){
                    setChips()
                    hasChipsBeenSet = true
                }
            }
    }

    private fun setChips() {
        tutorModules.forEach { module ->
            Chip(requireContext()).apply {
                text = module.capitalize()
                isCloseIconVisible = false
                isClickable = false
                isFocusable = false
                binding.profileChipGroup.addView(this)
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
        done = menu.findItem(R.id.done)
        edit = menu.findItem(R.id.edit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout-> viewModel.logout()
            R.id.edit -> {
                try{
                updateProfile()
                edit.isVisible = !hasBeenUpdated
                done.isVisible = hasBeenUpdated
                }catch(e:Exception){}
            }
            R.id.done->{
                try {
                    updateProfile()
                    edit.isVisible = !hasBeenUpdated
                    done.isVisible = hasBeenUpdated
                }catch(e:Exception){}
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeToTutorLogoutEvents() = viewLifecycleOwner.lifecycleScope.launch {
       repeatOnLifecycle(Lifecycle.State.STARTED){
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
    }

    private fun subscribeToTutorUpdateEvents() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            viewModel.tutorUpdateState.collect { response->
                when(response){
                    is Result.Loading-> showProgressBar()
                    is Result.Error ->{
                        hideProgressBar()
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                        findNavController().run {
                            popBackStack()
                            navigate(R.id.profileFragment)
                        }
                    }
                    is Result.Success->{
                        hideProgressBar()
                        Toast.makeText(requireContext(),"Successfully updated", Toast.LENGTH_LONG).show()
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}