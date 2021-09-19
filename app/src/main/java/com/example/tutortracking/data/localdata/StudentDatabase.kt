package com.example.tutortracking.data.localdata

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.util.Converters

@Database(entities = [LocalStudent::class, Tutor::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StudentDatabase : RoomDatabase()  {
    abstract fun getStudentDao() : StudentDao
}