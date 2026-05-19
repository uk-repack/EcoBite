package com.example.ecobite.di

import android.content.Context
import androidx.room.Room
import com.example.ecobite.data.local.AppDatabase
import com.example.ecobite.data.local.PantryDao
import com.example.ecobite.data.local.WasteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.ecobite.data.remote.FoodFactsRepository
import com.example.ecobite.data.remote.OpenFoodFactsApi
import com.example.ecobite.data.remote.gemini.GeminiApi
import com.example.ecobite.data.remote.gemini.GeminiRepository

import androidx.work.WorkManager
import com.example.ecobite.worker.NotificationScheduler
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ecobite_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePantryDao(database: AppDatabase): PantryDao {
        return database.pantryDao()
    }

    @Provides
    @Singleton
    fun provideWasteDao(database: AppDatabase): WasteDao {
        return database.wasteDao()
    }

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(): OpenFoodFactsApi {
        return retrofit2.Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/api/v0/")
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodFactsRepository(
        api: OpenFoodFactsApi
    ): FoodFactsRepository {
        return FoodFactsRepository(api)
    }

    @Provides
    @Singleton
    fun provideGeminiApi(): GeminiApi {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create()
            )
            .build()
            .create(GeminiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiRepository(
        api: GeminiApi
    ): GeminiRepository {
        return GeminiRepository(api)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context
    ): NotificationScheduler {
        return NotificationScheduler(context)
    }
}

