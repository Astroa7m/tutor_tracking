package com.example.tutortracking.data.localdata

import androidx.room.*
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    // Students CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: LocalStudent)

    @Delete(entity = LocalStudent::class)
    suspend fun deleteStudent(student: LocalStudent)

    @Query("DELETE FROM studentTable WHERE _id = :id")
    suspend fun deleteStudentById(id: String)

    @Query("DELETE FROM studentTable")
    suspend fun deleteAllLocalStudents()

    // read students and search functionality
    fun getStudents(searchQuery: String, sortOrder: SortOrder) : Flow<List<LocalStudent>> =
        when(sortOrder){
            SortOrder.BY_NAME -> getStudentsSortedByName(searchQuery)
            SortOrder.BY_SUBJECT-> getStudentsSortedBySubject(searchQuery)
            SortOrder.BY_YEAR-> getStudentsSortedByYear(searchQuery)
        }

    @Query("SELECT * FROM studentTable")
    suspend fun getAllStudentsForAsList() : List<LocalStudent>

    @Query("SELECT * FROM studentTable WHERE studentSubject LIKE '%' || :query || '%' OR studentYear LIKE '%' || :query || '%' OR studentName LIKE '%' || :query || '%' ORDER BY studentName")
    fun getStudentsSortedByName(query: String) : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable WHERE studentSubject LIKE '%' || :query || '%' OR studentYear LIKE '%' || :query || '%' OR studentName LIKE '%' || :query || '%' ORDER BY studentYear")
    fun getStudentsSortedByYear(query: String) : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable WHERE studentSubject LIKE '%' || :query || '%' OR studentYear LIKE '%' || :query || '%' OR studentName LIKE '%' || :query || '%' ORDER BY studentSubject")
    fun getStudentsSortedBySubject(query: String) : Flow<List<LocalStudent>>

    // Tutor CRUD operations

    @Insert(entity = Tutor::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutor(tutor: Tutor)

    @Query("DELETE FROM tutor_table")
    suspend fun deleteTutor()

    @Query("SELECT * FROM tutor_table Limit 1")
    suspend fun getTutor() : Tutor

    @Query("SELECT modules FROM tutor_table LIMIT 1")
    suspend fun getTutorModules(): String

    //Local CRUD operation

    //locally updated student
    @Insert(entity = LocallyUpdatedStudent::class)
    suspend fun insertLocallyUpdatedStudent(student: LocallyUpdatedStudent)

    @Delete(entity = LocallyUpdatedStudent::class)
    suspend fun deleteLocallyUpdatedStudent(student: LocallyUpdatedStudent)

    @Query("SELECT * FROM locally_update_student")
    suspend fun getAllLocallyUpdated() : List<LocallyUpdatedStudent>

    //locally deleted student
    @Insert(entity = LocallyDeletedStudent::class)
    suspend fun insertLocallyDeletedStudent(student: LocallyDeletedStudent)

    @Delete(entity = LocallyDeletedStudent::class)
    suspend fun deleteLocallyDeletedStudent(student: LocallyDeletedStudent)

    @Query("SELECT * FROM locally_deleted_student")
    suspend fun getAllLocallyDeleted() : List<LocallyDeletedStudent>

    //locally added student
    @Insert(entity = LocallyAddedStudent::class)
    suspend fun insertLocallyAddedStudent(student: LocallyAddedStudent)

    @Delete(entity = LocallyAddedStudent::class)
    suspend fun deleteLocallyAddedStudent(student: LocallyAddedStudent)

    @Query("SELECT * FROM locally_added_student")
    suspend fun getAllLocallyAdded() : List<LocallyAddedStudent>

    // deleting records from tables when tutor logs out

    @Query("DELETE FROM locally_added_student")
    suspend fun deleteRecordsFromLocallyAddedStudent()

    @Query("DELETE FROM locally_update_student")
    suspend fun deleteRecordsFromLocallyUpdatedStudent()

    @Query("DELETE FROM locally_deleted_student")
    suspend fun deleteRecordsFromLocallyDeletedStudent()

}
