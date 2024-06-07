package com.firmannurcahyo.submission.database.datamodel

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("userId") val userId: String?,

    @SerializedName("name") val name: String?,

    @SerializedName("token") val token: String?
)