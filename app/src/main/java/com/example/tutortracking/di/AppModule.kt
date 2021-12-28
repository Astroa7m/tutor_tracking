package com.example.tutortracking.di

import android.content.Context
import androidx.room.Room
import com.example.tutortracking.data.localdata.StudentDao
import com.example.tutortracking.data.localdata.StudentDatabase
import com.example.tutortracking.data.remotedata.MessageService
import com.example.tutortracking.data.remotedata.TutorApi
import com.example.tutortracking.data.repository.TutorRepository
import com.example.tutortracking.data.repository.TutorRepositoryImpl
import com.example.tutortracking.util.Constants.BASE_URL
import com.example.tutortracking.util.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
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
        sessionManager: SessionManager,
        messageService: MessageService
    ) : TutorRepository = TutorRepositoryImpl(
        studentDao,
        tutorApi,
        sessionManager,
        messageService)

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient{
        return HttpClient(CIO){
            install(Logging)
            install(WebSockets)
            install(JsonFeature){
                serializer = KotlinxSerializer()
            }
        }
    }

    @Singleton
    @Provides
    fun provideMessageService(
        client: HttpClient
    ) = MessageService(client)

}