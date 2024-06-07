package com.firmannurcahyo.submission.database.datamodel

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "stories")
data class StoriesDatabase(

    @PrimaryKey(autoGenerate = false) @field:SerializedName("id") val id: String,

    @field:SerializedName("name") val name: String?,

    @field:SerializedName("photoUrl") val photoUrl: String?,

    @field:SerializedName("createdAt") val createdAt: String?,

    @field:SerializedName("description") val description: String?,

    @field:SerializedName("lon") val lon: Double? = null,

    @field:SerializedName("lat") val lat: Double? = null
) : Parcelable