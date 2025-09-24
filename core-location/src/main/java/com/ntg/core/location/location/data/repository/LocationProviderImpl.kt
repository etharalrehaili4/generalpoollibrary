package com.ntg.core.location.location.data.repository

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.ntg.core.location.location.domain.repository.LocationProvider
import com.ntg.core.location.location.domain.model.Coordinates

class LocationProviderImpl(
    private val fusedClient: FusedLocationProviderClient
) : LocationProvider {
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getLastKnownLocation(
        context: Context,
        onResult: (Coordinates?) -> Unit,
    ) {
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    onResult(
                        loc?.let { Coordinates(it.latitude, it.longitude) }
                    )
                }
                .addOnFailureListener { onResult(null) }
        } catch (e: SecurityException) {
            onResult(null)
        }
    }
}