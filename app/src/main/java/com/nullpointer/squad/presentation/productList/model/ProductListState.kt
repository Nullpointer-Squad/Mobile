package com.nullpointer.squad.presentation.productList.model

import com.nullpointer.squad.domain.model.LatestProductResponse
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.model.ProductDetailResponse
import com.nullpointer.squad.presentation.productList.intent.NavigationEvent

data class ProductListState(
    val products: Product = Product(),
    val productDetailResponse: ProductDetailResponse? = ProductDetailResponse(),
    val similarProducts: Product = Product(), // Similar products data
    val similarProductsCurrentPage: Int = 1, // Current page for similar products
    val similarProductsHasMore: Boolean = true, // Whether there are more similar products
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingSimilar: Boolean = false, // Loading first page of similar products
    val isLoadingMoreSimilar: Boolean = false, // Loading more similar products
    val currentSimilarCategory: String? = null, // Track current category for similar products
    val error: String? = null,
    val message: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val cartItemCount: Int = 0,
    val currentPage: Int = 1,
    val latestProducts: LatestProductResponse = LatestProductResponse(),
    val totalPages: Int = 1,
    val pageSize: Int = 50,
    val hasMorePages: Boolean = true,
    var usdToInrRate: Double = 1.0,
    val navigationEvent: NavigationEvent? = null,
    val sortOption: String = "",
    val currentSortField: String = "", // Track current sort field
    val isSorted: Boolean = false
)