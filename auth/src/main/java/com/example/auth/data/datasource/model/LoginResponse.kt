package com.example.auth.data.datasource.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: LoginData?,
    @SerializedName("error")
    val error: String?,
)