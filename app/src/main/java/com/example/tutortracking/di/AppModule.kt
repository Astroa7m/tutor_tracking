package com.example.tutortracking.di

import android.content.Context
import androidx.room.Dao
import androidx.room.Room
import com.example.tutortracking.data.localdata.StudentDao
import com.example.tutortracking.data.localdata.StudentDatabase
import com.example.tutortracking.data.remotedata.models.Student
import com.example.tutortracking.data.remotedata.models.TutorApi
import com.example.tutortracking.data.repository.TutorRepository
import com.example.tutortracking.data.repository.TutorRepositoryImpl
import com.example.tutortracking.util.Constants.BASE_URL
import com.example.tutortracking.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideStudentDao(
        db: StudentDatabase
    ) = db.getStudentDao()

    @Singleton
    @Provides
    fun provideStudentDatabase(
        @ApplicationContext context: Context
    ) : StudentDatabase = Room.databaseBuilder(
        context,
        StudentDatabase::class.java,
        "studentDatabase"
    ).build()

    @Singleton
    @Provides
    fun provideTutorApi() : TutorApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TutorApi::class.java)
    }

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) = SessionManager(context)

    @Singleton
    @Provides
    fun provideRepository(
        studentDao: StudentDao,
        tutorApi: TutorApi,
        sessionManager: SessionManager
    ) : TutorRepository = TutorRepositoryImpl(
        studentDao,
        tutorApi,
        sessionManager)
}