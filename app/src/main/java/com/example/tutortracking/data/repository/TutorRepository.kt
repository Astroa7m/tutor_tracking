package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import kotlinx.coroutines.flow.Flow
import com.example.tutortracking.util.Result

interface TutorRepository {
    suspend fun register() : Result<UserResponse>
    suspend fun login() : Result<UserResponse>
    suspend fun update() : Result<UserResponse>
    suspend fun addStudent(student: LocalStudent) : Result<UserResponse>
    fun getAllStudents() : Flow<List<LocalStudent>>
    suspend fun updateStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun deleteStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun logout() : Result<String>
    suspend fun getAllStudentsFromServer() : Result<UserResponse>
}