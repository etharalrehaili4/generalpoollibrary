package com.ntg.core.location.location.domain.usecase

import com.ntg.core.location.location.domain.model.Coordinates
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ComputeDistancesUseCase {
    companion object {
        private const val METERS_IN_KM = 1000.0
        private const val EARTH_RADIUS_KM = 6371.0
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }

    fun computeDistances(
        origin: Coordinates,
        targets: List<Coordinates>
    ): List<Double> {
        return targets.map { target ->
            if (isValidLatLng(target.latitude, target.longitude)) {
                distanceKm(origin, target)
            } else {
                Double.POSITIVE_INFINITY
            }
        }
    }

    fun isValidLatLng(lat: Double, lng: Double): Boolean {
        val finite = lat.isFinite() && lng.isFinite()
        val nonZero = !(lat == 0.0 && lng == 0.0)
        val inBounds = abs(lat) <= MAX_LATITUDE && abs(lng) <= MAX_LONGITUDE
        return finite && nonZero && inBounds
    }

    fun distanceKm(from: Coordinates, to: Coordinates): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)

        val a = sin(dLat / 2).pow(2.0) +
                sin(dLng / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }
}