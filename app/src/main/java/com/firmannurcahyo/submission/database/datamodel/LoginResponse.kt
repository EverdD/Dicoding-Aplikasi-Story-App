package com.firmannurcahyo.submission.database.datamodel

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("loginResult") val loginResult: AuthResponse?,

    @field:SerializedName("error") val error: Boolean?,

    @field:SerializedName("message") val message: String?
)