package com.example.tutortracking.data.common.models

import com.example.tutortracking.data.remotedata.models.Student
import com.example.tutortracking.data.remotedata.models.Tutor

data class UserResponse(
    val success: Boolean,
    val tutorInfo: Tutor?=null,
    val token: String?=null,
    val message: String?=null,
    val studentsList: List<Student>?=null
)
