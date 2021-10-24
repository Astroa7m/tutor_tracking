package com.example.tutortracking.util

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    private const val RESOURCE = "Global"

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment(){
        countingIdlingResource.increment()
    }
    fun decrement(){
        if(!countingIdlingResource.isIdleNow)
            countingIdlingResource.decrement()
    }
}