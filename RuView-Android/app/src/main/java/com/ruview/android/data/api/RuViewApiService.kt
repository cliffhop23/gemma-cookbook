package com.ruview.android.data.api

import com.ruview.android.data.models.MatResponse
import com.ruview.android.data.models.ServerStatus
import com.ruview.android.data.models.VitalSigns
import com.ruview.android.data.models.ZonesResponse
import retrofit2.http.GET

interface RuViewApiService {
    @GET("/health")
    suspend fun getHealth(): ServerStatus

    @GET("/api/v1/vital-signs")
    suspend fun getVitalSigns(): VitalSigns

    @GET("/api/v1/zones")
    suspend fun getZones(): ZonesResponse

    @GET("/api/v1/mat")
    suspend fun getMat(): MatResponse
}
