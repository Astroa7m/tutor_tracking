package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.util.Result
import kotlinx.coroutines.flow.Flow

class TutorRepositoryImpl : TutorRepository {
    override suspend fun register(): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun login(): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun update(): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun addStudent(student: LocalStudent): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override fun getAllStudents(): Flow<List<LocalStudent>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateStudent(student: LocalStudent): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStudent(student: LocalStudent): Result<UserResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllStudentsFromServer(): Result<UserResponse> {
        TODO("Not yet implemented")
    }
}