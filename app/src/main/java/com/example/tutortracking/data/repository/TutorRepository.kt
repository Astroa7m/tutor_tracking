package com.example.tutortracking.data.repository

import androidx.room.Query
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
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
    fun getAllStudentsLocally() : Flow<List<LocalStudent>>
    suspend fun updateStudent(student: LocalStudent, id: String) : Result<UserResponse>
    suspend fun deleteStudent(student: LocalStudent) : Result<UserResponse>
    suspend fun logout() : Result<String>
    suspend fun getAllStudentsFromServer() : Result<UserResponse>
    suspend fun validateUser() : String?
    fun getCurrentUser() : Flow<List<Tutor>>
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
    fun searchStudent(query: String) : Flow<List<LocalStudent>>
    suspend fun sync()

}