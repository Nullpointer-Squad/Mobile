package com.nullpointer.squad.domain.model

import com.google.gson.annotations.SerializedName

data class Product(
    val success: Boolean = false,
    val data: ProductData = ProductData(),
    val message: String = ""
)

data class ProductData(
    val products: List<ProductItem> = emptyList(),
    val pagination: Pagination = Pagination()
)

data class ProductItem(
    val Index: Int = 0,
    val Name: String = "",
    val Brand: String = "",
    val Category: String? = null,
    val Price: Double = 0.0,
    val Currency: String = "",
    val Stock: Int = 0,
    val Image: String? = null,
    val Rating: Double = 0.0,
    val InitialPrice: Double = 0.0,
    val Internal_id: String = ""
)

data class Pagination(
    val page: Int = 0,
    val limit: Int = 50,
    val total: Int = 1000,
    val totalPages: Int = 20,
    val hasNext: Boolean = false,
    val hasPrev: Boolean = false,
)

data class ProductDetailResponse(
    val success: Boolean = false,
    val data: ProductDetail = ProductDetail(),
    val message: String = ""
)
data class ProductDetail(
    val Index: Int = 0,
    val Name: String = "",
    val Description: String = "",
    val Brand: String = "",
    val Category: String? = null,
    val Price: Double = 0.0,
    val Currency: String = "",
    val Stock: Int = 0,
    val EAN: String = "",
    val Availability: Boolean = false,
    val ShortDescription: String = "",
    val Image: String? = null,
    val Rating: Double = 0.0,
    val InitialPrice: Double = 0.0,
    val Internal_id: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    @SerializedName("__v")
    val version: Int = 0
)

data class SearchProductResponse(
    val success: Boolean,
    val data: List<SearchProductItem>,
    val message: String
)

data class SearchProductItem(
    val Index: Int,
    val Name: String,
    val Description: String?,
    val Brand: String,
    val Category: String?,
    val Price: Double,
    val Currency: String,
    val Stock: Int,
    val EAN: String,
    val Availability: Boolean,
    val ShortDescription: String,
    val Image: String,
    val Rating: Double,
    val InitialPrice: Double,
    val Internal_id: String?,
    val __v: Int,
    val createdAt: String,
    val updatedAt: String
)


data class LatestProductResponse(
    val success: Boolean = false,
    val data: List<LatestProduct> = emptyList(),
    val message: String = ""
)

data class LatestProduct(
    val Index: Int = 0,
    val Name: String = "",
    val Brand: String = "",
    val Category: String? = null,
    val Price: Double = 0.0,
    val Currency: String = "",
    val Stock: Int = 0,
    val Image: String = "",
    val Rating: Double = 0.0,
    val InitialPrice: Double = 0.0,
    val Internal_id: String = ""
)

