package com.example.tutortracking.data.remotedata.models

import com.example.tutortracking.data.common.models.UserResponse
import com.example.tutortracking.util.Constants.CREATE_STUDENTS
import com.example.tutortracking.util.Constants.DELETE_STUDENTS
import com.example.tutortracking.util.Constants.LOGIN_TUTOR
import com.example.tutortracking.util.Constants.READ_STUDENTS
import com.example.tutortracking.util.Constants.REGISTER_TUTOR
import com.example.tutortracking.util.Constants.UPDATE_STUDENTS
import com.example.tutortracking.util.Constants.UPDATE_TUTOR
import retrofit2.http.*

interface TutorApi {

    //Tutor
    @Headers("Content-Type: application/json")
    @POST(REGISTER_TUTOR)
    fun registerTutor(
        @Body tutor : Register
    ) : UserResponse

    @Headers("Content-Type: application/json")
    @POST(LOGIN_TUTOR)
    fun loginTutor(
        @Body tutor : Login
    ) : UserResponse

    @Headers("Content-Type: application/json")
    @POST(UPDATE_TUTOR)
    fun updateTutor(
        @Body tutor : Update,
        @Header("Authorization") token: String
    ) : UserResponse

    //Students
    @Headers("Content-Type: application/json")
    @POST(CREATE_STUDENTS)
    fun createStudent(
        @Body student: Student,
        @Header("Authorization") token: String
    ) : UserResponse

    @Headers("Content-Type: application/json")
    @GET(READ_STUDENTS)
    fun readStudents(
        @Header("Authorization") token: String
    ) : UserResponse

    @Headers("Content-Type: application/json")
    @PUT(UPDATE_STUDENTS)
    fun updateStudent(
        @Path("id") id: String,
        @Body student: Student,
        @Header("Authorization") token: String
    ) : UserResponse

    @Headers("Content-Type: application/json")
    @DELETE(DELETE_STUDENTS)
    fun deleteStudent(
        @Query("id") id : String,
        @Header("Authorization") token: String
    ) : UserResponse

}