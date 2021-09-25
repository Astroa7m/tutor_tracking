package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.StudentDao
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.TutorApi
import com.example.tutortracking.data.remotedata.models.*
import com.example.tutortracking.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TutorRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val tutorApi: TutorApi,
    private val sessionManager: SessionManager
) : TutorRepository {
    override suspend fun register(tutor: Register): Result<UserResponse> {
        return if (!hasInternetConnection(sessionManager.context)) {
            Result.Error(message = "No Internet Connection")
        } else {
            try {
                val result: UserResponse
                withContext(Dispatchers.IO) {
                    result = tutorApi.registerTutor(tutor)
                }
                if (result.success) {
                    sessionManager.updateSession(result.token!!, result.tutorInfo!!._id)
                    studentDao.upsertTutor(result.tutorInfo)
                    Result.Success(result, "Registered Successfully")
                } else {
                    Result.Error(result.message.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.Error(e.message ?: "Error occurred while registering")
            }
        }
    }

    override suspend fun login(tutor: Login): Result<UserResponse> {
        return if (!hasInternetConnection(sessionManager.context)) {
            Result.Error(message = "No Internet Connection")
        } else {
            return try {
                val result = tutorApi.loginTutor(tutor)
                if (result.success) {
                    sessionManager.updateSession(result.token!!, result.tutorInfo!!._id)
                    studentDao.upsertTutor(result.tutorInfo)
                    Result.Success(result, "Logged In Successfully")
                } else {
                    Result.Error(result.message.toString())
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error occurred while logging in")
            }
        }
    }

    override suspend fun update(update: Update): Result<UserResponse> {
        return if (!hasInternetConnection(sessionManager.context)) {
            Result.Error(message = "No Internet Connection")
        } else {
            return try {
                val token = sessionManager.getTutorToken() ?: return Result.Error("No Token")
                val result = tutorApi.updateTutor(update, "Bearer $token")
                if (result.success) {
                    studentDao.upsertTutor(result.tutorInfo!!)
                    Result.Success(result)
                } else {
                    Result.Error(result.message.toString())
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Error occurred while updating")
            }
        }
    }

    override suspend fun addStudent(student: LocalStudent): Result<UserResponse> {
        try{
            val tutorId = sessionManager.getTutorId()
            val tutorToken = sessionManager.getTutorToken()
            student.studentTutorId = tutorId
            studentDao.upsertStudent(student)
            return if (!hasInternetConnection(sessionManager.context)) {
                addLocallyAddedStudent(getLocallyAddedFromStudent(student))
                Result.Success(UserResponse(true, message = "Inserted Student locally"))
            }else{
                val remoteStudent = Student(student.studentName, student.studentYear, student.studentSubject,tutorId, student.studentPic, student._id)
                val result = tutorApi.createStudent(remoteStudent, "Bearer $tutorToken")
                if(result.success){
                    student.isConnected = true
                    studentDao.upsertStudent(student)
                    deleteLocallyAddedStudent(getLocallyAddedFromStudent(student))
                    Result.Success(result)
                }
                else
                    Result.Error(result.message!!)
            }
        }catch (e: Exception){
            return Result.Error(e.message?: "Error occurred")
        }
    }

    override fun getAllStudentsLocally(
        query: String,
        sortOrder: SortOrder
    ): Flow<List<LocalStudent>> {
        return try{
            studentDao.getStudents(query, sortOrder)
        }catch (e: Exception){
            emptyFlow()
        }
    }

    override suspend fun updateStudent(student: LocalStudent, id: String): Result<UserResponse> {
       return try{
            studentDao.upsertStudent(student)
            val token = sessionManager.getTutorToken()
            return if (!hasInternetConnection(sessionManager.context)) {
                addLocallyUpdatedStudent(getLocallyUpdatedFromStudent(student))
                Result.Success(UserResponse(true, message = "Updated Student locally"))
            }else{
                student.isConnected = true
                studentDao.upsertStudent(student)
                val remoteStudent = Student(student.studentName, student.studentYear, student.studentSubject, studentPic = student.studentPic)
                val result = tutorApi.updateStudent(id, remoteStudent, "Bearer $token")
                if (result.success){
                    deleteLocallyUpdatedStudent(getLocallyUpdatedFromStudent(student))
                    Result.Success(result)
                }
                else
                    Result.Error(result.message.toString())
            }
        }catch (e: Exception){
            Result.Error(e.message?:"Error occurred")
        }
    }

    override suspend fun deleteStudent(student: LocalStudent): Result<UserResponse> {
        return try{
            val token = sessionManager.getTutorToken()
            studentDao.deleteStudent(student)
            return if (!hasInternetConnection(sessionManager.context)) {
                addLocallyDeletedStudent(getLocallyDeletedFromStudent(student))
                Result.Success(UserResponse(true, message = "Updated Student locally"))
            }else {
                val result = tutorApi.deleteStudent(student._id, "Bearer $token")
                if (result.success){
                    deleteLocallyDeletedStudent(getLocallyDeletedFromStudent(student))
                    Result.Success(result)
                }
                else
                    Result.Error(result.message.toString())
            }
        }catch (e: Exception){
            Result.Error(e.message?: "Error occurred on deleting student")
        }
    }

    override suspend fun logout(): Result<String> {
        return if (!hasInternetConnection(sessionManager.context)) {
            Result.Error("No Internet Connection")
        }else{
            return try{
                sessionManager.logout()
                studentDao.deleteAllLocalStudents()
                studentDao.deleteTutor()
                studentDao.deleteRecordsFromLocallyAddedStudent()
                studentDao.deleteRecordsFromLocallyUpdatedStudent()
                studentDao.deleteRecordsFromLocallyDeletedStudent()
                Result.Success("Successfully logged out")
            } catch (e: Exception){
                Result.Error("Could not log out")
            }
        }
    }

    override suspend fun getAllStudentsFromServer(): Result<UserResponse> {
        return try{
            val token = sessionManager.getTutorToken()
            val result = tutorApi.readStudents("Bearer $token")
            if(result.success)
                Result.Success(result)
            else
                Result.Error(result.message.toString())
        }catch (e : Exception){
            Result.Error(e.message?: "Error while getting students")
        }
    }

    override suspend fun validateUser() = sessionManager.getTutorToken()

    override fun getCurrentUser(): Flow<List<Tutor>> = studentDao.getTutor()

    override suspend fun addLocallyUpdatedStudent(student: LocallyUpdatedStudent) {
        studentDao.insertLocallyUpdatedStudent(student)
    }

    override suspend fun addLocallyDeletedStudent(student: LocallyDeletedStudent) {
        studentDao.insertLocallyDeletedStudent(student)
    }

    override suspend fun addLocallyAddedStudent(student: LocallyAddedStudent) {
        studentDao.insertLocallyAddedStudent(student)
    }

    override suspend fun deleteLocallyUpdatedStudent(student: LocallyUpdatedStudent) {
        studentDao.deleteLocallyUpdatedStudent(student)
    }

    override suspend fun deleteLocallyDeletedStudent(student: LocallyDeletedStudent) {
        studentDao.deleteLocallyDeletedStudent(student)
    }

    override suspend fun deleteLocallyAddedStudent(student: LocallyAddedStudent) {
        studentDao.deleteLocallyAddedStudent(student)
    }

    override suspend fun deleteRecordsFromLocallyAddedStudent() {
        studentDao.deleteRecordsFromLocallyAddedStudent()
    }

    override suspend fun deleteRecordsFromLocallyUpdatedStudent() {
        studentDao.deleteRecordsFromLocallyUpdatedStudent()
    }

    override suspend fun deleteRecordsFromLocallyDeletedStudent() {
        studentDao.deleteRecordsFromLocallyDeletedStudent()
    }

    override suspend fun getAllLocallyDelete() = studentDao.getAllLocallyDeleted()

    override suspend fun getAllLocallyUpdated() = studentDao.getAllLocallyUpdated()

    override suspend fun getAllLocallyAdded() = studentDao.getAllLocallyAdded()

    override suspend fun sync() {
        try {
            val token = sessionManager.getTutorToken()
            val localDeleted = getAllLocallyDelete()
            localDeleted.forEach {
                tutorApi.deleteStudent(it._id, "Bearer $token")
            }
            val localAdded = getAllLocallyAdded()
            localAdded.forEach {
                tutorApi.createStudent(getStudentFromLocallyAdded(it), "Bearer $token")
            }
            val localUpdate = getAllLocallyUpdated()
            localUpdate.forEach {
                tutorApi.updateStudent(
                    getStudentFromLocallyUpdated(it)._id.toString(),
                    getStudentFromLocallyUpdated(it),
                    "Bearer $token"
                )
            }
            val data = getAllStudentsFromServer()
            if(data.data!!.studentsList.isNullOrEmpty())
                studentDao.deleteAllLocalStudents()
            val localStudents  = studentDao.getAllStudentsForAsList()
            val convertedLocalToRemote = mutableListOf<Student>()
            localStudents.forEach {
                convertedLocalToRemote.add(Student(it.studentName, it.studentYear, it.studentSubject,it.studentTutorId,
                it.studentPic, it._id))
            }
            data.data.studentsList?.let { remoteStudentsList ->
                remoteStudentsList.forEach { remoteStudent ->
                    val localStudent = LocalStudent(
                        remoteStudent.studentName,
                        remoteStudent.studentYear,
                        remoteStudent.studentSubject,
                        remoteStudent.studentTutorId,
                        remoteStudent.studentPic,
                        isConnected = true,
                        remoteStudent._id.toString()
                    )
                     if(!convertedLocalToRemote.contains(remoteStudent)){
                         studentDao.deleteStudentById(remoteStudent._id.toString())
                     }
                    studentDao.upsertStudent(localStudent)
                }
            }
            deleteRecordsFromLocallyAddedStudent()
            deleteRecordsFromLocallyUpdatedStudent()
            deleteRecordsFromLocallyDeletedStudent()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override suspend fun getAllStudentsAsList() = studentDao.getAllStudentsForAsList()
}