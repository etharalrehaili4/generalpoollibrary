package com.ntg.core.location.location.domain.usecase

import android.location.Location
import com.ntg.core.location.location.domain.repository.LocationRepository

class GetDeviceLocationsUseCase(
    private val locationRepo: LocationRepository,
) {
    suspend operator fun invoke(): Pair<Location?, Location?> {
        val last = locationRepo.getLastLocation()
        val current = locationRepo.getCurrentLocation()
        return last to current
    }
}