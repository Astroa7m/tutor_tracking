package com.example.tutortracking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.remotedata.models.Login
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.repository.TutorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.areFieldsEmpty
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class TutorViewModel @Inject constructor(private val repository: TutorRepository) : ViewModel(){
        private val _tutorState = MutableSharedFlow<Result<UserResponse>>()
        val tutorState = _tutorState.asSharedFlow()
        private val _shouldNavigateToRegister = MutableSharedFlow<Boolean>()
        val shouldNavigateToRegister = _shouldNavigateToRegister.asSharedFlow()

        init {
            validateUser()
        }

    fun register(
        email: String,
        password: String,
        name: String,
        modules: String,
        profilePic: ByteArray?
    ) = viewModelScope.launch {
        _tutorState.emit(Result.Loading())
        if(areFieldsEmpty(email.trim(), password.trim(), name.trim(), modules.trim())){
            _tutorState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val modulesList = modules.split(",").map { it.trim() }
        val tutor = Register(email.trim(), password.trim(), name.trim(), modulesList, profilePic)
        _tutorState.emit(repository.register(tutor))
    }

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _tutorState.emit(Result.Loading())
        if(areFieldsEmpty(email = email, password = password)){
            _tutorState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Login(email.trim(), password.trim())
        _tutorState.emit(repository.login(tutor))
    }

    private fun validateUser() = viewModelScope.launch {
        val token = repository.validateUser()
        if(token==null)
            _shouldNavigateToRegister.emit(true)
        else
            _shouldNavigateToRegister.emit(false)
    }

}