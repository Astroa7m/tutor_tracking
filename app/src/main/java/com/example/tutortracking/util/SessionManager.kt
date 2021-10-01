package com.example.tutortracking.util

import android.content.Context
import android.icu.number.IntegerWidth
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.tutortracking.util.Constants.SORT_ORDER
import com.example.tutortracking.util.Constants.THEME_MODE_KEY
import com.example.tutortracking.util.Constants.TOKEN_KEY
import com.example.tutortracking.util.Constants.TUTOR_ID_KEY
import com.example.tutortracking.util.PreferencesKeys.JWT_TOKEN
import com.example.tutortracking.util.PreferencesKeys.SORTING_ORDER
import com.example.tutortracking.util.PreferencesKeys.THEME_MODE
import com.example.tutortracking.util.PreferencesKeys.TUTOR_ID
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class SessionManager(val context: Context) {
    private val Context.myDatastore : DataStore<Preferences> by preferencesDataStore("session_info")

    suspend fun updateSession(token: String, tutorId: String){
        context.myDatastore.edit { pref->
            pref[JWT_TOKEN] = token
            pref[TUTOR_ID] = tutorId
        }
    }

    suspend fun updateThemeMode(themeInt: Int) {
       context.myDatastore.edit { pref->
           pref[THEME_MODE] = themeInt
       }
    }


    suspend fun getTutorToken(): String? {
        val preference = context.myDatastore.data.first()
        return preference[JWT_TOKEN]
    }

    suspend fun getThemeMode() : Int {
        val preference = context.myDatastore.data.first()
        return preference[THEME_MODE]?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    suspend fun getTutorId(): String?{
        val preference = context.myDatastore.data.first()
        return preference[TUTOR_ID]
    }

    suspend fun logout() {
        context.myDatastore.edit {
            it.clear()
        }
    }

    val filterPreferences = context.myDatastore.data
        .catch { exception->
            if(exception is IOException)
                emptyPreferences()
            else{
                exception.printStackTrace()
                throw exception
            }
        }.map { preferences->
            val sortingOrder = SortOrder.valueOf(preferences[SORTING_ORDER]?:SortOrder.BY_NAME.name)
            sortingOrder
        }

    suspend fun updateSortingOrder(sortOrder: SortOrder){
        context.myDatastore.edit { preference->
            preference[SORTING_ORDER] = sortOrder.name
        }
    }

}

enum class SortOrder{BY_NAME, BY_YEAR, BY_SUBJECT}

private object PreferencesKeys{
    val JWT_TOKEN = stringPreferencesKey(TOKEN_KEY)
    val TUTOR_ID = stringPreferencesKey(TUTOR_ID_KEY)
    val SORTING_ORDER = stringPreferencesKey(SORT_ORDER)
    val THEME_MODE = intPreferencesKey(THEME_MODE_KEY)
}