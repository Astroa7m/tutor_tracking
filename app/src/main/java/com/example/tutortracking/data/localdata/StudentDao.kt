package com.example.tutortracking.data.localdata

import androidx.room.*
import com.example.tutortracking.data.localdata.models.LocalStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertStudent(student: LocalStudent)

    @Delete
    fun deleteStudent(student: LocalStudent)

    @Query("SELECT * FROM studentTable")
    fun getAllStudents() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentName")
    fun getAllStudentsOrderedByName() : Flow<List<LocalStudent>>

    @Query("SELECT * FROM studentTable ORDER BY studentYear")
    fun getAllStudentsOrderedByYear() : Flow<List<LocalStudent>>
}
