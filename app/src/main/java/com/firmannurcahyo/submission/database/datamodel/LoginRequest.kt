package com.firmannurcahyo.submission.database.datamodel

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val name: String?,

    @SerializedName("password") val password: String?
)