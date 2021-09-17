package com.example.tutortracking.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tutortracking.util.Constants.TOKEN_KEY
import com.example.tutortracking.util.Constants.TUTOR_EMAIL_KEY
import com.example.tutortracking.util.Constants.TUTOR_NAME_KEY
import kotlinx.coroutines.flow.first

class SessionManager(val context: Context) {
    private val Context.myDatastore : DataStore<Preferences> by preferencesDataStore("session_info")

    suspend fun updateSession(token: String, tutorName: String, tutorEmail: String){
        // creating keys for the preferences
        val jwtTokenKey = stringPreferencesKey(TOKEN_KEY)
        val tutorNameKey = stringPreferencesKey(TUTOR_NAME_KEY)
        val tutorEmailKey = stringPreferencesKey(TUTOR_EMAIL_KEY)
        //writing them in the preferences
        context.myDatastore.edit { pref->
            pref[jwtTokenKey] = token
            pref[tutorNameKey] = tutorName
            pref[tutorEmailKey] = tutorEmail
        }
    }
    suspend fun getTutorToken() : String? {
        val jwtTokenKey = stringPreferencesKey(TOKEN_KEY)
        val preference = context.myDatastore.data.first()
        return preference[jwtTokenKey]
    }

    suspend fun getTutorName() : String? {
        val tutorNameKey = stringPreferencesKey(TUTOR_NAME_KEY)
        val preference = context.myDatastore.data.first()
        return preference[tutorNameKey]
    }

    suspend fun getTutorEmail() : String? {
        val tutorEmailKey = stringPreferencesKey(TUTOR_EMAIL_KEY)
        val preference = context.myDatastore.data.first()
        return preference[tutorEmailKey]
    }

    suspend fun logout() {
        context.myDatastore.edit {
            it.clear()
        }
    }

}