package com.example.tutortracking.ui

import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageString
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class ImageBitmapMatcher (imageByteArray: ByteArray) : TypeSafeMatcher<View?>(View::class.java) {

    private val bitmap = decode(getImageString(imageByteArray))

    override fun describeTo(description: Description) {
        description.appendText("with bitmap")
    }

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageView) {
            return false
        }
        val expectedBitmap = target.drawable?.toBitmap()
        return bitmap?.sameAs(expectedBitmap) ?: true
    }

    companion object{
        fun withBitmap(imageByteArray: ByteArray) = ImageBitmapMatcher(imageByteArray)
    }

}
