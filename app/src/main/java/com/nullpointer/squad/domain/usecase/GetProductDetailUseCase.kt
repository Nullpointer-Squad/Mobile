package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.ProductDetailResponse
import com.nullpointer.squad.domain.repository.ProductRepository
import jakarta.inject.Inject


class GetProductDetailUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        id: String
    ): ProductDetailResponse = repository.getProductById(id)
}