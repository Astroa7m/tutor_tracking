package com.example.tutortracking.data.remotedata.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutor_table")
data class Tutor(
    val email: String,
    val hashedPassword: String,
    val name: String,
    val modules: List<String>,
    val profilePic: ByteArray?=null,
    @PrimaryKey(autoGenerate = false)
    val _id: String
)
