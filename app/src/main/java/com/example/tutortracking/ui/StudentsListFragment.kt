package com.example.tutortracking.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tutortracking.R
import com.example.tutortracking.adapters.StudentsAdapter
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.databinding.FragmentStudentsListBinding
import com.example.tutortracking.util.EspressoIdlingResource
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
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class StudentsListFragment @Inject constructor(
    private val studentViewModel: StudentViewModel?
) : Fragment(R.layout.fragment_students_list), SearchView.OnQueryTextListener {
    private var _binding: FragmentStudentsListBinding? = null
    private val binding
    get() = _binding!!
    private val tutorViewModel : TutorViewModel by activityViewModels()
    lateinit var studentsViewModel : StudentViewModel
    private val colorDrawable = ColorDrawable(Color.parseColor("#BD4545"))
    private lateinit var deleteIcon : Drawable
    lateinit var adapter: StudentsAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentStudentsListBinding.bind(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentsListBinding.inflate(inflater, container, false)
        val view = binding.root

        studentsViewModel = studentViewModel ?: ViewModelProvider(requireActivity()).get(StudentViewModel::class.java)

        val hasSessionStarted: Boolean = try {
            (activity as MainActivity).hasSessionStarted
        }catch (e: Exception){true}

        if (hasSessionStarted){
            syncData()
        }

        setHasOptionsMenu(true)
        setUpRV()
        setList()
        deleteIcon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_delete)!!
        validateUser()
        subscribeToDeleteEvents()
        binding.swipeRefreshLayout.setOnRefreshListener {
            syncData()
        }

        adapter.setOnClickListener {
                navigateSafe(it)
        }

        return view
    }

    private fun validateUser() = viewLifecycleOwner.lifecycleScope.launch {
        tutorViewModel.shouldNavigateToRegister.collect{ shouldNavigate->
            if(shouldNavigate)
                navigateToLoginScreen()
        }
    }

    private fun syncData() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            binding.swipeRefreshLayout.isRefreshing = true
            studentsViewModel.syncData {
                binding.swipeRefreshLayout.isRefreshing = false
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
            R.id.add_student -> {
                navigateSafe()
                return true
            }
            R.id.by_name -> sortBy(SortOrder.BY_NAME)
            R.id.by_year -> sortBy(SortOrder.BY_YEAR)
            R.id.by_subject -> sortBy(SortOrder.BY_SUBJECT)
            R.id.dark_mode -> setTheme(AppCompatDelegate.MODE_NIGHT_YES)
            R.id.light_mode -> setTheme(AppCompatDelegate.MODE_NIGHT_NO)
            R.id.system_default -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        }
        return super.onOptionsItemSelected(item)
    }

    private fun setTheme(themeInt: Int) {
        tutorViewModel.updateThemePreferences(themeInt)
        AppCompatDelegate.setDefaultNightMode(themeInt)
    }

    private fun sortBy(sortOrder: SortOrder) {
        studentsViewModel.updateSortOrderPreferences(sortOrder)
    }

    private fun moveToFirst() {
       binding.mainRecyclerView.smoothScrollToPosition(0)
    }

    @ExperimentalCoroutinesApi
    private fun setList() = viewLifecycleOwner.lifecycleScope.launch {
        studentsViewModel.studentsList.collect {
            binding.noStudentsText.isVisible = it.isEmpty()
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
                        binding.root,
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
                colorDrawable.setBounds(itemView.left-dX.toInt(), itemView.top, itemView.right-dX.toInt(), itemView.bottom)
                deleteIcon.setBounds(((itemView.width/1.5).toInt())-iconMargin-(deleteIcon.intrinsicWidth)-(dX.toInt()/2), itemView.top+iconMargin, ((itemView.width/1.5).toInt())-iconMargin-(dX.toInt()), itemView.bottom-iconMargin)
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
        binding.mainProgressBar.isVisible = false
    }

    private fun showProgressBar() {
        binding.mainProgressBar.isVisible = true
    }

    private fun setUpRV(){
        adapter = StudentsAdapter()
            {
                moveToFirst()
            }
        binding.mainRecyclerView.also {
            it.adapter = adapter
            ItemTouchHelper(simpleGestureCallBack).attachToRecyclerView(it)
        }
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
            query!! == "" && adapter.currentList.isEmpty() -> binding.noStudentsText.text = getString(R.string.no_students)
            query != "" && adapter.currentList.isEmpty() ->  binding.noStudentsText.text = getString(R.string.no_student_from_search)
        }
    }

    private fun navigateSafe(student: LocalStudent?=null){
        try{
            if(findNavController().currentDestination?.id==R.id.studentsListFragment)
                findNavController().navigate(StudentsListFragmentDirections.actionStudentsListFragmentToAddStudentBottomSheetFragment(student))
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
