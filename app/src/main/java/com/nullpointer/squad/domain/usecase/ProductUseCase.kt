package com.nullpointer.squad.domain.usecase

data class ProductUseCase (
    val getProductsUseCase: GetProductsUseCase,
    val getProductDetailUseCase: GetProductDetailUseCase,
    val getExchangeRatesUseCase: GetExchangeRateUseCase,
    val getProductBySearch:GetProductBySearch,
    val getLatestProduct: GetLatestProduct,
    val getProductByCategory: GetProductByCategory,
    val sortTheProducts: SortTheProducts
)