package com.example.tutortracking.util

private fun areFieldsEmpty(
    email: String,
    password: String,
    name:String?=null
) = name?.isEmpty()?:true && email.isEmpty() && password.isEmpty()