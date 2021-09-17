package com.example.tutortracking.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tutortracking.R
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StudentsListFragment : Fragment() {
    private var _binding: FragmentStudentsListBinding? =null
    private val binding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsListBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
