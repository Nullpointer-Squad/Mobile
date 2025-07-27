package com.nullpointer.squad.presentation.productList

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.model.ProductData
import com.nullpointer.squad.domain.model.ProductItem
import com.nullpointer.squad.domain.usecase.ProductUseCase
import com.nullpointer.squad.presentation.productList.intent.NavigationEvent.NavigateToProductDetail
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.presentation.productList.model.ProductListState
import com.nullpointer.squad.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val applicationContext: Application,
    private val productUseCase: ProductUseCase,
    private val coroutineDispatcher: DispatcherProvider
) : ViewModel()
{

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(ProductListState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                loadProducts()
                loadExchangeRate()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProductListState()
        )
    private val _selectedCurrency = MutableStateFlow("USD")
    val selectedCurrency = _selectedCurrency.asStateFlow()


    fun onAction(action: ProductListAction) {
        Log.d("SimilarProducts", "ViewModel onAction called with: ${action::class.simpleName}")

        when (action) {
            is ProductListAction.AddToCart -> {
//                addToCart(action.productId)
            }
            is ProductListAction.ToggleFavorite -> {
//                toggleFavorite(action.productId)
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
            is ProductListAction.ToggleCurrency -> toggleCurrency()

            is ProductListAction.GetProductDetail -> {
                getProductDetails(action.productId) // Add this
            }
            is ProductListAction.LoadSimilarProducts -> {
                loadSimilarProducts(action.category)
            }
            is ProductListAction.LoadMoreSimilarProducts -> {
                loadMoreSimilarProducts()
            }

            ProductListAction.NavigateBack -> Unit

            is ProductListAction.NavigateToProductDetail ->{
                _state.update {
                    it.copy(navigationEvent = NavigateToProductDetail(action.productId))
                }
            }

            is ProductListAction.SortBy -> {
                if (action.field.isEmpty() || action.field == "Clear Sort") {
                    clearSort()
                } else {
                    sortProduct(action.field)
                }
            }

            is ProductListAction.NavigateToPage -> {
                if (_state.value.isSorted) {
                    // If currently sorted, navigate with sort
                    navigateToPageWithSort(_state.value.currentSortField, action.page)
                } else {
                    // Regular navigation
                    navigateToPage(action.page)
                }
            }
        }
    }

    fun clearNavigationEvent() {
        _state.update { it.copy(navigationEvent = null) }
    }

    private fun sortProduct(field: String) {
        viewModelScope.launch(coroutineDispatcher.io()) {
            try {
                Log.d("ProductList", "Sorting products by: $field")
                _state.update {
                    it.copy(
                        isLoading = true,
                        currentSortField = field,
                        isSorted = true,
                        currentPage = 1 // Reset to first page when sorting
                    )
                }

                val sortedProducts = productUseCase.sortTheProducts(
                    field = field,
                    page = 1, // Always start from page 1 when sorting
                    limit = _state.value.pageSize
                )

                _state.update {
                    it.copy(
                        products = sortedProducts,
                        isLoading = false,
                        error = null,
                        currentPage = 1,
                        totalPages = sortedProducts.data.pagination.totalPages,
                        hasMorePages = sortedProducts.data.pagination.hasNext
                    )
                }

                Log.d("ProductList", "Sort completed - Total pages: ${sortedProducts.data.pagination.totalPages}")

            } catch (e: Exception) {
                Log.e("ProductList", "Sort failed: ${e.message}")
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to sort products",
                        isLoading = false,
                        isSorted = false,
                        currentSortField = ""
                    )
                }
            }
        }
    }

    private fun clearSort() {
        viewModelScope.launch(coroutineDispatcher.io()) {
            try {
                Log.d("ProductList", "Clearing sort - loading regular products")
                _state.update {
                    it.copy(
                        isLoading = true,
                        currentSortField = "",
                        isSorted = false,
                        currentPage = 1 // Reset to first page
                    )
                }

                // Load regular products (first page)
                val products = productUseCase.getProductsUseCase(
                    page = 1,
                    pageSize = _state.value.pageSize
                )

                _state.update {
                    it.copy(
                        products = products,
                        isLoading = false,
                        error = null,
                        currentPage = 1,
                        totalPages = products.data.pagination.totalPages,
                        hasMorePages = products.data.pagination.hasNext
                    )
                }

                Log.d("ProductList", "Clear sort completed - loaded regular products")

            } catch (e: Exception) {
                Log.e("ProductList", "Clear sort failed: ${e.message}")
                _state.update {
                    it.copy(
                        error = e.message ?: "Failed to load products",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun navigateToPageWithSort(sortField: String, page: Int) {
        viewModelScope.launch(coroutineDispatcher.io()) {
            try {
                Log.d("ProductList", "Navigating to page $page with sort: $sortField")
                _state.update { it.copy(isLoading = true) }

                val response = productUseCase.sortTheProducts(
                    field = sortField,
                    page = page,
                    limit = _state.value.pageSize
                )

                _state.update {
                    it.copy(
                        products = response,
                        currentPage = page,
                        totalPages = response.data.pagination.totalPages,
                        hasMorePages = response.data.pagination.hasNext,
                        isLoading = false,
                        error = null
                    )
                }

                Log.d("ProductList", "Sort navigation completed - Page: $page")

            } catch (e: Exception) {
                Log.e("ProductList", "Sort navigation failed: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load page $page: ${e.message}"
                    )
                }
            }
        }
    }


    private fun loadSimilarProducts(category: String) {
        viewModelScope.launch(coroutineDispatcher.io()) {
            try {
                _state.update {
                    it.copy(
                        isLoadingSimilar = true,
                        currentSimilarCategory = category,
                        similarProductsCurrentPage = 1,
                        similarProductsHasMore = true
                    )
                }

                val similarProductsResponse = productUseCase.getProductByCategory(
                    page = 1,
                    pageSize = 10, // Get 10 similar products per page
                    category = category
                )
                _state.update {
                    it.copy(
                        similarProducts = similarProductsResponse,
                        isLoadingSimilar = false,
                        similarProductsCurrentPage = similarProductsResponse.data.pagination.page,
                        similarProductsHasMore = similarProductsResponse.data.pagination.hasNext
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingSimilar = false,
                        error = "Failed to load similar products"
                    )
                }
            }
        }
    }

    private fun loadMoreSimilarProducts() {
        viewModelScope.launch(coroutineDispatcher.io()){
            val currentState = _state.value

            // Check if we can load more
            if (currentState.isLoadingMoreSimilar) {
                return@launch
            }

            if (!currentState.similarProductsHasMore) {
                return@launch
            }

            if (currentState.currentSimilarCategory == null) {
                return@launch
            }

            try {
                _state.update { it.copy(isLoadingMoreSimilar = true) }

                val nextPage = currentState.similarProductsCurrentPage + 1

                val moreProductsResponse = productUseCase.getProductByCategory(
                    page = nextPage,
                    pageSize = 10,
                    category = currentState.currentSimilarCategory!!
                )

                // Combine existing products with new ones
                val existingProducts = currentState.similarProducts.data.products
                val newProducts = moreProductsResponse.data.products
                val combinedProducts = existingProducts + newProducts


                _state.update {
                    it.copy(
                        similarProducts = Product(
                            success = moreProductsResponse.success,
                            data = ProductData(
                                products = combinedProducts,
                                pagination = moreProductsResponse.data.pagination
                            ),
                            message = moreProductsResponse.message
                        ),
                        isLoadingMoreSimilar = false,
                        similarProductsCurrentPage = moreProductsResponse.data.pagination.page,
                        similarProductsHasMore = moreProductsResponse.data.pagination.hasNext
                    )
                }

            } catch (e: Exception) {
                   _state.update {
                    it.copy(
                        isLoadingMoreSimilar = false,
                        error = "Failed to load more similar products: ${e.message}"
                    )
                }
            }
        }
    }


    private fun loadProducts() {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                val products = productUseCase.getProductsUseCase()

                // Also load latest product on initial load
                loadLatestProducts()

                _state.update {
                    it.copy(
                        products = products,
                        totalPages = products.data.pagination.totalPages,
                        currentPage = 1,
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


    private fun toggleCurrency() {
        _selectedCurrency.value = if (_selectedCurrency.value == "USD") "INR" else "USD"
    }

    fun loadExchangeRate() {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                val response = productUseCase.getExchangeRatesUseCase()
                _state.value = _state.value.copy(
                    usdToInrRate = response.rates["INR"] ?: 83.0 // fallback to static value
                )
                Log.d("ExchangeRate", "USD to INR: ${_state.value.usdToInrRate}")
            } catch (e: Exception) {
                Log.e("ExchangeRate", "Failed to fetch currency: ${e.message}")
            }
        }
    }

    private fun viewAllProducts() {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                _state.update { it.copy(isLoading = true) }

                val allProducts = productUseCase.getProductsUseCase()

                _state.update {
                    it.copy(
                        products = allProducts,
                        isLoading = false,
                        selectedCategory = null,
                        searchQuery = ""
                    )
                }
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load all products"
                    )
                }
            }
        }
    }

    private fun getProductDetails(id: String) {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                _state.update { it.copy(isLoading = true) }
                val productDetail = productUseCase.getProductDetailUseCase(id)
                _state.update {
                    it.copy(
                        productDetailResponse = productDetail,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load product details: ${e.message}"
                    )
                }
            }
        }
    }

    private fun refreshProducts() {
        viewModelScope.launch(coroutineDispatcher.io()) {
            hasLoadedInitialData = false
            loadProducts()
        }
    }

    private fun filterByCategory(category: String) {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                _state.update { it.copy(isLoading = true, selectedCategory = category) }

                val filteredProducts = productUseCase.getProductsUseCase().data.products.filter {
                    it.Category.equals(category, ignoreCase = true)
                }

                _state.update {
                    it.copy(
                        products = Product(
                            success = true,
                            data = ProductData(products = filteredProducts),
                            message = "Filtered by category"
                        ),
                        isLoading = false,
                        error = null
                    )
                }

            } catch (_: Exception) {
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
        viewModelScope.launch(coroutineDispatcher.io()){
            val currentState = _state.value

            if (currentState.isLoadingMore || !currentState.hasMorePages) return@launch

            _state.update { it.copy(isLoadingMore = true) }

            try {
                val response = productUseCase.getProductsUseCase(
                    page = currentState.currentPage + 1,
                    pageSize = currentState.pageSize,
                )

                val updatedProducts = currentState.products.data.products + response.data.products

                _state.update {
                    it.copy(
                        products = Product(
                            success = response.success,
                            data = ProductData(
                                products = updatedProducts,
                                pagination = response.data.pagination
                            ),
                            message = response.message
                        ),
                        currentPage = response.data.pagination.page,
                        isLoadingMore = false,
                        hasMorePages = response.data.pagination.hasNext
                    )
                }

            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Failed to load more products"
                    )
                }
            }
        }
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {
                _state.update { it.copy(isLoading = true, searchQuery = query) }

                val trimmedQuery = query.trim()

                if (trimmedQuery.isEmpty()) {
                    viewAllProducts()
                    return@launch
                }

                // Check if the query is a number (product index)
                val indexQuery = trimmedQuery.toIntOrNull()

                if (indexQuery != null) {
                    // Search by index
                    searchByIndex(indexQuery)
                } else {
                    // Regular text search
                    performTextSearch(trimmedQuery)
                }
            } catch (e: Exception) {
                Log.e("SearchProducts", "Search failed: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun searchByIndex(index: Int) {
        viewModelScope.launch(coroutineDispatcher.io()){
            try {

                val searchResult = productUseCase.getProductBySearch(index)


                if (searchResult.success && searchResult.data.isNotEmpty()) {
                    val productData = searchResult.data[0]

                    val productItem = ProductItem(
                        Internal_id = productData.Internal_id ?: productData.EAN,
                        Name = productData.Name,
                        Brand = productData.Brand,
                        InitialPrice = productData.InitialPrice,
                        Price = productData.Price,
                        Rating = productData.Rating,
                        Index = productData.Index,
                        Stock = productData.Stock,
                        Image = productData.Image,
                        Currency = productData.Currency,
                        Category = productData.Category ?: "Unknown",
                    )

                    _state.update {
                        it.copy(
                            products = Product(
                                success = true,
                                data = ProductData(products = listOf(productItem)),
                                message = "Product found at index $index"
                            ),
                            isLoading = false,
                            error = null
                        )
                    }


                } else {

                    // This is the key part - set empty products list which will trigger EmptyStateSection
                    _state.update {
                        it.copy(
                            products = Product(
                                success = true,
                                data = ProductData(products = emptyList()),
                                message = "No product found at index $index"
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {

                _state.update {
                    it.copy(
                        products = Product(
                            success = true,
                            data = ProductData(products = emptyList()),
                            message = "Search failed"
                        ),
                        isLoading = false,
                        error = "Could not find product at index $index"
                    )
                }
            }
        }

    }

    private fun performTextSearch(query: String) {
        viewModelScope.launch (coroutineDispatcher.io()){
            try {
                Log.d("TextSearch", "Performing text search for: $query")

                val allProducts = productUseCase.getProductsUseCase()

                val filteredProducts = allProducts.data.products.filter { product ->
                    product.Name.contains(query, ignoreCase = true) ||
                            product.Brand.contains(query, ignoreCase = true) ||
                            product.Category?.contains(query, ignoreCase = true) == true
                }

                Log.d("TextSearch", "Found ${filteredProducts.size} products")

                _state.update {
                    it.copy(
                        products = Product(
                            success = true,
                            data = ProductData(products = filteredProducts),
                            message = if (filteredProducts.isEmpty())
                                "No products found for '$query'"
                            else
                                "${filteredProducts.size} products found"
                        ),
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("TextSearch", "Text search failed: ${e.message}")

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Search failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun navigateToPage(page: Int) {
        viewModelScope.launch (coroutineDispatcher.io()) {
            try {
                _state.update { it.copy(isLoading = true) }

                val response = productUseCase.getProductsUseCase(
                    page = page,
                    pageSize = _state.value.pageSize
                )

                _state.update {
                    it.copy(
                        products = response,
                        currentPage = page,
                        totalPages = response.data.pagination.totalPages,
                        hasMorePages = response.data.pagination.hasNext,
                        isLoading = false,
                        error = null
                    )
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load page $page: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadLatestProducts() {
        viewModelScope.launch (coroutineDispatcher.io()) {
            try {
                val latestProductsResponse = productUseCase.getLatestProduct()
                if (latestProductsResponse.success && latestProductsResponse.data.isNotEmpty()) {
                    _state.update {
                        it.copy(latestProducts = latestProductsResponse)
                    }

                    Log.d("LatestProducts", "Loaded ${latestProductsResponse.data.size} latest products")
                }
            } catch (e: Exception) {
                Log.e("LatestProducts", "Failed to load latest products: ${e.message}")
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

    // HelperFile function to clear error
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // HelperFile function to clear message
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}