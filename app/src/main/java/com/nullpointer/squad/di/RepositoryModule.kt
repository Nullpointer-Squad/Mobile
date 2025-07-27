package com.nullpointer.squad.di


import com.nullpointer.squad.data.remote.ApiService
import com.nullpointer.squad.data.remote.ExchangeRateApi
import com.nullpointer.squad.domain.repository.ProductRepository
import com.nullpointer.squad.domain.repository.ProductRepositoryImpl
import com.nullpointer.squad.domain.usecase.GetExchangeRateUseCase
import com.nullpointer.squad.domain.usecase.GetLatestProduct
import com.nullpointer.squad.domain.usecase.GetProductByCategory
import com.nullpointer.squad.domain.usecase.GetProductBySearch
import com.nullpointer.squad.domain.usecase.GetProductDetailUseCase
import com.nullpointer.squad.domain.usecase.GetProductsUseCase
import com.nullpointer.squad.domain.usecase.ProductUseCase
import com.nullpointer.squad.domain.usecase.SortTheProducts
import com.nullpointer.squad.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        apiService: ApiService,
        coroutineDispatcher: DispatcherProvider,
        exchangeRateApi: ExchangeRateApi
    ): ProductRepository {
        return ProductRepositoryImpl(
            apiService = apiService,
            coroutineDispatcher = coroutineDispatcher,
            exchangeRateApi = exchangeRateApi
        )
    }

    @Provides
    @Singleton
    fun provideProductUseCase(
        repository: ProductRepository
    ): ProductUseCase {
       return ProductUseCase(
           getProductsUseCase = GetProductsUseCase(repository),
           getProductDetailUseCase = GetProductDetailUseCase(repository),
           getExchangeRatesUseCase = GetExchangeRateUseCase(repository),
           getProductBySearch = GetProductBySearch(repository),
           getLatestProduct = GetLatestProduct(repository),
           getProductByCategory = GetProductByCategory(repository),
           sortTheProducts = SortTheProducts(repository)
       )
    }
}
