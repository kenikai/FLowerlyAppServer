package com.example.flowerlyapp.server.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItemResponse(
    val id: String,
    val flowerId: String,
    val flowerName: String,
    val quantity: Int,
    val price: Double,
    val totalPrice: Double,
    val imageResourceId: Int = 0
)
