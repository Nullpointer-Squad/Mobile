package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.SearchProductResponse
import com.nullpointer.squad.domain.repository.ProductRepository
import jakarta.inject.Inject


class GetProductBySearch @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        id: Int?
    ): SearchProductResponse = repository.getProductBySearch(id)
}