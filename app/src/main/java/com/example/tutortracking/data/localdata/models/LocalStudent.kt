package com.example.tutortracking.data.localdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studentTable")
data class LocalStudent(
    val studentName: String?=null,
    val studentYear: Int?=null,
    val studentSubject: String?=null,
    val studentTutorId: String?=null,
    val studentPic: ByteArray?=null,
    var isConnected: Boolean,
    @PrimaryKey(autoGenerate = true)
    val _id: Int?=null
)
