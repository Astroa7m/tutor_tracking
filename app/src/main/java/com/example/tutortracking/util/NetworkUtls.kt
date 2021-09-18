package com.example.tutortracking.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun hasInternetConnection(context: Context) : Boolean{
    val connectivityManger = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
        val activeNetwork = connectivityManger.activeNetwork ?: return false
        val capabilities = connectivityManger.getNetworkCapabilities(activeNetwork) ?: return false
        return when{
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->true
            else -> false
        }
    }else{
        connectivityManger.activeNetworkInfo?.run {
            return when(type){
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> true
                ConnectivityManager.TYPE_ETHERNET -> true
                else -> false
            }
        }
    }
    return false
}