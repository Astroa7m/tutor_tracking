package com.example.tutortracking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.repository.TutorRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.doNamesOperations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@HiltViewModel
class StudentViewModel @Inject constructor(private val repository: TutorRepository) : ViewModel() {
    private val _addStudentState = MutableSharedFlow<Result<UserResponse>>()
    val addStudentState = _addStudentState.asSharedFlow()
    private val _updateStudentState = MutableSharedFlow<Result<UserResponse>>()
    val updateStudentState = _updateStudentState.asSharedFlow()
    private val _deleteStudentState = MutableSharedFlow<Result<UserResponse>>()
    val deleteStudentState = _deleteStudentState.asSharedFlow()
    val studentsList = repository.getAllStudentsLocally()

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
            _addStudentState.emit(Result.Error("Fields cannot be left empty"))
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

    suspend fun syncData(
        onFinish : (() -> Unit)? = null
    )
   {
       repository.sync()
       withContext(Dispatchers.Main){onFinish?.invoke()}

    }

    fun getSearchedStudent(query: String) = repository.searchStudent("%$query%")

}