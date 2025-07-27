package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.LatestProductResponse
import com.nullpointer.squad.domain.repository.ProductRepository
import jakarta.inject.Inject

class GetLatestProduct @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): LatestProductResponse = repository.getLatestProduct()
}