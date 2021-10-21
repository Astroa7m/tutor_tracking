package com.example.tutortracking.util

import java.util.*

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

fun String.capitalize()=
    this.replaceFirstChar { firstChar->
        if (firstChar.isLowerCase())
            firstChar.titlecase(Locale.getDefault())
        else
            firstChar.toString()
    }

