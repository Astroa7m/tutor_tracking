package com.example.tutortracking.ui

import android.app.Activity
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tutortracking.R
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.databinding.AddStudentBottomSheetBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageBytes
import com.example.tutortracking.util.getImageString
import com.example.tutortracking.viewmodels.StudentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
@AndroidEntryPoint
class AddStudentBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: AddStudentBottomSheetBinding?=null
    private val binding: AddStudentBottomSheetBinding
        get() = _binding!!
    private val viewModel: StudentViewModel by activityViewModels()
    private val args: AddStudentBottomSheetFragmentArgs by navArgs()
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddStudentBottomSheetBinding.inflate(inflater, container, false)
        val view = binding.root

        subscribeToAddingStudentEvent()
        subscribeToUpdateStudentEvent()

        return view
    }

    private fun subscribeToUpdateStudentEvent() = lifecycleScope.launch {
        viewModel.updateStudentState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.data!!.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(args.student==null)
            addNewStudent()
        else
            viewOrUpdateStudent()
    }

    private fun viewOrUpdateStudent() {
        doDisabling()
        setFields()
        binding.addStudentsDialogEdit.setOnClickListener {
            doEnabling()
        }
        binding.addStudentsDialogFab.setOnClickListener { updateStudent() }
    }

    private fun updateStudent() {
        val name = binding.addStudentsDialogName.text.toString()
        val year = binding.addStudentsDialogYear.text.toString()
        val subject = binding.addStudentsDialogSubject.text.toString()
        val imageByteArray = when{
            args.student!!.studentPic!=null && imageUri!=null -> getImageBytes(imageUri, requireContext())
            args.student!!.studentPic!=null -> args.student!!.studentPic
            else -> null
        }
        val id = args.student!!._id
        viewModel.updateStudent(name, year, subject, imageByteArray, id)

    }

    private fun setFields() {
        binding.apply {
            addStudentsDialogName.setText(args.student!!.studentName)
            addStudentsDialogYear.setText(args.student!!.studentYear.toString())
            addStudentsDialogSubject.setText(args.student!!.studentSubject)
            addStudentsDialogImage.apply {
                when{
                    args.student!!.studentPic != null-> setImageBitmap(decode(getImageString(args.student!!.studentPic)))
                    imageUri!=null -> setImageURI(imageUri)
                    else -> setImageResource(R.drawable.ic_user)
                }
            }
        }
    }

    private fun doEnabling() {
        binding.apply {
            disableOrEnableViews(true, addStudentsDialogYear, addStudentsDialogSubject, addStudentsDialogName)
            addStudentsDialogEdit.isVisible = false
            addStudentsDialogFab.isVisible = true
            addStudentsDialogChip.isVisible = true
        }
    }

    private fun doDisabling() {
        binding.apply {
            disableOrEnableViews(false, addStudentsDialogYear, addStudentsDialogSubject, addStudentsDialogName)
            addStudentsDialogEdit.isVisible = true
            addStudentsDialogFab.isVisible = false
            addStudentsDialogChip.isVisible = false
        }
    }

    private fun disableOrEnableViews(enable: Boolean, vararg views: View) {
            for(view in views){
                view.isEnabled = enable
            }
    }

    private fun addNewStudent() {
        binding.addStudentsDialogFab.setOnClickListener { addStudent() }
        binding.addStudentsDialogEdit.isVisible = false
    }

    private fun subscribeToAddingStudentEvent() = lifecycleScope.launch {
        viewModel.addStudentState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.data!!.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.addStudentsDialogProgressbar.isVisible = false
    }

    private fun showProgressBar() {
        binding.addStudentsDialogProgressbar.isVisible = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUpLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addStudentsDialogChip.setOnClickListener { startImageIntent() }
    }

    private fun startImageIntent() {
            Intent(Intent.ACTION_PICK).also {
                it.type = "image/*"
                launcher.launch(it)
        }
    }

    private fun addStudent(){
        val name = binding.addStudentsDialogName.text.toString()
        val year = binding.addStudentsDialogYear.text.toString()
        val subject = binding.addStudentsDialogSubject.text.toString()
        val imageByteArray = if(imageUri!=null) getImageBytes(imageUri, requireContext()) else null
        viewModel.addStudent(name, year, subject, imageByteArray)
    }

    private fun setUpLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode== Activity.RESULT_OK){
                imageUri = it.data?.data
                binding.addStudentsDialogImage.setImageURI(imageUri)
            }else{
                Toast.makeText(requireContext(), "no image was selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}