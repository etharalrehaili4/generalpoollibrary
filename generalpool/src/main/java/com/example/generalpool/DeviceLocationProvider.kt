package com.example.generalpool

import android.location.Location

interface DeviceLocationProvider {
    suspend fun getDeviceLocations(): Pair<Location?, Location?>
}
