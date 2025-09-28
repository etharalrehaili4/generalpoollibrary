package com.example.generalpool.models

data class PageInfo(
    val page: Int? = null,
    val totalPages: Int? = null,
    val nextPage: Int? = null,
    val hasMore: Boolean? = null,
    val cursor: String? = null,
    val nextCursor: String? = null,
)
