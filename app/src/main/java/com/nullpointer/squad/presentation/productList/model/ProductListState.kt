package com.nullpointer.squad.presentation.productList.model

import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.presentation.productList.view.sampleProducts

data class ProductListState(
    val products: List<Product> = sampleProducts,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val cartItemCount: Int = 0,
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val hasMorePages: Boolean = true
)