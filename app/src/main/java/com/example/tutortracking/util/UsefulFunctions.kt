package com.example.tutortracking.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

fun areFieldsEmpty(
    email: String,
    password: String,
    name:String?=null,
    modules: String?=null
) = name?.isEmpty()?: true && email.isEmpty() && password.isEmpty() && modules?.isEmpty()?: true

fun getImageBytes(imageUri: Uri?, context: Context): ByteArray {
    val input = context.contentResolver.openInputStream(imageUri!!)
    val image = BitmapFactory.decodeStream(input, null, null)
    // Encode image to base64 string
    val baos = ByteArrayOutputStream()
    image!!.compress(Bitmap.CompressFormat.JPEG, 45, baos)
    return baos.toByteArray()
    //return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}

fun getImageString(imageByteArray: ByteArray?) : String = Base64.encodeToString(imageByteArray, Base64.DEFAULT)

fun decode(imageString: String) : Bitmap{

    // Decode base64 string to image
    val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return decodedImage
    //binding.imageView.setImageBitmap(decodedImage)
}