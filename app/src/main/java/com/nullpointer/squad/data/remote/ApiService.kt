package com.nullpointer.squad.data.remote

import com.nullpointer.squad.domain.model.LatestProductResponse
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.model.ProductDetailResponse
import com.nullpointer.squad.domain.model.SearchProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("products")
    suspend fun getProducts(
        @Query("min_price") minPrice: Int,
        @Query("max_price") maxPrice: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): Product


    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: String
    ): ProductDetailResponse

    @GET("products/search")
    suspend fun searchProductByIndex(
        @Query("q") index: Int?
    ): SearchProductResponse

    @GET("products/latest-10")
    suspend fun getLatestProduct(): LatestProductResponse

    @GET("products/by-category")
    suspend fun getProductByCategory(
        @Query("page") page: Int,
        @Query("limit") pageSize: Int,
        @Query("category") category: String?=null,
    ): Product

    @GET("products/sort")
    suspend fun sortProduct(
        @Query("field") field: String?=null,
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): Product

}
