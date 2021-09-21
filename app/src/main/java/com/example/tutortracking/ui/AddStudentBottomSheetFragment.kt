package com.example.tutortracking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tutortracking.R
import com.example.tutortracking.databinding.AddStudentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddStudentBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: AddStudentBottomSheetBinding?=null
    private val binding: AddStudentBottomSheetBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return AddStudentBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}