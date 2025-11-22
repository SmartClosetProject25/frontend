package com.example.smartcloset_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.data.WeatherData
import com.example.smartcloset_frontend.data.repository.GetWeatherRepository
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

    fun fetchWeather(locationData: LocationData) {
        viewModelScope.launch {
            repository.getWeather(locationData)
                .onSuccess {
                    _weatherData.value = it
                }
                .onFailure {
                    _error.value = "Error fetching weather data: ${it.message}"
                }
        }
    }
}
