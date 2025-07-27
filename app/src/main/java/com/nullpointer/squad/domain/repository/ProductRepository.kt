package com.nullpointer.squad.domain.repository

import com.nullpointer.squad.domain.model.ExchangeRateResponse
import com.nullpointer.squad.domain.model.LatestProductResponse
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.model.ProductDetailResponse
import com.nullpointer.squad.domain.model.SearchProductResponse

interface ProductRepository {
    suspend fun getProducts(page: Int, pageSize: Int): Product
    suspend fun getProductByCategory(page: Int, pageSize: Int,category: String?): Product
    suspend fun sortProduct(limit: Int, page: Int,field: String?): Product
    suspend fun getCurrencyRates(): ExchangeRateResponse
    suspend fun getProductById(id: String): ProductDetailResponse
    suspend fun getProductBySearch(id: Int?): SearchProductResponse
    suspend fun getLatestProduct(): LatestProductResponse
}