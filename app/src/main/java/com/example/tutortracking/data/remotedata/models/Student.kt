package com.example.tutortracking.data.remotedata.models

data class Student(
    val studentName: String?=null,
    val studentYear: Int?=null,
    val studentSubject: String?=null,
    val studentTutorId: String?=null,
    val studentPic: ByteArray?=null,
    val _id: String?=null
)
