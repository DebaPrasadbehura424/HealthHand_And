package com.example.healthhand.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Test(
    val id: String,
    @SerialName("clinic_id") val clinicId: String,
    val name: String,
    @SerialName("test_type") val testType: String,
    val description: String = "",
    val price: Double,
    @SerialName("parameters_count") val parametersCount: Int = 0,
    val included: List<String> = emptyList(),
    @SerialName("is_popular") val isPopular: Boolean = false
)