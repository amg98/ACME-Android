package com.dam.acmeexplorer.extensions

import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest

fun FusedLocationProviderClient.tryRequestLocationUpdates(locationCallback: LocationCallback): Boolean {

    val req = LocationRequest.create()
    req.interval = 5000
    req.priority = LocationRequest.PRIORITY_LOW_POWER
    req.smallestDisplacement = 5.0f

    return try {
        this.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
        true
    } catch (e: SecurityException) {
        false
    }
}
