package com.ntg.lmd.mainscreen.data.model

import com.google.gson.annotations.SerializedName

data class Column(
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
)
