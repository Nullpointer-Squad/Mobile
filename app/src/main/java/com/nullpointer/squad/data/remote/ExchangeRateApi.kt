package com.nullpointer.squad.data.remote

import com.nullpointer.squad.domain.model.ExchangeRateResponse
import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("latest/USD")
    suspend fun getRates(): ExchangeRateResponse
}
