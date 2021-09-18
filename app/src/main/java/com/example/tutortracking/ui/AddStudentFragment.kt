package com.example.tutortracking.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentAddStudentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddStudentFragment : Fragment(R.layout.fragment_add_student) {
    private var _binding: FragmentAddStudentBinding? =null
    private val binding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddStudentBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}