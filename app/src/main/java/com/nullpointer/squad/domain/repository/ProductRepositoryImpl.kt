package com.nullpointer.squad.domain.repository

import com.nullpointer.squad.data.remote.RemoteDataSource
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.presentation.productList.view.sampleProducts
import jakarta.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : ProductRepository {
    override suspend fun getProducts(page: Int, pageSize: Int, category: String?): List<Product> {

        return sampleProducts.let { products ->
            category?.let { cat ->
                products.filter { it.category.equals(cat, ignoreCase = true) }
            } ?: products
        }.let { filteredProducts ->
            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, filteredProducts.size)
            if (startIndex < filteredProducts.size) {
                filteredProducts.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }
//        return remoteDataSource.fetchProducts()
    }
}

