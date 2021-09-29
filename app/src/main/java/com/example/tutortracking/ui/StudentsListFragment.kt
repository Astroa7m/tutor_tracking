package com.example.tutortracking.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tutortracking.R
import com.example.tutortracking.adapters.StudentsAdapter
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SimpleGesture
import com.example.tutortracking.util.SortOrder
import com.example.tutortracking.viewmodels.StudentViewModel
import com.example.tutortracking.viewmodels.TutorViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class StudentsListFragment : Fragment(R.layout.fragment_students_list), SearchView.OnQueryTextListener {
    private var _binding: FragmentStudentsListBinding? = null
    private val binding:FragmentStudentsListBinding?
        get() = _binding!!
    private val tutorViewModel : TutorViewModel by activityViewModels()
    private val studentsViewModel : StudentViewModel by activityViewModels()
    private val colorDrawable = ColorDrawable(Color.parseColor("#F37575"))
    private lateinit var deleteIcon : Drawable
    private lateinit var adapter: StudentsAdapter

    override fun onResume() {
        super.onResume()
        val hasSessionStarted = (activity as MainActivity).hasSessionStarted
        if (hasSessionStarted) {
            binding!!.swipeRefreshLayout.isRefreshing = true
            syncData()
        }
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentsListBinding.bind(view)
        setHasOptionsMenu(true)
        setUpRV()
        setList()
        deleteIcon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_delete)!!
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
        when(item.itemId){
            R.id.add_student -> findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment())
            R.id.by_name -> sortBy(SortOrder.BY_NAME)
            R.id.by_year -> sortBy(SortOrder.BY_YEAR)
            R.id.by_subject -> sortBy(SortOrder.BY_SUBJECT)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sortBy(sortOrder: SortOrder) {
        studentsViewModel.updatePreferences(sortOrder)
    }

    private fun moveToFirst() {
       binding!!.mainRecyclerView.smoothScrollToPosition(0)
    }

    @ExperimentalCoroutinesApi
    private fun setList() = viewLifecycleOwner.lifecycleScope.launch {
        studentsViewModel.studentsList.collect {
            binding!!.noStudentsText.isVisible = it.isEmpty()
            adapter.submitList(it){
                moveToFirst()
            }
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

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height-deleteIcon.intrinsicHeight) / 2
            if(dX<0){
                colorDrawable.setBounds(itemView.left-dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                deleteIcon.setBounds(itemView.right-iconMargin-deleteIcon.intrinsicWidth, itemView.top+iconMargin, itemView.right-iconMargin-(dX.toInt()), itemView.bottom-iconMargin)
            }else{
                colorDrawable.setBounds(0,0,0,0)
                deleteIcon.setBounds(0,0,0,0)
            }
            colorDrawable.draw(c)
            deleteIcon.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
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
        adapter = StudentsAdapter ({
            findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment(it))
        },
            {
                moveToFirst()
            })
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
                studentsViewModel.searchQuery.emit(query)
            }
        }
       updateEmptyListText(query)
        return true
    }


    override fun onQueryTextChange(query: String?): Boolean {
        query?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                studentsViewModel.searchQuery.emit(query)
            }
        }
        updateEmptyListText(query)
        return true
    }

    private fun updateEmptyListText(query: String?) {
        when{
            query!! == "" && adapter.currentList.isEmpty() -> binding!!.noStudentsText.text = getString(R.string.no_students)
            query != "" && adapter.currentList.isEmpty() ->  binding!!.noStudentsText.text = getString(R.string.no_student_from_search)
        }
    }

}
