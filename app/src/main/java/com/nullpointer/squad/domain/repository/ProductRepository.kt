package com.nullpointer.squad.domain.repository

import com.nullpointer.squad.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(page: Int, pageSize: Int, category: String?): List<Product>
}