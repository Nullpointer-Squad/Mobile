package com.nullpointer.squad.data.remote

import com.nullpointer.squad.domain.model.Product
import jakarta.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchProducts(): List<Product> {
        return apiService.getProducts() // Directly return List<Product>
    }
}

