package com.nullpointer.squad.domain.model

data class Product(
    val id: String = "",
    val name: String ="",
    val description: String? = "",
    val originalPrice: Int = 0,
    val discountedPrice: Int = 0,
    val discountPercentage: Int = 0,
    val rating: Float = 0F,
    val category: String = "",
    val imageUrl: String? = "",
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val isAddingToCart: Boolean = false,
    val stock: Int = 0,
    val brand: String? = "",
    val imageResId: String = ""
)
