package com.example.data.model

import java.time.LocalDate

data class HistoricalWeatherDay(
    val date: LocalDate,
    val dateFormatted: String, // e.g. "Mon, Oct 12"
    val tempMax: Double, // in Celsius
    val tempMin: Double,
    val tempAvg: Double,
    val condition: String, // "Sunny", "Partly Cloudy", "Overcast", "Rainy", "Heavy Storm", "Snow", etc.
    val precipitationMm: Double, // mm of rain/snow
    val precipitationChance: Int, // %
    val windSpeedAvg: Double, // km/h
    val windSpeedMax: Double, // peak gust km/h
    val windDirection: Double, // degrees 0-360
    val windDirectionText: String, // "NE", "SW", etc.
    val humidityAvg: Int, // %
    val pressureHpa: Int // hPa
)

enum class DateRangePreset(val label: String, val shortLabel: String, val days: Int) {
    LAST_7_DAYS("Last 7 Days (Week)", "Last Week", 7),
    LAST_14_DAYS("Last 14 Days", "Last 14D", 14),
    LAST_30_DAYS("Last 30 Days (Month)", "Last Month", 30),
    SPECIFIC_DATE("Specific Date", "Single Day", 1)
}

enum class MetricType(val label: String) {
    TEMPERATURE("Temperature"),
    PRECIPITATION("Precipitation"),
    WIND("Wind & Gusts")
}

data class HistoricalSummary(
    val avgTemp: Double,
    val maxTemp: Double,
    val minTemp: Double,
    val totalPrecipitation: Double,
    val rainyDaysCount: Int,
    val avgWindSpeed: Double,
    val maxWindGust: Double,
    val predominantCondition: String
)
