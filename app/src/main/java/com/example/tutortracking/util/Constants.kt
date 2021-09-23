package com.example.tutortracking.util

object Constants {
    const val BASE_URL = "https://salty-hollows-72744.herokuapp.com"
    private const val API_VERSION = "/v1"
    const val REGISTER_TUTOR = "$API_VERSION/users/register"
    const val LOGIN_TUTOR = "$API_VERSION/users/login"
    const val UPDATE_TUTOR = "$API_VERSION/users/update"
    const val CREATE_STUDENTS = "$API_VERSION/students/create"
    const val READ_STUDENTS = "$API_VERSION/students/read"
    const val UPDATE_STUDENTS = "$API_VERSION/students/update/{id}"
    const val DELETE_STUDENTS = "$API_VERSION/students/delete"
    const val TUTOR_ID_KEY = "TUTOR_ID_KEY"
    const val TOKEN_KEY = "TOKEN"
    const val SESSION_KEY = "SESSION_KEY"
}