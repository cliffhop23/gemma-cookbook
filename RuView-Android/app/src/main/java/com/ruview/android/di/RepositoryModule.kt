package com.ruview.android.di

import com.ruview.android.data.api.RuViewApiService
import com.ruview.android.data.repository.RuViewRepository
import com.ruview.android.data.websocket.SensingWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSensingWebSocketClient(
        okHttpClient: OkHttpClient,
        json: Json
    ): SensingWebSocketClient = SensingWebSocketClient(okHttpClient, json)

    @Provides
    @Singleton
    fun provideRuViewRepository(
        apiService: RuViewApiService,
        wsClient: SensingWebSocketClient
    ): RuViewRepository = RuViewRepository(apiService, wsClient)
}
