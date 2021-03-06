package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.*
import com.example.tutortracking.data.remotedata.models.Login
import com.example.tutortracking.data.remotedata.models.Register
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.data.remotedata.models.Update
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SortOrder
import kotlinx.coroutines.flow.Flow

interface TutorRepository {
    suspend fun register(tutor: Register) : Result<UserResponse>
    suspend fun login(tutor: Login) : Result<UserResponse>
    suspend fun update(update: Update) : Result<UserResponse>
    suspend fun addStudent(student: LocalStudent) : Result<UserResponse>
    fun getAllStudentsLocally(query: String, sortOrder: SortOrder) : Flow<List<LocalStudent>>
    suspend fun updateStudent(student: LocalStudent, id: String) : Result<UserResponse>
    suspend fun deleteStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun logout() : Result<String>
    suspend fun getAllStudentsFromServer() : Result<UserResponse>
    suspend fun validateUser() : String?
    suspend fun getCurrentUser() : Tutor
    suspend fun addLocallyUpdatedStudent(student: LocallyUpdatedStudent)
    suspend fun addLocallyDeletedStudent(student: LocallyDeletedStudent)
    suspend fun addLocallyAddedStudent(student: LocallyAddedStudent)
    suspend fun deleteLocallyUpdatedStudent(student: LocallyUpdatedStudent)
    suspend fun deleteLocallyDeletedStudent(student: LocallyDeletedStudent)
    suspend fun deleteLocallyAddedStudent(student: LocallyAddedStudent)
    suspend fun deleteRecordsFromLocallyAddedStudent()
    suspend fun deleteRecordsFromLocallyUpdatedStudent()
    suspend fun deleteRecordsFromLocallyDeletedStudent()
    suspend fun getAllLocallyDelete() : List<LocallyDeletedStudent>
    suspend fun getAllLocallyUpdated() : List<LocallyUpdatedStudent>
    suspend fun getAllLocallyAdded() : List<LocallyAddedStudent>
    suspend fun getAllStudentsAsList() : List<LocalStudent>
    suspend fun getTutorModules() : String
    suspend fun updateTheme(themeInt: Int)
    suspend fun getAllMessages(): List<Message>
    suspend fun openSession(): Result<Unit>
    suspend fun sendMessage(message: String)
    suspend fun observeMessage() : Flow<Message>
    suspend fun disconnect()
    suspend fun sync()

}