package com.chicken.jelly.di

import android.content.Context
import com.chicken.jelly.data.GameDataStoreRepository
import com.chicken.jelly.data.GameRepository
import com.chicken.jelly.sound.SoundManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRepository(@ApplicationContext context: Context): GameRepository =
        GameDataStoreRepository(context)

    @Provides
    @Singleton
    fun provideSoundManager(@ApplicationContext context: Context): SoundManager =
        SoundManager(context)
}
