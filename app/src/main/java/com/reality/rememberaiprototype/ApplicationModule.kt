package com.reality.rememberaiprototype

import android.app.Application
import androidx.room.Room
import com.reality.rememberaiprototype.home.data.AppDatabase
import com.reality.rememberaiprototype.home.data.DefaultHomeRepository
import com.reality.rememberaiprototype.home.data.DefaultLocalRepository
import com.reality.rememberaiprototype.home.data.MemoryDao
import com.reality.rememberaiprototype.processors.data.MlKitTextRecognition
import com.reality.rememberaiprototype.home.domain.HomeRepository
import com.reality.rememberaiprototype.home.domain.LocalRepository
import com.reality.rememberaiprototype.imagetotext.data.DefaultImageToTextRepository
import com.reality.rememberaiprototype.imagetotext.domain.ImageToTextRepository
import com.reality.rememberaiprototype.processors.domain.TextRecognitionProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
        @Provides
        @Singleton
        fun providesHomeRepository(
            application: Application,
            localRepository: LocalRepository,
            imageToTextRepository: ImageToTextRepository
        ): HomeRepository {
            return DefaultHomeRepository(application, localRepository, imageToTextRepository)
        }

        @Provides
        @Singleton
        fun providesLocalRepository(memoryDao: MemoryDao): LocalRepository {
            return DefaultLocalRepository(memoryDao)
        }

        @Provides
        @Singleton
        fun providesImageToTextRepository(
            application: Application,
            textRecognitionProcessor: TextRecognitionProcessor,
            localRepository: LocalRepository): ImageToTextRepository {
            return DefaultImageToTextRepository(application, textRecognitionProcessor, localRepository)
        }

        @Provides
        @Singleton
        fun providesIODispatcher(): CoroutineDispatcher {
            return Dispatchers.IO
        }

    }

}