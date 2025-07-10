package com.nullpointer.squad.di


import com.nullpointer.squad.data.remote.RemoteDataSource
import com.nullpointer.squad.domain.repository.ProductRepository
import com.nullpointer.squad.domain.repository.ProductRepositoryImpl
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
        remoteDataSource: RemoteDataSource
    ): ProductRepository {
        return ProductRepositoryImpl(remoteDataSource)
    }
}
