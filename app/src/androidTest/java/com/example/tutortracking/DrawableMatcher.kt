package com.example.tutortracking

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class DrawableMatcher (private val expectedId: Int) : TypeSafeMatcher<View?>(View::class.java) {
    private var resourceName: String? = null
    private fun getBitmap(drawable: Drawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(expectedId)
        if (resourceName != null) {
            description.appendText("[")
            description.appendText(resourceName)
            description.appendText("]")
        }
    }

    companion object {
        private const val EMPTY = -1
        private const val ANY = -2

        fun withDrawable(resourceId: Int): DrawableMatcher {
            return DrawableMatcher(resourceId)
        }
    }

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageView) {
            return false
        }
        if (expectedId == EMPTY) {
            return target.drawable == null
        }
        if (expectedId == ANY) {
            return target.drawable != null
        }
        val resources = target.getContext().resources
        val expectedDrawable = resources.getDrawable(expectedId, null)
        resourceName = resources.getResourceEntryName(expectedId)
        val bitmap : Bitmap? = getBitmap(target.drawable)
        bitmap?.let {
            val otherBitmap = getBitmap(expectedDrawable)
            return bitmap.sameAs(otherBitmap)
        }
        return false
    }
}
