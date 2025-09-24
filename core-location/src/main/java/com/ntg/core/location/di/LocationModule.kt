package com.ntg.core.location.di

import com.google.android.gms.location.LocationServices
import com.ntg.core.location.location.data.repository.LocationProviderImpl
import com.ntg.core.location.location.data.repository.LocationRepositoryImpl
import com.ntg.core.location.location.domain.usecase.GetDeviceLocationsUseCase
import com.ntg.core.location.location.domain.repository.LocationProvider
import com.ntg.core.location.location.domain.repository.LocationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val locationModule = module {

    // FusedLocationProviderClient (from Google Play services)
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    // Provider API (callback style)
    single<LocationProvider> { LocationProviderImpl(get()) }

    // Repository API (suspend style)
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // UseCase (depends on LocationRepository)
    factory { GetDeviceLocationsUseCase(get()) }
}