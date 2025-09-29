package com.example.generalpool.data.models

data class PageInfo(
    val page: Int? = null,
    val totalPages: Int? = null,
    val nextPage: Int? = null,
    val hasMore: Boolean? = null,
    val cursor: String? = null,
    val nextCursor: String? = null,
)
