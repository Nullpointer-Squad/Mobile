package com.nullpointer.squad.presentation.productList

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.squad.domain.usecase.GetProductsUseCase
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.presentation.productList.model.ProductListState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val applicationContext: Application,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProductListState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadProducts()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProductListState()
        )

    fun onAction(action: ProductListAction) {
        when (action) {
            is ProductListAction.AddToCart -> {
                addToCart(action.productId)
            }
            is ProductListAction.ToggleFavorite -> {
                toggleFavorite(action.productId)
            }
            is ProductListAction.SearchProducts -> {
                searchProducts(action.query)
            }
            ProductListAction.ViewAllProducts -> {
                viewAllProducts()
            }
            ProductListAction.RefreshProducts -> {
                refreshProducts()
            }
            is ProductListAction.FilterByCategory -> {
                filterByCategory(action.category)
            }
            is ProductListAction.LoadMoreProducts -> {
                loadMoreProducts()
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val products = getProductsUseCase()

                _state.update {
                    it.copy(
                        products = products,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load products"
                    )
                }
            }
        }
    }

    private fun addToCart(productId: String) {
        viewModelScope.launch {
            try {
                // Show loading state for the specific product
                _state.update { currentState ->
                    currentState.copy(
                        products = currentState.products.map { product ->
                            if (product.id == productId) {
                                product.copy(isAddingToCart = true)
                            } else {
                                product
                            }
                        }
                    )
                }

                // Simulate API call or actual cart addition logic
                delay(1000) // Remove this in real implementation

                // Update cart count and product state
                _state.update { currentState ->
                    currentState.copy(
                        products = currentState.products.map { product ->
                            if (product.id == productId) {
                                product.copy(
                                    isAddingToCart = false,
                                    isInCart = true
                                )
                            } else {
                                product
                            }
                        },
                        cartItemCount = currentState.cartItemCount + 1
                    )
                }

                // Show success message
                showMessage("Product added to cart successfully!")

            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        products = currentState.products.map { product ->
                            if (product.id == productId) {
                                product.copy(isAddingToCart = false)
                            } else {
                                product
                            }
                        }
                    )
                }
                showMessage("Failed to add product to cart")
            }
        }
    }

    private fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    currentState.copy(
                        products = currentState.products.map { product ->
                            if (product.id == productId) {
                                product.copy(isFavorite = !product.isFavorite)
                            } else {
                                product
                            }
                        }
                    )
                }

                // You might want to sync this with backend
                val updatedProduct = _state.value.products.find { it.id == productId }
                val message = if (updatedProduct?.isFavorite == true) {
                    "Added to favorites"
                } else {
                    "Removed from favorites"
                }
                showMessage(message)

            } catch (e: Exception) {
                showMessage("Failed to update favorites")
            }
        }
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, searchQuery = query) }

                // Implement your search logic here
                val filteredProducts = if (query.isBlank()) {
                    getProductsUseCase() // Get all products
                } else {
                    getProductsUseCase().filter { product ->
                        product.name.contains(query, ignoreCase = true) ||
                                product.description?.contains(query, ignoreCase = true) == true
                    }
                }

                _state.update {
                    it.copy(
                        products = filteredProducts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun viewAllProducts() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val allProducts = getProductsUseCase()

                _state.update {
                    it.copy(
                        products = allProducts,
                        isLoading = false,
                        selectedCategory = null,
                        searchQuery = ""
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load all products"
                    )
                }
            }
        }
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            hasLoadedInitialData = false
            loadProducts()
        }
    }

    private fun filterByCategory(category: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, selectedCategory = category) }

                // Implement category filtering logic
                val filteredProducts = getProductsUseCase().filter { product ->
                    product.category.equals(category, ignoreCase = true)
                }

                _state.update {
                    it.copy(
                        products = filteredProducts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to filter products"
                    )
                }
            }
        }
    }

    private fun loadMoreProducts() {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                if (currentState.isLoadingMore || !currentState.hasMorePages) {
                    return@launch
                }

                _state.update { it.copy(isLoadingMore = true) }

                // Implement pagination logic here
                val moreProducts = getProductsUseCase(
                    page = currentState.currentPage + 1,
                    pageSize = currentState.pageSize
                )

                _state.update {
                    it.copy(
                        products = it.products + moreProducts,
                        currentPage = it.currentPage + 1,
                        isLoadingMore = false,
                        hasMorePages = moreProducts.size == it.pageSize
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Failed to load more products"
                    )
                }
            }
        }
    }

    private fun showMessage(message: String) {
        _state.update { it.copy(message = message) }

        // Clear message after delay
        viewModelScope.launch {
            delay(3000)
            _state.update { it.copy(message = null) }
        }
    }

    // Helper function to clear error
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // Helper function to clear message
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}