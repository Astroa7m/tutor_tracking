package com.example.tutortracking.data.repository

import android.se.omapi.Session
import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.StudentDao
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.*
import com.example.tutortracking.util.Result
import com.example.tutortracking.util.SessionManager
import com.example.tutortracking.util.hasInternetConnection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.contracts.Returns

class TutorRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val tutorApi: TutorApi,
    private val sessionManager: SessionManager
) : TutorRepository {
    override suspend fun register(tutor: Register): Result<UserResponse> {
        return if(!hasInternetConnection(sessionManager.context)){
            Result.Error(message = "No Internet Connection")
        }else{
            try{
                val result = tutorApi.registerTutor(tutor)
                if(result.success){
                    sessionManager.updateSession(result.token!!, result.tutorInfo!!.name, result.tutorInfo.email)
                    Result.Success(result)
                }else{
                    Result.Error(result.message.toString())
                }
            }catch (e: Exception){
                e.printStackTrace()
                Result.Error(e.message?:"Error occurred while registering")
            }
        }
    }

    override suspend fun login(tutor: Login): Result<UserResponse> {
        return if(!hasInternetConnection(sessionManager.context)){
            Result.Error(message = "No Internet Connection")
        }else{
            return try{
                val result = tutorApi.loginTutor(tutor)
                if(result.success){
                    sessionManager.updateSession(result.token!!, result.tutorInfo!!.name, result.tutorInfo.email)
                    Result.Success(result)
                }else{
                    Result.Error(result.message.toString())
                }
            }catch (e: Exception){
                Result.Error(e.message?:"Error occurred while logging in")
            }
        }
    }

    override suspend fun update(update: Update): Result<UserResponse> {
        return if(!hasInternetConnection(sessionManager.context)) {
            Result.Error(message = "No Internet Connection")
        }else{
           return try{
                val token = sessionManager.getTutorToken()?: return Result.Error("No Token")
                val result = tutorApi.updateTutor(update,"Bearer $token")
                if(result.success){
                    Result.Success(result)
                }else{
                    Result.Error(result.message.toString())
                }
            }catch (e: Exception){
                Result.Error(e.message?:"Error occurred while updating")
            }
        }
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

    override suspend fun validateUser() = sessionManager.getTutorToken()

}