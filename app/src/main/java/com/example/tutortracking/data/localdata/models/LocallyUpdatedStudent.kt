package com.example.tutortracking.data.localdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "locally_update_student")
data class LocallyUpdatedStudent(
    val studentName: String?=null,
    val studentYear: Int?=null,
    val studentSubject: String?=null,
    var studentTutorId: String?=null,
    val studentPic: ByteArray?=null,
    @PrimaryKey(autoGenerate = false)
    val _id: String
)
