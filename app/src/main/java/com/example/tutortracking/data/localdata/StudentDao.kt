package com.example.tutortracking.data.localdata

import androidx.room.*
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.Tutor
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: LocalStudent)

    @Delete
    suspend fun deleteStudent(student: LocalStudent)

    @Query("SELECT * FROM studentTable")
    fun getAllStudents() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentName")
    fun getAllStudentsOrderedByName() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentYear")
    fun getAllStudentsOrderedByYear() : Flow<List<LocalStudent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutor(tutor: Tutor)

    @Query("DELETE FROM tutor_table")
    suspend fun deleteTutor()

    @Query("SELECT * FROM tutor_table")
    fun getTutor() : Flow<List<Tutor>>

}
