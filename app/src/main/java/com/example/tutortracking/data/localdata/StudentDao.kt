package com.example.tutortracking.data.localdata

import androidx.room.*
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.Tutor
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: LocalStudent)

    @Delete(entity = LocalStudent::class)
    suspend fun deleteStudent(student: LocalStudent)

    @Query("DELETE FROM studentTable WHERE _id = :id")
    suspend fun deleteStudentById(id: String)
    @Query("DELETE FROM studentTable")
    suspend fun deleteAllLocalStudents()

    @Query("SELECT * FROM studentTable")
    fun getAllStudents() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentName")
    fun getAllStudentsOrderedByName() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentYear")
    fun getAllStudentsOrderedByYear() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable")
    suspend fun getAllStudentsForChecking() : List<LocalStudent>

    @Query("SELECT * FROM studentTable WHERE studentSubject LIKE :query OR studentYear LIKE :query OR studentName LIKE :query")
    fun searchStudents(query: String) : Flow<List<LocalStudent>>

    @Insert(entity = Tutor::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutor(tutor: Tutor)

    @Query("DELETE FROM tutor_table")
    suspend fun deleteTutor()

    @Query("DELETE FROM studentTable")
    suspend fun dropStudents()

    @Query("SELECT * FROM tutor_table")
    fun getTutor() : Flow<List<Tutor>>

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
