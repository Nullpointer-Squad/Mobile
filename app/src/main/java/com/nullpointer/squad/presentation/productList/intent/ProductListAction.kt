package com.nullpointer.squad.presentation.productList.intent

sealed class ProductListAction {
    data class AddToCart(val productId: String) : ProductListAction()
    data class ToggleFavorite(val productId: String) : ProductListAction()
    data class SearchProducts(val query: String) : ProductListAction()
    data class FilterByCategory(val category: String) : ProductListAction()
    data object ViewAllProducts : ProductListAction()
    data object RefreshProducts : ProductListAction()
    data object LoadMoreProducts : ProductListAction()
}