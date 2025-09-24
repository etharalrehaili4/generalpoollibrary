package com.ntg.core.location.location.domain.repository

import android.location.Location

interface LocationRepository {
    suspend fun getLastLocation(): Location?

    suspend fun getCurrentLocation(): Location?
}