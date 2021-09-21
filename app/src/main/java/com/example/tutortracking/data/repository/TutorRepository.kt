package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.Login
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.data.remotedata.models.Update
import kotlinx.coroutines.flow.Flow
import com.example.tutortracking.util.Result

interface TutorRepository {
    suspend fun register(tutor: Register) : Result<UserResponse>
    suspend fun login(tutor: Login) : Result<UserResponse>
    suspend fun update(update: Update) : Result<UserResponse>
    suspend fun addStudent(student: LocalStudent) : Result<UserResponse>
    fun getAllStudents() : Flow<List<LocalStudent>>
    suspend fun updateStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun deleteStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun logout() : Result<String>
    suspend fun getAllStudentsFromServer() : Result<UserResponse>
    suspend fun validateUser() : String?
    fun getCurrentUser() : Flow<List<Tutor>>
}