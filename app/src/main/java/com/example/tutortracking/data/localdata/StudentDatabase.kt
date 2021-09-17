package com.example.tutortracking.data.localdata

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tutortracking.data.localdata.models.LocalStudent

@Database(entities = [LocalStudent::class], version = 1, exportSchema = false)
abstract class StudentDatabase : RoomDatabase()  {
    abstract fun getStudentDao() : StudentDao
}