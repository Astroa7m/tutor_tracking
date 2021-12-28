package com.example.tutortracking.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.remotedata.models.Login
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.data.remotedata.models.Update
import com.example.tutortracking.data.repository.TutorRepository
import com.example.tutortracking.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TutorViewModel @Inject constructor(val repository: TutorRepository) :
    ViewModel() {
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
    var currentTutor: Tutor? = null
    private val _messagesState = MutableStateFlow<List<ChatBubble>>(listOf(ChatBubble.Loading))
    val messagesState = _messagesState.asStateFlow()
    var lastIndex = 0

    init {
        viewModelScope.launch {
            currentTutor = repository.getCurrentUser()
            validateUser()
        }
    }

    fun connectToChat() = viewModelScope.launch{
        getAllMessages()
        repository.openSession()
        repository.observeMessage().onEach { message->

            val oldList = _messagesState.value.toMutableList()
            if (message.senderId == currentTutor?._id)
                oldList.add(ChatBubble.Outgoing(message))
            else
                oldList.add((ChatBubble.Incoming(message)))
            _messagesState.value = oldList
            lastIndex = oldList.lastIndex
        }.launchIn(viewModelScope)
    }

    private fun getAllMessages() = viewModelScope.launch {
        val listOfChatBubble = mutableListOf<ChatBubble>()
        repository.getAllMessages().forEach { message->
            if (message.senderId == currentTutor?._id)
                listOfChatBubble.add(ChatBubble.Outgoing(message))
            else
                listOfChatBubble.add((ChatBubble.Incoming(message)))
        }
        lastIndex = listOfChatBubble.lastIndex
        _messagesState.emit(listOfChatBubble)
    }

    fun sendMessage(message: String) = viewModelScope.launch {
        if (message.isNotEmpty()) {
            repository.sendMessage(message.trim())
        }
    }

    fun disconnect() = viewModelScope.launch {
        repository.disconnect()
    }

    fun register(
        email: String,
        password: String,
        name: String,
        modules: List<String>,
        profilePic: ByteArray?
    ) = viewModelScope.launch {
        _tutorRegisterState.emit(Result.Loading())
        if (areFieldsEmpty(email.trim(), password.trim(), name.trim(), modules)) {
            _tutorRegisterState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Register(
            email.trim().lowercase(),
            password.trim(),
            name.doNamesOperations(),
            modules,
            profilePic
        )
        val result = repository.register(tutor)
        _tutorRegisterState.emit(result)
        currentTutor = repository.getCurrentUser()
    }

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _tutorLoginState.emit(Result.Loading())
        if (areFieldsEmpty(email = email, password = password)) {
            _tutorLoginState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Login(email.trim().lowercase(), password.trim())
        _tutorLoginState.emit(repository.login(tutor))
        currentTutor = repository.getCurrentUser()
    }

    fun update(
        email: String,
        password: String,
        name: String,
        modules: List<String>,
        profilePic: ByteArray?
    ) = viewModelScope.launch {
        _tutorUpdateState.emit(Result.Loading())
        if (areFieldsEmpty(email.trim(), name = name.trim(), modules = modules)) {
            _tutorUpdateState.emit(Result.Error("Some fields might be empty"))
            return@launch
        }
        val tutor = Update(
            email.trim().lowercase(),
            if (password.isEmpty())
                null
            else
                password.trim(),
            name.doNamesOperations(),
            modules,
            profilePic
        )
        _tutorUpdateState.emit(repository.update(tutor))
        currentTutor = repository.getCurrentUser()
    }

    private fun validateUser() = viewModelScope.launch {
        val token = repository.validateUser()
        if (token == null)
            _shouldNavigateToRegister.emit(true)
        else
            _shouldNavigateToRegister.emit(false)
    }

    suspend fun getStudentsCount(): Int {
        return repository.getAllStudentsAsList().size
    }

    fun updateThemePreferences(themeInt: Int) = viewModelScope.launch {
        repository.updateTheme(themeInt)
    }

    fun logout() = viewModelScope.launch {
        _tutorLogoutState.emit(Result.Loading())
        _tutorLogoutState.emit(repository.logout())
    }
}
