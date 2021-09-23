package com.example.tutortracking.util

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListOfString(list: List<String>) = list.joinToString(
        separator = ","
    )
    @TypeConverter
    fun toListOfString(string: String) = string.split(",").map { it.trim()}
}