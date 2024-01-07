package com.reality.rememberaiprototype

import android.app.Application
import androidx.room.Room
import com.reality.rememberaiprototype.home.data.AppDatabase
import com.reality.rememberaiprototype.home.data.MemoryDao
import com.reality.rememberaiprototype.home.data.MlKitTextRecognition
import com.reality.rememberaiprototype.home.domain.TextRecognitionProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    companion object {
        @Provides
        @Singleton
        fun providesMemoryDatabase(application: Application): AppDatabase {
            return Room.databaseBuilder(
                application.applicationContext,
                AppDatabase::class.java, "memory-database"
            ).build()
        }

        @Provides
        @Singleton
        fun providesMemoryDao(db: AppDatabase): MemoryDao {
            return db.memoryDao()
        }

        @Provides
        @Singleton
        fun providesTextRecognitionProcessor(application: Application): TextRecognitionProcessor {
            return MlKitTextRecognition(application.contentResolver)
        }
    }

}