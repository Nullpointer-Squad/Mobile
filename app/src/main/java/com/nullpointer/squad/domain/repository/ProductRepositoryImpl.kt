package com.nullpointer.squad.domain.repository

import com.nullpointer.squad.data.remote.ApiService
import com.nullpointer.squad.data.remote.ExchangeRateApi
import com.nullpointer.squad.data.remote.ExchangeRatesApi
import com.nullpointer.squad.domain.model.ExchangeRateResponse
import com.nullpointer.squad.domain.model.LatestProductResponse
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.model.ProductDetailResponse
import com.nullpointer.squad.domain.model.SearchProductResponse
import com.nullpointer.squad.util.DispatcherProvider
import jakarta.inject.Inject
import kotlinx.coroutines.withContext

class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val coroutineDispatcher: DispatcherProvider,
    @ExchangeRatesApi private val exchangeRateApi: ExchangeRateApi,
    ) : ProductRepository {

    override suspend fun getProducts(
        page: Int,
        pageSize: Int
    ): Product {
        return withContext(coroutineDispatcher.io()) {
            apiService.getProducts(
                minPrice = 1,
                maxPrice = 10,
                page = page,
                pageSize = pageSize,
            )
        }
    }

    override suspend fun getProductByCategory(
        page: Int,
        pageSize: Int,
        category: String?
    ): Product {
       return withContext(coroutineDispatcher.io()) {
           apiService.getProductByCategory(
               page = page,
               pageSize = pageSize,
               category = category
           )
       }
    }

    override suspend fun sortProduct(
        limit: Int,
        page: Int,
        field: String?
    ): Product {
        return withContext(coroutineDispatcher.io()){
            apiService.sortProduct(
                limit = limit,
                page = page,
                field = field
            )
        }

    }

    override suspend fun getCurrencyRates(): ExchangeRateResponse {
        return withContext(coroutineDispatcher.io()) {
            exchangeRateApi.getRates()
        }
    }

    override suspend fun getProductById(id: String): ProductDetailResponse {
        return withContext(coroutineDispatcher.io()){
            apiService.getProductById(id)
        }
    }

    override suspend fun getProductBySearch(id: Int?): SearchProductResponse {
        return withContext(coroutineDispatcher.io()){
            apiService.searchProductByIndex(id)
        }
    }

    override suspend fun getLatestProduct(): LatestProductResponse {
        return withContext(coroutineDispatcher.io()){
            apiService.getLatestProduct()
        }
    }
}


