package com.example.tutortracking.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tutortracking.util.Constants.TOKEN_KEY
import com.example.tutortracking.util.Constants.TUTOR_ID_KEY
import kotlinx.coroutines.flow.first

class SessionManager(val context: Context) {
    private val Context.myDatastore : DataStore<Preferences> by preferencesDataStore("session_info")

    suspend fun updateSession(token: String){
        // creating keys for the preferences
        val jwtTokenKey = stringPreferencesKey(TOKEN_KEY)
        //writing them in the preferences
        context.myDatastore.edit { pref->
            pref[jwtTokenKey] = token
        }
    }
    suspend fun getTutorToken(): String? {
        val jwtTokenKey = stringPreferencesKey(TOKEN_KEY)
        val preference = context.myDatastore.data.first()
        return preference[jwtTokenKey]
    }

    suspend fun logout() {
        context.myDatastore.edit {
            it.clear()
        }
    }

}