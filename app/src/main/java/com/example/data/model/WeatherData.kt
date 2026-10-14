package com.example.data.model

import androidx.compose.ui.graphics.Color

data class HourlySituation(
    val timeLabel: String, // e.g. "Now", "14:00", "17:00", "20:00", "23:00"
    val temperature: Double,
    val condition: String,
    val rainProbability: Int,
    val windSpeed: Double
)

data class CityWeather(
    val name: String,
    val country: String,
    val region: String, // "Asia", "Europe", "North America", "South America", "Africa", "Middle East", "Oceania", "Polar"
    val latitude: Double, // in degrees, -90 to 90
    val longitude: Double, // in degrees, -180 to 180
    val temperature: Double, // in Celsius
    val minTemp: Double,
    val maxTemp: Double,
    val feelsLike: Double,
    val condition: String, // "Clear", "Rainy", "Cloudy", "Stormy", "Aurora", "Blizzard", "Sunny"
    val humidity: Int, // %
    val windSpeed: Double, // km/h
    val windDirection: Double, // degrees
    val pressure: Int, // hPa
    val uvIndex: Int,
    val visibility: Double, // km
    val dewPoint: Double, // °C
    val satelliteCloudCover: Int, // % cloud cover
    val liveSatelliteName: String, // "Sentinel-6", "Aqua", "GOES-16", "METEOSAT-11"
    val satelliteSignalStrength: Int, // dBm (e.g. -85)
    val orbitalHeight: Int, // km (e.g. 814)
    val orbitalVelocity: Double, // km/s (e.g. 7.4)
    val carbonDioxidePpm: Int, // Carbon dioxide sensor, e.g. 421 ppm
    val airQualityIndex: Int, // 1 to 5
    val situationDescription: String,
    val imageUrl: String,
    val satellitePhotoUrl: String = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800",
    val localTime: String = "12:00 UTC",
    val hourlyForecast: List<HourlySituation> = emptyList()
)

enum class SatelliteFilter {
    CLOUD_DENSITY,
    ATMOSPHERE_THERMAL,
    WIND_STREAM,
    CO2_EMISSIONS
}

data class OrbitSatPoint(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val color: Color,
    val symbol: String
)
