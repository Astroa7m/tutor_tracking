package com.example.tutortracking.data.repository

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.*
import com.example.tutortracking.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TutorRepositoryFakeAndroid(internetConnection: Boolean = true, tutorToken: String = "token")  : TutorRepository {

    private var hasInternetConnection = internetConnection

    private val tutorTable = mutableListOf<Tutor>()
    private val remoteStudentsList = mutableListOf<Student>()
    private val localStudentsTable = mutableListOf<LocalStudent>()

    private val locallyAddedStudentsTable = mutableListOf<LocallyAddedStudent>()
    private val locallyUpdatedStudentsTable = mutableListOf<LocallyUpdatedStudent>()
    private val locallyDeletedStudentsTable = mutableListOf<LocallyDeletedStudent>()

    private var theme = Pair(0, "DEFAULT")

    private var token = tutorToken

    override suspend fun register(tutor: Register): Result<UserResponse> {
        return if(!hasInternetConnection)
            Result.Error("No Internet Connection", UserResponse(false))
        else{
            val registeredTutor = Tutor(tutor.email!!, tutor.password.hashCode().toString(), tutor.name!!, tutor.modules!!, tutor.profilePic, tutor._id ?: "id")
            token = "justToken"
            if(tutorTable.isEmpty()) tutorTable.add(registeredTutor)
            val result = UserResponse(true, registeredTutor, token)
            Result.Success(result)
        }
    }

    override suspend fun login(tutor: Login): Result<UserResponse> {
        return if(!hasInternetConnection)
            Result.Error("No Internet Connection", UserResponse(false))
        else{
            val loggedInTutor = Tutor(tutor.email, tutor.password.hashCode().toString(), "cool tutor", listOf(), null, "id")
            token = "justToken"
            if(tutorTable.isEmpty()) tutorTable.add(loggedInTutor)
            val result = UserResponse(true, loggedInTutor, token, studentsList = remoteStudentsList.toList())
            Result.Success(result)
        }
    }

    override suspend fun update(update: Update): Result<UserResponse> {
        return if (!hasInternetConnection)
            Result.Error("No Internet Connection", UserResponse(false))
        else {
            return if(token.isNotEmpty()){
                val updatedTutor = Tutor(update.email!!, update.password.hashCode().toString(), update.name!!, update.modules!!, update.profilePic, update._id ?: "id")
                tutorTable.clear()
                tutorTable.add(updatedTutor)
                val result = UserResponse(true, updatedTutor)
                Result.Success(result)
            }else{
                Result.Error("error occurred", UserResponse(false))
            }
        }
    }

    override suspend fun addStudent(student: LocalStudent): Result<UserResponse> {
        return if(token.isNotEmpty()){
            localStudentsTable.add(student)
            return if(!hasInternetConnection){
                addLocallyAddedStudent(getLocallyAddedFromStudent(student))
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "inserted locally"), "inserted locally")
            }else{
                val remoteStudent = Student(student.studentName, student.studentYear, student.studentSubject, student.studentTutorId, student.studentPic, student._id)
                remoteStudentsList.add(remoteStudent)
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "inserted successfully", remoteStudentsList), message = "inserted successfully")
            }
        }else{
            Result.Error("no token",UserResponse(false))
        }
    }

    override fun getAllStudentsLocally(
        query: String,
        sortOrder: SortOrder
    ): Flow<List<LocalStudent>> {
        return flowOf(localStudentsTable.toList())
    }

    override suspend fun updateStudent(student: LocalStudent, id: String): Result<UserResponse> {
        return if(token.isNotEmpty()){
            localStudentsTable.removeIf { s: LocalStudent -> s.studentName.equals(student.studentName) }
            localStudentsTable.add(student)
            return if(!hasInternetConnection){
                addLocallyUpdatedStudent(getLocallyUpdatedFromStudent(student))
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "updated locally"))
            }else{
                remoteStudentsList.removeIf { s: Student -> s.studentName.equals(student.studentName) }
                remoteStudentsList.add(Student(student.studentName, student.studentYear, student.studentSubject, student.studentTutorId, student.studentPic, student._id))
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "updated successfully", remoteStudentsList))
            }
        }else{
            Result.Error("no token",UserResponse(false))
        }
    }

    override suspend fun deleteStudent(student: LocalStudent): Result<UserResponse> {
        return if(token.isNotEmpty()){
            localStudentsTable.removeIf { s: LocalStudent -> s.studentName.equals(student.studentName) }
            return if(!hasInternetConnection){
                addLocallyDeletedStudent(getLocallyDeletedFromStudent(student))
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "deleted locally"))
            }else{
                remoteStudentsList.removeIf { s: Student -> s.studentName.equals(student.studentName) }
                Result.Success(UserResponse(true, if(tutorTable.isEmpty())null else tutorTable[0], token, "deleted successfully", remoteStudentsList))
            }
        }else{
            Result.Error("no token",UserResponse(false))
        }
    }

    override suspend fun logout(): Result<String> {
        return if(hasInternetConnection){
            token = ""
            tutorTable.clear()
            Result.Success("logged out", "logged out")
        }else{
            Result.Error("No internet connection")
        }
    }

    override suspend fun getAllStudentsFromServer(): Result<UserResponse> {
        return if(token.isNotEmpty()) {
            Result.Success(UserResponse(true, studentsList = remoteStudentsList))
        }else{
            Result.Error("no token",UserResponse(false))
        }
    }

    override suspend fun validateUser(): String? {
        return if(token.isNotEmpty()) token else null
    }

    override fun getCurrentUser(): Flow<List<Tutor>> {
        return flowOf(tutorTable)
    }

    override suspend fun addLocallyUpdatedStudent(student: LocallyUpdatedStudent) {
        locallyUpdatedStudentsTable.add(student)
    }

    override suspend fun addLocallyDeletedStudent(student: LocallyDeletedStudent) {
        locallyDeletedStudentsTable.add(student)
    }

    override suspend fun addLocallyAddedStudent(student: LocallyAddedStudent) {
        locallyAddedStudentsTable.add(student)

    }

    override suspend fun deleteLocallyUpdatedStudent(student: LocallyUpdatedStudent) {
        locallyUpdatedStudentsTable.removeIf { LocallyUpdatedStudent::_id.equals(student._id) }
    }

    override suspend fun deleteLocallyDeletedStudent(student: LocallyDeletedStudent) {
        locallyDeletedStudentsTable.removeIf { LocallyDeletedStudent::_id.equals(student._id) }

    }

    override suspend fun deleteLocallyAddedStudent(student: LocallyAddedStudent) {
        locallyAddedStudentsTable.removeIf { LocallyAddedStudent::_id.equals(student._id) }

    }

    override suspend fun deleteRecordsFromLocallyAddedStudent() {
        locallyAddedStudentsTable.clear()
    }

    override suspend fun deleteRecordsFromLocallyUpdatedStudent() {
        locallyUpdatedStudentsTable.clear()
    }

    override suspend fun deleteRecordsFromLocallyDeletedStudent() {
        locallyDeletedStudentsTable.clear()

    }

    override suspend fun getAllLocallyDelete(): List<LocallyDeletedStudent> {
        return locallyDeletedStudentsTable
    }

    override suspend fun getAllLocallyUpdated(): List<LocallyUpdatedStudent> {
        return locallyUpdatedStudentsTable
    }

    override suspend fun getAllLocallyAdded(): List<LocallyAddedStudent> {
        return locallyAddedStudentsTable
    }

    override suspend fun getAllStudentsAsList(): List<LocalStudent> {
        return localStudentsTable
    }

    override suspend fun getTutorModules(): String {
        return tutorTable[0].modules.joinToString(separator = ",")
    }

    override suspend fun updateTheme(themeInt: Int) {
        theme = when(themeInt){
            1 -> Pair(themeInt, "LIGHT")
            2 -> Pair(themeInt, "DARK")
            else -> Pair(0, "DEFAULT")
        }
    }

    override suspend fun sync() {
        getAllLocallyDelete().forEach { locallyDeletedStudents ->
            remoteStudentsList.removeIf { s: Student -> s._id.equals(locallyDeletedStudents._id) }
        }

        getAllLocallyUpdated().forEach { locallyUpdatedStudent ->
            remoteStudentsList.removeIf { s: Student -> s._id.equals(locallyUpdatedStudent._id) }
            remoteStudentsList.add(getStudentFromLocallyUpdated(locallyUpdatedStudent))
        }
        getAllLocallyAdded().forEach { locallyAddedStudents ->
            remoteStudentsList.add(getStudentFromLocallyAdded(locallyAddedStudents))
        }

        if (remoteStudentsList.isNullOrEmpty())
            localStudentsTable.clear()

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
            if (!remoteStudentsList.contains(remoteStudent)) {
                localStudentsTable.removeIf { s: LocalStudent -> s._id == localStudent._id }
            }
            localStudentsTable.removeIf { s: LocalStudent -> s._id == localStudent._id }
            localStudentsTable.add(localStudent)
        }

        deleteRecordsFromLocallyAddedStudent()
        deleteRecordsFromLocallyUpdatedStudent()
        deleteRecordsFromLocallyDeletedStudent()
    }

}