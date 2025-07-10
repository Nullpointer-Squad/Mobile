package com.nullpointer.squad.data.remote

import com.nullpointer.squad.domain.model.Product
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
}
