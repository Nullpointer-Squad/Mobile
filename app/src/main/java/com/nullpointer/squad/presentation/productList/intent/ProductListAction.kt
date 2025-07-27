package com.nullpointer.squad.presentation.productList.intent

sealed class ProductListAction {
    data class AddToCart(val productId: String) : ProductListAction()
    data class ToggleFavorite(val productId: String) : ProductListAction()
    data class SearchProducts(val query: String) : ProductListAction()
    data class FilterByCategory(val category: String) : ProductListAction()
    data object ViewAllProducts : ProductListAction()
    data object RefreshProducts : ProductListAction()
    data object LoadMoreProducts : ProductListAction()
    data class NavigateToProductDetail(val productId: String) : ProductListAction()
    object NavigateBack : ProductListAction()
    object ToggleCurrency : ProductListAction()
    data class GetProductDetail(val productId: String) : ProductListAction()
    data class NavigateToPage(val page: Int) : ProductListAction() // Add this
    data class LoadSimilarProducts(val category: String) : ProductListAction()
    data object LoadMoreSimilarProducts : ProductListAction()
    data class SortBy(val field: String) : ProductListAction()
}