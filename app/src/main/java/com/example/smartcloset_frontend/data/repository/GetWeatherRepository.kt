package com.example.smartcloset_frontend.data.repository
import com.example.smartcloset_frontend.network.RetrofitClient
import com.example.smartcloset_frontend.data.WeatherDto
import com.example.smartcloset_frontend.data.LocationData

class GetWeatherRepository {
    suspend fun getWeather(locationData: LocationData): Result<WeatherDto> {
        return runCatching {
            val response = RetrofitClient.instance.getWeather(locationData)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Response body is null")
            } else {
                throw Exception("Error: ${response.code()} ${response.message()}")
            }
        }
    }
}
