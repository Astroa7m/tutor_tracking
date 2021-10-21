package com.example.tutortracking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.repository.TutorRepository
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SessionManager
import com.example.tutortracking.util.SortOrder
import com.example.tutortracking.util.doNamesOperations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.*

@HiltViewModel
class StudentViewModel @Inject constructor(
    @PublishedApi internal val repository: TutorRepository,
    @PublishedApi internal val sessionManager: SessionManager) : ViewModel() {

    private val _addStudentState = MutableSharedFlow<Result<UserResponse>>()
    val addStudentState = _addStudentState.asSharedFlow()
    private val _updateStudentState = MutableSharedFlow<Result<UserResponse>>()
    val updateStudentState = _updateStudentState.asSharedFlow()
    private val _deleteStudentState = MutableSharedFlow<Result<UserResponse>>()
    val deleteStudentState = _deleteStudentState.asSharedFlow()

    //making mutable states of these two because these are going to change gradually and because they need an initial value
    val searchQuery = MutableStateFlow("")
    private val sortOrder = sessionManager.filterPreferences

    @ExperimentalCoroutinesApi
    //a flow that combines all the changes to happen to the previous flows and
    //return a flow<list> of the students depending on the user filtering and search
    // and combine function does the exact thing as it returns the changes to these flows together
    // moving the flows into the pair to easily return them as one value
    val studentsList = combine(
        searchQuery,
        sortOrder
    ){searchQuery, sortOrder ->
        Pair(searchQuery, sortOrder)
    }.flatMapLatest { (searchQuery, sortOrder)->
        repository.getAllStudentsLocally(searchQuery, sortOrder)
    }


    fun addStudent(
        name: String,
        year: String,
        subject: String,
        studentImageByteArray: ByteArray?)
    = viewModelScope.launch {
        if(name.isEmpty() || year.isEmpty() || subject.isEmpty()){
            _addStudentState.emit(Result.Error("Fields cannot be left empty"))
            return@launch
        }
        _addStudentState.emit(Result.Loading())
        val result = repository.addStudent(LocalStudent(name.doNamesOperations(), year.trim().toInt(),
            subject.trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, studentPic = studentImageByteArray, _id = UUID.randomUUID().toString()))
        _addStudentState.emit(result)
    }

    fun updateStudent(
        name: String,
        year: String,
        subject: String,
        studentImageByteArray: ByteArray?,
        id: String
    ) = viewModelScope.launch {
        if(name.isEmpty() || year.isEmpty() || subject.isEmpty()){
            _updateStudentState.emit(Result.Error("Fields cannot be left empty"))
            return@launch
        }
        _updateStudentState.emit(Result.Loading())
        val result = repository.updateStudent(LocalStudent(name.doNamesOperations(), year.trim().toInt(),
            subject.trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, studentPic = studentImageByteArray, _id = id), id)
        _updateStudentState.emit(result)
    }

    fun deleteStudent(
        student: LocalStudent
    ) = viewModelScope.launch {
        _deleteStudentState.emit(Result.Loading())
        _deleteStudentState.emit(repository.deleteStudent(student))
    }

    suspend inline fun syncData(
        crossinline onFinish: () -> Unit
    )
   {
       repository.sync()
       withContext(Dispatchers.Main){onFinish()}

    }

    fun updateSortOrderPreferences(sortingOrder: SortOrder) = viewModelScope.launch {
        sessionManager.updateSortingOrder(sortingOrder)
    }

    suspend fun getStudentsTutorModules() = repository.getTutorModules().split(",")

}