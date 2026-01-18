package com.example.smartcloset_frontend.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.WeatherData
import com.example.smartcloset_frontend.data.repository.GetWeatherRepository
import com.example.smartcloset_frontend.data.toWeatherData
import com.example.smartcloset_frontend.utils.GetLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GetWeatherViewModel(
    private val repository: GetWeatherRepository = GetWeatherRepository()
) : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var fetched = false

    fun fetchWeatherByCurrentLocationOnce(context: Context) {
        if (fetched) return
        fetched = true

        viewModelScope.launch {
            try {
                val loc = GetLocation.getLastLocationSuspend(context)
                fetchWeather(LocationData(lat = loc.latitude, lon = loc.longitude))
                Log.d("WeatherVM1", "VM instance=${this.hashCode()}")

            } catch (e: Exception) {
                Log.e("Weather", "Location error: ${e.message}", e)
            }
        }
    }

    fun fetchWeather(locationData: LocationData) {
        viewModelScope.launch {
            repository.getWeather(locationData)
                .onSuccess { dto ->
                    _weatherData.value = dto.toWeatherData()
                    _error.value = null
                    Log.d("WeatherVM2", "VM instance=${this.hashCode()}")

                }
                .onFailure {
                    _error.value = "Error fetching weather data: ${it.message}"
                }
        }
    }
}

