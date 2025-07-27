package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.domain.repository.ProductRepository
import jakarta.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 50,
    ): Product = repository.getProducts(page,pageSize)
}