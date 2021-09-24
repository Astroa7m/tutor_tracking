package com.example.tutortracking.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.localdata.models.LocallyAddedStudent
import com.example.tutortracking.data.localdata.models.LocallyDeletedStudent
import com.example.tutortracking.data.localdata.models.LocallyUpdatedStudent
import com.example.tutortracking.data.remotedata.models.Student
import com.example.tutortracking.data.remotedata.models.Tutor
import com.example.tutortracking.data.remotedata.models.Update
import java.io.ByteArrayOutputStream
import java.util.*

fun areFieldsEmpty(
    email: String,
    password: String,
    name:String?=null,
    modules: String?=null
)
= name?.isEmpty()?: false
        || email.isEmpty()
        || password.isEmpty()
        || modules?.isEmpty()?: false

fun getImageBytes(imageUri: Uri?, context: Context): ByteArray {
    val input = context.contentResolver.openInputStream(imageUri!!)
    val image = BitmapFactory.decodeStream(input, null, null)
    // Encode image to base64 string
    val baos = ByteArrayOutputStream()
    image!!.compress(Bitmap.CompressFormat.JPEG, 15, baos)
    return baos.toByteArray()
    //return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}

fun getImageString(imageByteArray: ByteArray?) : String
= Base64.encodeToString(
    imageByteArray,
    Base64.DEFAULT)

fun decode(imageString: String): Bitmap? {
    // Decode base64 string to image
    val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    decodedImage?.let {
        return decodedImage
    }
    return null
}

fun getLocallyAddedFromStudent(student: LocalStudent)
= LocallyAddedStudent(
    student.studentName,
    student.studentYear,
    student.studentSubject,
    student.studentTutorId,
    student.studentPic,
    student._id)

fun getLocallyUpdatedFromStudent(student: LocalStudent)
= LocallyUpdatedStudent(
    student.studentName,
    student.studentYear,
    student.studentSubject,
    student.studentTutorId,
    student.studentPic,
    student._id)

fun getLocallyDeletedFromStudent(
    student: LocalStudent)
= LocallyDeletedStudent(student.studentName,
    student.studentYear,
    student.studentSubject,
    student.studentTutorId,
    student.studentPic
    , student._id)

fun getStudentFromLocallyAdded(
    locallyAdded: LocallyAddedStudent)
= Student(locallyAdded.studentName,
    locallyAdded.studentYear,
    locallyAdded.studentSubject,
    locallyAdded.studentTutorId,
    locallyAdded.studentPic,
    locallyAdded._id)

fun getStudentFromLocallyUpdated(
    locallyUpdated: LocallyUpdatedStudent)
= Student(locallyUpdated.studentName,
    locallyUpdated.studentYear,
    locallyUpdated.studentSubject,
    locallyUpdated.studentTutorId,
    locallyUpdated.studentPic,
    locallyUpdated._id)
fun String.doNamesOperations() : String{
    return this.trim()
        .split(" ")
        .map { name->
            name.replaceFirstChar { firstChat->
                if (firstChat.isLowerCase())
                    firstChat.titlecase(Locale.getDefault())
                else
                    firstChat.toString()
            }
        }.joinToString(separator = " ")
}