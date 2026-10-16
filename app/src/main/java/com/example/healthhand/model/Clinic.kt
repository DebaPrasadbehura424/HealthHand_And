package com.example.healthhand.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Clinic(
    val id: String,
    val name: String,
    val type: String = "Clinic",
    val image: String = "",
    val address: String,
    val phone: String = "",
    val rating: Double = 0.0,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("open_time") val openTime: String = "7:00 AM",
    @SerialName("close_time") val closeTime: String = "8:00 PM",
    @SerialName("home_collection") val homeCollection: Boolean = true,
    val lat: Double,
    val lng: Double
)