package com.ntg.core.location.location.data.repository

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.ntg.core.location.location.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationRepositoryImpl(
    private val fused: FusedLocationProviderClient,
) : LocationRepository {
    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fused
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
}