package com.example.smartcloset_frontend.utils

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.viewmodel.GetWeatherViewModel

@Composable
fun WeatherLocationLoader(
    getWeatherViewModel: GetWeatherViewModel
) {
    val context = LocalContext.current

    // 権限状態
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // 権限リクエスト
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    // 初回：権限なければ要求、あれば天気取得
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            try {
                val loc = GetLocation.getLastLocationSuspend(context)
                getWeatherViewModel.fetchWeather(
                    LocationData(lat = loc.latitude, lon = loc.longitude) // ←順番注意
                )
            } catch (e: Exception) {
                Log.e("Weather", "Location error: ${e.message}", e)
            }
        }
    }
}