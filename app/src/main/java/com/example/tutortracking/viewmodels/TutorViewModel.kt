package com.example.tutortracking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.remotedata.models.Login
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.data.remotedata.models.Update
import com.example.tutortracking.data.repository.TutorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.areFieldsEmpty
import com.example.tutortracking.util.capitalize
import com.example.tutortracking.util.doNamesOperations
import kotlinx.coroutines.flow.*
import java.util.*

@HiltViewModel
class TutorViewModel @Inject constructor(@PublishedApi internal val repository: TutorRepository) : ViewModel(){
        private val _tutorRegisterState = MutableSharedFlow<Result<UserResponse>>()
        val tutorRegisterState = _tutorRegisterState.asSharedFlow()
        private val _tutorLoginState = MutableSharedFlow<Result<UserResponse>>()
        val tutorLoginState = _tutorLoginState.asSharedFlow()
        private val _tutorLogoutState = MutableSharedFlow<Result<String>>()
        val tutorLogoutState = _tutorLogoutState.asSharedFlow()
        private val _shouldNavigateToRegister = MutableSharedFlow<Boolean>()
        val shouldNavigateToRegister = _shouldNavigateToRegister.asSharedFlow()
        private val _tutorUpdateState = MutableSharedFlow<Result<UserResponse>>()
        val tutorUpdateState = _tutorUpdateState.asSharedFlow()
        val currentTutor = repository.getCurrentUser()

    init {
        validateUser()
        }

    fun register(
        email: String,
        password: String,
        name: String,
        modules: List<String>,
        profilePic: ByteArray?
    ) = viewModelScope.launch {
        _tutorRegisterState.emit(Result.Loading())
        if(areFieldsEmpty(email.trim(), password.trim(), name.trim(), modules)){
            _tutorRegisterState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Register(
            email.trim().lowercase(),
            password.trim(),
            name.doNamesOperations(),
            modules,
            profilePic)
        val result = repository.register(tutor)
        _tutorRegisterState.emit(result)
    }

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _tutorLoginState.emit(Result.Loading())
        if(areFieldsEmpty(email = email, password = password)){
            _tutorLoginState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Login(email.trim().lowercase(), password.trim())
        _tutorLoginState.emit(repository.login(tutor))
    }

    fun update(
        email: String,
        password: String,
        name: String,
        modules: List<String>,
        profilePic: ByteArray?
    ) = viewModelScope.launch {
        _tutorUpdateState.emit(Result.Loading())
        if(areFieldsEmpty(email.trim(), name = name.trim(), modules =  modules)){
            _tutorUpdateState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
       val tutor = Update(
            email.trim().lowercase(),
            if(password.isEmpty())
                null
            else
                password.trim(),
            name.doNamesOperations(),
            modules,
            profilePic)
        _tutorUpdateState.emit(repository.update(tutor))
    }

    private fun validateUser() = viewModelScope.launch {
        val token = repository.validateUser()
        if(token==null)
            _shouldNavigateToRegister.emit(true)
        else
            _shouldNavigateToRegister.emit(false)
    }

    suspend fun getStudentsCount() : Int{
        return repository.getAllStudentsAsList().size
    }

    fun logout() = viewModelScope.launch {
        _tutorLogoutState.emit(Result.Loading())
        _tutorLogoutState.emit(repository.logout())
    }

}