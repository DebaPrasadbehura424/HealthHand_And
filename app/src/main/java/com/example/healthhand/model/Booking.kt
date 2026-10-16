package com.example.healthhand.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("test_id") val testId: String,
    @SerialName("clinic_id") val clinicId: String,
    @SerialName("user_name") val userName: String = "",
    @SerialName("user_phone") val userPhone: String = "",
    @SerialName("test_name") val testName: String,
    @SerialName("test_type") val testType: String,
    @SerialName("clinic_name") val clinicName: String,
    val amount: Double,
    @SerialName("collection_type") val collectionType: String,
    val date: String,
    @SerialName("time_slot") val timeSlot: String,
    val address: String = "",
    val instructions: String = "",
    val status: String = "Pending"
)