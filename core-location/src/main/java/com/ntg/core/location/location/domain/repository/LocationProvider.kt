package com.ntg.core.location.location.domain.repository

import android.content.Context
import com.ntg.core.location.location.domain.model.Coordinates

interface LocationProvider {
    fun getLastKnownLocation(
        context: Context,
        onResult: (Coordinates?) -> Unit,
    )
}