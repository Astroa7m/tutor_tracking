package com.example.tutortracking

import com.example.tutortracking.util.Result

fun <E> Collection<E>.log(): Collection<E> {
    print("list size = ${ this.size }")
    return this
}

fun <T> Result<T>.log(msg: String?=null): Result<T> {
    println(if(msg!=null) "In $msg: ${this::class.java.simpleName}" else this::class.java.simpleName)
    return this
}

fun <T> Result<T>.logMessage(): Result<T> {
    println("\tmessage = ${this.message}")
    return this
}