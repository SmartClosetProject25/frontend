package com.example.smartcloset_frontend.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object GetLocation {
    //使用する際はUI側に権限許可がいる
    @SuppressLint("MissingPermission")
    suspend fun getLastLocationSuspend(context: Context): Location =
        suspendCancellableCoroutine { cont ->
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        cont.resume(location)
                        Log.d("GetLocation", "getLastLocationSuspend: $location")
                    } else {
                        cont.resumeWithException(
                            IllegalStateException("現在地が取得できませんでした（null）")
                        )
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}