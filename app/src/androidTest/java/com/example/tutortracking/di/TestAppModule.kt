package com.example.tutortracking.di

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.tutortracking.data.localdata.StudentDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Named("db_test")
    fun provideInMemoryDatabaseInstance(
        @ApplicationContext context: Context
    ) = Room.inMemoryDatabaseBuilder(
        context,
        StudentDatabase::class.java,
    ).allowMainThreadQueries().build()
}
