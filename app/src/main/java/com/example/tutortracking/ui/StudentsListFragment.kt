package com.example.tutortracking.ui

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tutortracking.R
import com.example.tutortracking.adapters.StudentsAdapter
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SessionManager
import com.example.tutortracking.util.SimpleGesture
import com.example.tutortracking.viewmodels.StudentViewModel
import com.example.tutortracking.viewmodels.TutorViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates


@AndroidEntryPoint
class StudentsListFragment : Fragment(R.layout.fragment_students_list), SearchView.OnQueryTextListener {
    private var _binding: FragmentStudentsListBinding? = null
    private val binding:FragmentStudentsListBinding?
        get() = _binding!!
    private val tutorViewModel : TutorViewModel by activityViewModels()
    private val studentsViewModel : StudentViewModel by activityViewModels()
    private lateinit var adapter: StudentsAdapter

    override fun onResume() {
        super.onResume()
        val hasSessionStarted = (activity as MainActivity).hasSessionStarted
        if (hasSessionStarted) {
            binding!!.swipeRefreshLayout.isRefreshing = true
            syncData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsListBinding.bind(view)
        setHasOptionsMenu(true)
        setUpRV()
        setList()
        validateUser()
        subscribeToDeleteEvents()
        binding!!.swipeRefreshLayout.setOnRefreshListener {
                syncData()
             }
        }

    private fun validateUser() = viewLifecycleOwner.lifecycleScope.launch {
        tutorViewModel.shouldNavigateToRegister.collect{ shouldNavigate->
            if(shouldNavigate)
                navigateToLoginScreen()
        }
    }

    private fun syncData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            studentsViewModel.syncData {
                binding?.let {
                    it.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToLoginFragment())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.search_student)
        val searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_student)
            findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment())
        return super.onOptionsItemSelected(item)
    }

    private fun setList() = viewLifecycleOwner.lifecycleScope.launch {
        studentsViewModel.studentsList.collect {
            binding!!.noStudentsText.isVisible = it.isEmpty()
            adapter.submitList(it)
        }
    }

    private val simpleGestureCallBack = object : SimpleGesture() {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            when(direction){
                ItemTouchHelper.LEFT->{
                    val deletionTarget = adapter.currentList[viewHolder.bindingAdapterPosition]
                    studentsViewModel.deleteStudent(deletionTarget)
                    Snackbar.make(
                        binding!!.root,
                        "Student ${deletionTarget.studentName} was deleted",
                        Snackbar.LENGTH_LONG)
                        .setAction("Undo"){
                            studentsViewModel.addStudent(
                                deletionTarget.studentName.toString(),
                                deletionTarget.studentYear.toString(),
                                deletionTarget.studentSubject.toString(),
                                deletionTarget.studentPic
                            )
                        }.show()
                }
            }
        }
    }

    private fun subscribeToDeleteEvents() = viewLifecycleOwner.lifecycleScope.launch {
        studentsViewModel.deleteStudentState.collect { response->
            when(response){
                is Result.Loading-> showProgressBar()
                is Result.Error ->{
                    hideProgressBar()
                    Toast.makeText(requireContext(), response.message.toString(), Toast.LENGTH_LONG).show()
                }
                is Result.Success->{
                    hideProgressBar()
                    //Toast.makeText(requireContext(), response.data!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding!!.mainProgressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding!!.mainProgressBar.isVisible = true
    }

    private fun setUpRV(){
        adapter = StudentsAdapter {
            findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment(it))
        }
        binding!!.mainRecyclerView.also {
            it.adapter = adapter
            ItemTouchHelper(simpleGestureCallBack).attachToRecyclerView(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                studentsViewModel.getSearchedStudent(query).collect {
                    adapter.submitList(it)
                }
            }
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        query?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                studentsViewModel.getSearchedStudent(query).collect {
                    adapter.submitList(it)
                }
            }
        }
        return true
    }

}
