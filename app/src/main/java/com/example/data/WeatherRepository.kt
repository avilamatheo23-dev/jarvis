package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val cityName: String = "Bogotá",
    val temperatureC: Int = 18,
    val conditionText: String = "Parcialmente nublado",
    val conditionIcon: String = "partly_cloudy",
    val humidityPercent: Int = 68,
    val windSpeedKmh: Int = 12,
    val lastUpdated: String = "Ahora"
)

data class CityLocation(
    val name: String,
    val department: String,
    val latitude: Double,
    val longitude: Double
)

object ColombianCities {
    val list = listOf(
        CityLocation("Bogotá", "Cundinamarca", 4.7110, -74.0721),
        CityLocation("Medellín", "Antioquia", 6.2442, -75.5812),
        CityLocation("Cali", "Valle del Cauca", 3.4516, -76.5320),
        CityLocation("Barranquilla", "Atlántico", 10.9685, -74.7813),
        CityLocation("Cartagena", "Bolívar", 10.3910, -75.4794),
        CityLocation("Bucaramanga", "Santander", 7.1254, -73.1198),
        CityLocation("Pereira", "Risaralda", 4.8133, -75.6961),
        CityLocation("Manizales", "Caldas", 5.0689, -75.5174)
    )
    val defaultCity = list[0]
}

class WeatherRepository {
    private val TAG = "JarvisWeather"
    private val _weatherState = MutableStateFlow(WeatherData())
    val weatherState: StateFlow<WeatherData> = _weatherState.asStateFlow()

    private var currentCity = ColombianCities.defaultCity

    fun getSelectedCity(): CityLocation = currentCity

    suspend fun setCity(city: CityLocation) {
        currentCity = city
        fetchWeather()
    }

    suspend fun fetchWeather() = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=${currentCity.latitude}&longitude=${currentCity.longitude}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=America%2FBogota"
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current")
                val temp = current.getDouble("temperature_2m").toInt()
                val humidity = current.optInt("relative_humidity_2m", 65)
                val wind = current.optDouble("wind_speed_10m", 10.0).toInt()
                val code = current.getInt("weather_code")

                val (conditionText, iconName) = parseWeatherCode(code)

                _weatherState.value = WeatherData(
                    cityName = currentCity.name,
                    temperatureC = temp,
                    conditionText = conditionText,
                    conditionIcon = iconName,
                    humidityPercent = humidity,
                    windSpeedKmh = wind,
                    lastUpdated = "Actualizado"
                )
            } else {
                Log.w(TAG, "Weather API response not OK: ${connection.responseCode}")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch error: ${e.message}. Using cache/fallback.")
            // Maintain stable fallback based on city
            val fallbackTemp = when (currentCity.name) {
                "Bogotá" -> 17
                "Medellín" -> 24
                "Cali" -> 28
                "Barranquilla", "Cartagena" -> 31
                "Bucaramanga" -> 26
                else -> 20
            }
            _weatherState.value = WeatherData(
                cityName = currentCity.name,
                temperatureC = fallbackTemp,
                conditionText = "Parcialmente nublado",
                conditionIcon = "partly_cloudy",
                humidityPercent = 70,
                windSpeedKmh = 14,
                lastUpdated = "Estimado (Sin conexión)"
            )
        }
    }

    private fun parseWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Cielo despejado" to "sunny"
            1, 2 -> "Mayormente despejado" to "partly_cloudy"
            3 -> "Nublado" to "cloudy"
            45, 48 -> "Niebla" to "fog"
            51, 53, 55 -> "Llovizna" to "rain_light"
            61, 63, 65 -> "Lluvia" to "rain"
            80, 81, 82 -> "Chubascos" to "rain"
            95, 96, 99 -> "Tormenta eléctrica" to "thunderstorm"
            else -> "Parcialmente nublado" to "partly_cloudy"
        }
    }
}
