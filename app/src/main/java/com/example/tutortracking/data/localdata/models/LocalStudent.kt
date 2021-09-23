package com.example.tutortracking.data.localdata.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "studentTable")
data class LocalStudent(
    val studentName: String?=null,
    val studentYear: Int?=null,
    val studentSubject: String?=null,
    var studentTutorId: String?=null,
    val studentPic: ByteArray?=null,
    var isConnected: Boolean = false,
    @PrimaryKey(autoGenerate = false)
    val _id: String
) : Serializable
