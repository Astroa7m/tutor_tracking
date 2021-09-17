package com.example.tutortracking.data.remotedata.models

data class Tutor(
    val email: String,
    val hashedPassword: String,
    val name: String,
    val modules: List<String>,
    val profilePic: ByteArray?=null,
    val _id: String?=null
)
