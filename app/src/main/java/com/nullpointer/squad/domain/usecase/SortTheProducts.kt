package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.repository.ProductRepository
import jakarta.inject.Inject

class SortTheProducts @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        limit: Int = 50,
        page: Int = 1,
        field: String?
    ): Product = repository.sortProduct(limit = limit, page = page, field = field)
}