package com.nullpointer.squad.domain.usecase

import com.nullpointer.squad.domain.model.ExchangeRateResponse
import com.nullpointer.squad.domain.repository.ProductRepository
import javax.inject.Inject

class GetExchangeRateUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): ExchangeRateResponse {
        return repository.getCurrencyRates()
    }
}
