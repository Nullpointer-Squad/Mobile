package com.nullpointer.squad.presentation.productList.intent

sealed class NavigationEvent {
    data class NavigateToProductDetail(val productId: String) : NavigationEvent()
    object NavigateBack : NavigationEvent()
}