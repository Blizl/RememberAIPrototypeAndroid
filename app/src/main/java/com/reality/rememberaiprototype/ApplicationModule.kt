package com.reality.rememberaiprototype

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.reality.rememberaiprototype.home.data.AppDatabase
import com.reality.rememberaiprototype.home.data.MemoryDao
import com.reality.rememberaiprototype.home.data.MlKitTextRecognition
import com.reality.rememberaiprototype.home.domain.TextRecognitionProcessor
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    companion object {
        @Provides
        fun providesMemoryDatabase(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application.applicationContext,
                AppDatabase::class.java, "memory-database"
            ).build()
        }

        @Provides
        fun providesMemoryDao(db: AppDatabase): MemoryDao {
            return db.memoryDao()
        }

        @Provides
        fun providesTextRecognitionProcessor(application: Application): TextRecognitionProcessor {
            return MlKitTextRecognition(application.contentResolver)
        }
    }

}