package com.example.healthhand.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val photo: String? = null
)