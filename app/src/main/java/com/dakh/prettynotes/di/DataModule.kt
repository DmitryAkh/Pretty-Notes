package com.dakh.prettynotes.di

import android.content.Context
import androidx.room.Database
import com.dakh.prettynotes.data.NotesDao
import com.dakh.prettynotes.data.NotesDatabase
import com.dakh.prettynotes.data.NotesRepositoryImpl
import com.dakh.prettynotes.domain.NotesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindNotesRepository(
        impl: NotesRepositoryImpl
    ): NotesRepository

    companion object {
        @Singleton
        @Provides
        fun provideDatabase(
           @ApplicationContext context: Context
        ): NotesDatabase {
            return NotesDatabase.getInstance(context)
        }

        @Singleton
        @Provides
        fun provideNotesDao(database: NotesDatabase): NotesDao {
            return database.notesDao()
        }
    }
}