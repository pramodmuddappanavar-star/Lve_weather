package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

class WeatherRepository {

    var isSatelliteNetworkOnline: Boolean = true
        private set

    var isPythonBackendConnected: Boolean
        get() = isSatelliteNetworkOnline
        set(value) {
            isSatelliteNetworkOnline = value
        }

    val staticCities = listOf(
        CityWeather(
            name = "Tokyo", country = "Japan", region = "Asia",
            latitude = 35.6762, longitude = 139.6503,
            temperature = 22.5, minTemp = 18.0, maxTemp = 25.0, feelsLike = 22.8, dewPoint = 13.5,
            condition = "Clear", humidity = 58, windSpeed = 12.0, windDirection = 140.0,
            pressure = 1014, uvIndex = 6, visibility = 10.0, satelliteCloudCover = 12,
            liveSatelliteName = "HIMAWARI-9", satelliteSignalStrength = -78,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 418, airQualityIndex = 2,
            situationDescription = "High-pressure anticyclone dominating the Kanto basin. Gentle sea breeze from Tokyo Bay maintaining crisp visibility and pleasant autumnal skies.",
            imageUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?q=80&w=800",
            localTime = "21:30 JST",
            hourlyForecast = listOf(
                HourlySituation("Now", 22.5, "Clear", 0, 12.0),
                HourlySituation("+2h", 21.0, "Clear", 0, 10.0),
                HourlySituation("+4h", 19.5, "Partly Cloudy", 5, 8.5),
                HourlySituation("+6h", 18.2, "Clear", 0, 7.0),
                HourlySituation("+8h", 18.0, "Sunny", 0, 9.0)
            )
        ),
        CityWeather(
            name = "Paris", country = "France", region = "Europe",
            latitude = 48.8566, longitude = 2.3522,
            temperature = 16.4, minTemp = 12.0, maxTemp = 18.5, feelsLike = 16.0, dewPoint = 10.2,
            condition = "Partly Cloudy", humidity = 68, windSpeed = 15.0, windDirection = 210.0,
            pressure = 1012, uvIndex = 4, visibility = 10.0, satelliteCloudCover = 42,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -81,
            orbitalHeight = 850, orbitalVelocity = 7.39, carbonDioxidePpm = 419, airQualityIndex = 2,
            situationDescription = "Atlantic mild maritime trough passing over Île-de-France. Scattered altocumulus clouds with pleasant breaks of golden afternoon sunshine.",
            imageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=800",
            localTime = "14:30 CEST",
            hourlyForecast = listOf(
                HourlySituation("Now", 16.4, "Partly Cloudy", 10, 15.0),
                HourlySituation("+2h", 17.5, "Partly Cloudy", 15, 16.0),
                HourlySituation("+4h", 16.0, "Cloudy", 20, 14.0),
                HourlySituation("+6h", 14.2, "Clear", 5, 11.0),
                HourlySituation("+8h", 13.0, "Clear", 0, 9.5)
            )
        ),
        CityWeather(
            name = "New York", country = "United States", region = "North America",
            latitude = 40.7128, longitude = -74.0060,
            temperature = 17.8, minTemp = 14.5, maxTemp = 21.0, feelsLike = 17.2, dewPoint = 12.0,
            condition = "Cloudy", humidity = 72, windSpeed = 18.5, windDirection = 220.0,
            pressure = 1009, uvIndex = 4, visibility = 8.5, satelliteCloudCover = 65,
            liveSatelliteName = "GOES-16", satelliteSignalStrength = -82,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 422, airQualityIndex = 3,
            situationDescription = "Warm front extending from the Ohio Valley bringing dense stratocumulus ceiling across the Tri-State area. Mild southerly gusting winds along the Hudson.",
            imageUrl = "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=800",
            localTime = "08:30 EDT",
            hourlyForecast = listOf(
                HourlySituation("Now", 17.8, "Cloudy", 25, 18.5),
                HourlySituation("+2h", 19.2, "Overcast", 35, 20.0),
                HourlySituation("+4h", 20.5, "Light Rain", 60, 22.0),
                HourlySituation("+6h", 18.0, "Showers", 70, 19.0),
                HourlySituation("+8h", 16.5, "Cloudy", 30, 15.0)
            )
        ),
        CityWeather(
            name = "London", country = "United Kingdom", region = "Europe",
            latitude = 51.5074, longitude = -0.1278,
            temperature = 14.2, minTemp = 11.0, maxTemp = 16.5, feelsLike = 13.5, dewPoint = 12.1,
            condition = "Rainy", humidity = 88, windSpeed = 24.0, windDirection = 270.0,
            pressure = 1005, uvIndex = 2, visibility = 6.0, satelliteCloudCover = 90,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -85,
            orbitalHeight = 850, orbitalVelocity = 7.39, carbonDioxidePpm = 419, airQualityIndex = 1,
            situationDescription = "North Atlantic depression centered over the Irish Sea pushing continuous rain bands over the Thames basin. Gusty westerlies and saturated atmospheric boundary layer.",
            imageUrl = "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=800",
            localTime = "13:30 BST",
            hourlyForecast = listOf(
                HourlySituation("Now", 14.2, "Rainy", 85, 24.0),
                HourlySituation("+2h", 14.8, "Drizzle", 65, 22.0),
                HourlySituation("+4h", 15.1, "Overcast", 40, 20.0),
                HourlySituation("+6h", 13.8, "Light Rain", 50, 18.0),
                HourlySituation("+8h", 12.5, "Cloudy", 25, 16.0)
            )
        ),
        CityWeather(
            name = "Dubai", country = "United Arab Emirates", region = "Middle East",
            latitude = 25.2048, longitude = 55.2708,
            temperature = 34.8, minTemp = 28.0, maxTemp = 38.0, feelsLike = 37.5, dewPoint = 18.0,
            condition = "Clear", humidity = 42, windSpeed = 16.0, windDirection = 310.0,
            pressure = 1011, uvIndex = 9, visibility = 12.0, satelliteCloudCover = 2,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -79,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 420, airQualityIndex = 3,
            situationDescription = "Subtropical desert high pressure ridge delivering dry, radiant solar heating across the Arabian Gulf coastline. Clear skies with moderate sea-breeze thermal inversion.",
            imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?q=80&w=800",
            localTime = "16:30 GST",
            hourlyForecast = listOf(
                HourlySituation("Now", 34.8, "Clear", 0, 16.0),
                HourlySituation("+2h", 32.5, "Clear", 0, 14.0),
                HourlySituation("+4h", 30.0, "Clear", 0, 12.0),
                HourlySituation("+6h", 29.0, "Clear", 0, 10.0),
                HourlySituation("+8h", 28.2, "Clear", 0, 9.0)
            )
        ),
        CityWeather(
            name = "Sydney", country = "Australia", region = "Oceania",
            latitude = -33.8688, longitude = 151.2093,
            temperature = 19.5, minTemp = 15.0, maxTemp = 23.0, feelsLike = 19.5, dewPoint = 11.2,
            condition = "Clear", humidity = 60, windSpeed = 9.0, windDirection = 60.0,
            pressure = 1018, uvIndex = 5, visibility = 12.0, satelliteCloudCover = 5,
            liveSatelliteName = "SENTINEL-6A", satelliteSignalStrength = -74,
            orbitalHeight = 1336, orbitalVelocity = 7.12, carbonDioxidePpm = 416, airQualityIndex = 1,
            situationDescription = "Tasman Sea coastal anticyclone providing clear sapphire blue skies over Port Jackson. Gentle onshore northeasterly breezes and ideal coastal comfort.",
            imageUrl = "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=800",
            localTime = "22:30 AEST",
            hourlyForecast = listOf(
                HourlySituation("Now", 19.5, "Clear", 0, 9.0),
                HourlySituation("+2h", 18.0, "Clear", 0, 8.0),
                HourlySituation("+4h", 16.5, "Clear", 0, 7.0),
                HourlySituation("+6h", 15.5, "Clear", 0, 6.0),
                HourlySituation("+8h", 16.0, "Sunny", 0, 8.5)
            )
        ),
        CityWeather(
            name = "Cairo", country = "Egypt", region = "Africa",
            latitude = 30.0444, longitude = 31.2357,
            temperature = 31.0, minTemp = 24.0, maxTemp = 36.0, feelsLike = 30.2, dewPoint = 12.0,
            condition = "Clear", humidity = 35, windSpeed = 15.0, windDirection = 340.0,
            pressure = 1012, uvIndex = 9, visibility = 15.0, satelliteCloudCover = 0,
            liveSatelliteName = "NOAA-20", satelliteSignalStrength = -80,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 421, airQualityIndex = 4,
            situationDescription = "Continental Saharan dry air mass dominant over the Nile Delta. Intense direct solar insolation, virtually zero cloud formation, and northern breeze.",
            imageUrl = "https://images.unsplash.com/photo-1572252009286-268acec5ca0a?q=80&w=800",
            localTime = "14:30 EEST",
            hourlyForecast = listOf(
                HourlySituation("Now", 31.0, "Clear", 0, 15.0),
                HourlySituation("+2h", 33.5, "Sunny", 0, 16.0),
                HourlySituation("+4h", 30.0, "Clear", 0, 12.0),
                HourlySituation("+6h", 27.5, "Clear", 0, 10.0),
                HourlySituation("+8h", 25.0, "Clear", 0, 8.0)
            )
        ),
        CityWeather(
            name = "Rio de Janeiro", country = "Brazil", region = "South America",
            latitude = -22.9068, longitude = -43.1729,
            temperature = 26.3, minTemp = 22.0, maxTemp = 29.5, feelsLike = 28.5, dewPoint = 21.0,
            condition = "Stormy", humidity = 80, windSpeed = 30.0, windDirection = 180.0,
            pressure = 1002, uvIndex = 7, visibility = 5.0, satelliteCloudCover = 85,
            liveSatelliteName = "GOES-18", satelliteSignalStrength = -88,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 417, airQualityIndex = 2,
            situationDescription = "Cold oceanic polar front arriving from the South Atlantic, generating squall lines and cumulonimbus thunderheads over Guanabara Bay and Corcovado.",
            imageUrl = "https://images.unsplash.com/photo-1483729558449-99ef09a8c325?q=80&w=800",
            localTime = "09:30 BRT",
            hourlyForecast = listOf(
                HourlySituation("Now", 26.3, "Stormy", 85, 30.0),
                HourlySituation("+2h", 25.0, "Heavy Rain", 90, 28.0),
                HourlySituation("+4h", 24.5, "Showers", 60, 22.0),
                HourlySituation("+6h", 24.0, "Cloudy", 30, 18.0),
                HourlySituation("+8h", 23.5, "Overcast", 20, 14.0)
            )
        ),
        CityWeather(
            name = "Singapore", country = "Singapore", region = "Asia",
            latitude = 1.3521, longitude = 103.8198,
            temperature = 29.1, minTemp = 26.0, maxTemp = 32.0, feelsLike = 34.0, dewPoint = 24.5,
            condition = "Rainy", humidity = 82, windSpeed = 10.5, windDirection = 120.0,
            pressure = 1010, uvIndex = 8, visibility = 9.0, satelliteCloudCover = 78,
            liveSatelliteName = "SUOMI-NPP", satelliteSignalStrength = -81,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 425, airQualityIndex = 3,
            situationDescription = "Intertropical Convergence Zone (ITCZ) convective cells triggering tropical afternoon monsoon showers across the island strait. High humidity and convective updrafts.",
            imageUrl = "https://images.unsplash.com/photo-1525625293386-3f8f99389edd?q=80&w=800",
            localTime = "20:30 SGT",
            hourlyForecast = listOf(
                HourlySituation("Now", 29.1, "Rainy", 75, 10.5),
                HourlySituation("+2h", 28.0, "Showers", 60, 9.0),
                HourlySituation("+4h", 27.5, "Cloudy", 30, 8.0),
                HourlySituation("+6h", 27.0, "Partly Cloudy", 15, 6.0),
                HourlySituation("+8h", 26.8, "Clear", 10, 5.0)
            )
        ),
        CityWeather(
            name = "Reykjavik", country = "Iceland", region = "Polar",
            latitude = 64.1466, longitude = -21.9426,
            temperature = 3.5, minTemp = 1.0, maxTemp = 6.0, feelsLike = -0.5, dewPoint = 0.5,
            condition = "Aurora", humidity = 75, windSpeed = 22.0, windDirection = 90.0,
            pressure = 998, uvIndex = 1, visibility = 10.0, satelliteCloudCover = 40,
            liveSatelliteName = "AQUA", satelliteSignalStrength = -79,
            orbitalHeight = 705, orbitalVelocity = 7.48, carbonDioxidePpm = 415, airQualityIndex = 1,
            situationDescription = "Subarctic clear window between North Atlantic cyclonic systems. Elevated solar wind speed (K-index 5) producing vibrant green atmospheric aurora borealis ribbons overhead.",
            imageUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=800",
            localTime = "12:30 GMT",
            hourlyForecast = listOf(
                HourlySituation("Now", 3.5, "Aurora", 10, 22.0),
                HourlySituation("+2h", 4.0, "Clear Skies", 10, 20.0),
                HourlySituation("+4h", 4.5, "Cloudy", 25, 24.0),
                HourlySituation("+6h", 3.0, "Flurries", 45, 25.0),
                HourlySituation("+8h", 2.0, "Snow", 60, 26.0)
            )
        ),
        CityWeather(
            name = "Anchorage", country = "United States (Alaska)", region = "Polar",
            latitude = 61.2181, longitude = -149.9003,
            temperature = -2.4, minTemp = -7.0, maxTemp = 0.5, feelsLike = -7.5, dewPoint = -5.0,
            condition = "Blizzard", humidity = 85, windSpeed = 35.0, windDirection = 45.0,
            pressure = 994, uvIndex = 0, visibility = 1.2, satelliteCloudCover = 98,
            liveSatelliteName = "TERRA", satelliteSignalStrength = -86,
            orbitalHeight = 705, orbitalVelocity = 7.48, carbonDioxidePpm = 416, airQualityIndex = 1,
            situationDescription = "Aleutian low-pressure system pumping moisture into Cook Inlet, producing heavy snow squalls and reduced visibility along the Chugach Mountain foothills.",
            imageUrl = "https://images.unsplash.com/photo-1516431883659-655d41c09bf9?q=80&w=800",
            localTime = "04:30 AKDT",
            hourlyForecast = listOf(
                HourlySituation("Now", -2.4, "Blizzard", 95, 35.0),
                HourlySituation("+2h", -2.0, "Heavy Snow", 90, 32.0),
                HourlySituation("+4h", -1.5, "Snow Showers", 70, 26.0),
                HourlySituation("+6h", -3.0, "Overcast", 40, 20.0),
                HourlySituation("+8h", -4.5, "Cloudy", 20, 15.0)
            )
        ),
        CityWeather(
            name = "Nairobi", country = "Kenya", region = "Africa",
            latitude = -1.2921, longitude = 36.8219,
            temperature = 23.0, minTemp = 16.0, maxTemp = 26.5, feelsLike = 23.0, dewPoint = 14.0,
            condition = "Cloudy", humidity = 62, windSpeed = 11.0, windDirection = 150.0,
            pressure = 1015, uvIndex = 7, visibility = 11.0, satelliteCloudCover = 50,
            liveSatelliteName = "METEOR-M2", satelliteSignalStrength = -77,
            orbitalHeight = 830, orbitalVelocity = 7.41, carbonDioxidePpm = 418, airQualityIndex = 2,
            situationDescription = "Equatorial highland climate moderated by 1,795m altitude. Mild southeasterly trades bringing pleasant cumulus clouds across the Great Rift Valley escarpment.",
            imageUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?q=80&w=800",
            localTime = "15:30 EAT",
            hourlyForecast = listOf(
                HourlySituation("Now", 23.0, "Cloudy", 20, 11.0),
                HourlySituation("+2h", 25.0, "Partly Cloudy", 15, 12.0),
                HourlySituation("+4h", 22.0, "Clear", 5, 10.0),
                HourlySituation("+6h", 19.5, "Clear", 0, 8.0),
                HourlySituation("+8h", 18.0, "Clear", 0, 7.0)
            )
        ),
        CityWeather(
            name = "Berlin", country = "Germany", region = "Europe",
            latitude = 52.5200, longitude = 13.4050,
            temperature = 15.2, minTemp = 9.5, maxTemp = 17.0, feelsLike = 14.8, dewPoint = 9.0,
            condition = "Cloudy", humidity = 64, windSpeed = 14.0, windDirection = 260.0,
            pressure = 1016, uvIndex = 3, visibility = 10.0, satelliteCloudCover = 55,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -82,
            orbitalHeight = 850, orbitalVelocity = 7.39, carbonDioxidePpm = 421, airQualityIndex = 2,
            situationDescription = "Central European transitional zone under the influence of a high-latitude Scandinavian ridge. Crisp, calm autumnal atmosphere with scattered stratus clouds.",
            imageUrl = "https://images.unsplash.com/photo-1560969184-10fe8719e047?q=80&w=800",
            localTime = "14:30 CEST",
            hourlyForecast = listOf(
                HourlySituation("Now", 15.2, "Cloudy", 15, 14.0),
                HourlySituation("+2h", 16.5, "Partly Cloudy", 10, 15.0),
                HourlySituation("+4h", 15.0, "Cloudy", 20, 12.0),
                HourlySituation("+6h", 13.0, "Clear", 5, 9.0),
                HourlySituation("+8h", 11.5, "Clear", 0, 7.0)
            )
        ),
        CityWeather(
            name = "Rome", country = "Italy", region = "Europe",
            latitude = 41.9028, longitude = 12.4964,
            temperature = 23.5, minTemp = 16.0, maxTemp = 26.0, feelsLike = 24.0, dewPoint = 15.2,
            condition = "Clear", humidity = 55, windSpeed = 10.0, windDirection = 190.0,
            pressure = 1017, uvIndex = 6, visibility = 12.0, satelliteCloudCover = 10,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -78,
            orbitalHeight = 850, orbitalVelocity = 7.39, carbonDioxidePpm = 419, airQualityIndex = 2,
            situationDescription = "Mediterranean anticyclone giving sunny, golden illumination to the Tiber valley. Gentle Tyrrhenian sea-breeze ('Ponentino') keeping temperatures comfortable.",
            imageUrl = "https://images.unsplash.com/photo-1552832230-c0197dd311b5?q=80&w=800",
            localTime = "14:30 CEST",
            hourlyForecast = listOf(
                HourlySituation("Now", 23.5, "Clear", 0, 10.0),
                HourlySituation("+2h", 25.0, "Sunny", 0, 11.0),
                HourlySituation("+4h", 23.0, "Clear", 0, 9.0),
                HourlySituation("+6h", 20.0, "Clear", 0, 7.0),
                HourlySituation("+8h", 18.5, "Clear", 0, 6.0)
            )
        ),
        CityWeather(
            name = "Mumbai", country = "India", region = "Asia",
            latitude = 19.0760, longitude = 72.8777,
            temperature = 31.8, minTemp = 26.5, maxTemp = 34.0, feelsLike = 36.5, dewPoint = 23.0,
            condition = "Partly Cloudy", humidity = 74, windSpeed = 14.5, windDirection = 270.0,
            pressure = 1008, uvIndex = 8, visibility = 8.0, satelliteCloudCover = 38,
            liveSatelliteName = "INSAT-3D", satelliteSignalStrength = -80,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 426, airQualityIndex = 4,
            situationDescription = "Arabian Sea marine boundary layer bringing humid westerly winds to the Konkan coast. Convective cloud development along the Western Ghats range.",
            imageUrl = "https://images.unsplash.com/photo-1570168007204-dfb528c6958f?q=80&w=800",
            localTime = "18:00 IST",
            hourlyForecast = listOf(
                HourlySituation("Now", 31.8, "Partly Cloudy", 15, 14.5),
                HourlySituation("+2h", 30.5, "Partly Cloudy", 20, 13.0),
                HourlySituation("+4h", 29.0, "Overcast", 30, 11.0),
                HourlySituation("+6h", 28.2, "Cloudy", 25, 9.5),
                HourlySituation("+8h", 27.5, "Hazy", 15, 8.0)
            )
        ),
        CityWeather(
            name = "Cape Town", country = "South Africa", region = "Africa",
            latitude = -33.9249, longitude = 18.4241,
            temperature = 18.2, minTemp = 13.0, maxTemp = 21.5, feelsLike = 17.8, dewPoint = 11.0,
            condition = "Partly Cloudy", humidity = 65, windSpeed = 26.0, windDirection = 160.0,
            pressure = 1020, uvIndex = 6, visibility = 12.0, satelliteCloudCover = 25,
            liveSatelliteName = "METEOSAT-11", satelliteSignalStrength = -79,
            orbitalHeight = 850, orbitalVelocity = 7.39, carbonDioxidePpm = 416, airQualityIndex = 1,
            situationDescription = "South Atlantic high ridge funneling fresh southeasterly wind ('Cape Doctor') across False Bay, creating the iconic orographic 'tablecloth' cloud over Table Mountain.",
            imageUrl = "https://images.unsplash.com/photo-1580618672591-eb180b1a973f?q=80&w=800",
            localTime = "14:30 SAST",
            hourlyForecast = listOf(
                HourlySituation("Now", 18.2, "Partly Cloudy", 10, 26.0),
                HourlySituation("+2h", 19.5, "Sunny", 5, 28.0),
                HourlySituation("+4h", 17.5, "Clear", 0, 24.0),
                HourlySituation("+6h", 15.5, "Clear", 0, 20.0),
                HourlySituation("+8h", 14.0, "Clear", 0, 16.0)
            )
        ),
        CityWeather(
            name = "Toronto", country = "Canada", region = "North America",
            latitude = 43.6532, longitude = -79.3832,
            temperature = 14.8, minTemp = 9.0, maxTemp = 17.5, feelsLike = 14.0, dewPoint = 8.5,
            condition = "Clear", humidity = 56, windSpeed = 16.0, windDirection = 310.0,
            pressure = 1017, uvIndex = 4, visibility = 12.0, satelliteCloudCover = 15,
            liveSatelliteName = "GOES-16", satelliteSignalStrength = -81,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 421, airQualityIndex = 2,
            situationDescription = "Post-frontal Canadian polar continental air mass settling over Lake Ontario. Crisp, transparent atmosphere with pristine skyline views across the harbor.",
            imageUrl = "https://images.unsplash.com/photo-1517090504586-fde19ea6066f?q=80&w=800",
            localTime = "08:30 EDT",
            hourlyForecast = listOf(
                HourlySituation("Now", 14.8, "Clear", 0, 16.0),
                HourlySituation("+2h", 16.5, "Sunny", 0, 17.0),
                HourlySituation("+4h", 17.0, "Partly Cloudy", 10, 15.0),
                HourlySituation("+6h", 15.0, "Clear", 0, 12.0),
                HourlySituation("+8h", 12.5, "Clear", 0, 9.0)
            )
        ),
        CityWeather(
            name = "Buenos Aires", country = "Argentina", region = "South America",
            latitude = -34.6037, longitude = -58.3816,
            temperature = 21.0, minTemp = 16.0, maxTemp = 24.5, feelsLike = 21.0, dewPoint = 13.0,
            condition = "Clear", humidity = 58, windSpeed = 15.0, windDirection = 110.0,
            pressure = 1015, uvIndex = 6, visibility = 10.0, satelliteCloudCover = 8,
            liveSatelliteName = "GOES-16", satelliteSignalStrength = -82,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 417, airQualityIndex = 2,
            situationDescription = "Mild maritime Pampean breeze blowing off the Río de la Plata estuary. Stable boundary layer with sunny spring conditions throughout the metropolitan district.",
            imageUrl = "https://images.unsplash.com/photo-1589909202802-8f4aadce1849?q=80&w=800",
            localTime = "09:30 ART",
            hourlyForecast = listOf(
                HourlySituation("Now", 21.0, "Clear", 0, 15.0),
                HourlySituation("+2h", 23.0, "Sunny", 0, 16.0),
                HourlySituation("+4h", 24.0, "Clear", 0, 14.0),
                HourlySituation("+6h", 21.5, "Clear", 0, 11.0),
                HourlySituation("+8h", 19.0, "Clear", 0, 9.0)
            )
        ),
        CityWeather(
            name = "Honolulu", country = "United States (Hawaii)", region = "Oceania",
            latitude = 21.3069, longitude = -157.8583,
            temperature = 28.5, minTemp = 24.0, maxTemp = 30.5, feelsLike = 30.0, dewPoint = 20.0,
            condition = "Clear", humidity = 62, windSpeed = 22.0, windDirection = 70.0,
            pressure = 1016, uvIndex = 9, visibility = 15.0, satelliteCloudCover = 20,
            liveSatelliteName = "GOES-18", satelliteSignalStrength = -78,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 421, airQualityIndex = 1,
            situationDescription = "Consistent northeast Pacific trade winds pushing orographic clouds against the Koolau Range. Rainbows over Manoa Valley and sunny tropical beach conditions at Waikiki.",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=800",
            localTime = "02:30 HST",
            hourlyForecast = listOf(
                HourlySituation("Now", 28.5, "Clear", 10, 22.0),
                HourlySituation("+2h", 29.5, "Sunny", 15, 24.0),
                HourlySituation("+4h", 29.0, "Partly Cloudy", 20, 22.0),
                HourlySituation("+6h", 27.5, "Clear", 10, 19.0),
                HourlySituation("+8h", 26.0, "Clear", 5, 17.0)
            )
        ),
        CityWeather(
            name = "Seoul", country = "South Korea", region = "Asia",
            latitude = 37.5665, longitude = 126.9780,
            temperature = 19.2, minTemp = 13.0, maxTemp = 22.0, feelsLike = 18.8, dewPoint = 10.0,
            condition = "Clear", humidity = 52, windSpeed = 10.0, windDirection = 320.0,
            pressure = 1018, uvIndex = 5, visibility = 12.0, satelliteCloudCover = 10,
            liveSatelliteName = "GEO-KOMPSAT-2A", satelliteSignalStrength = -76,
            orbitalHeight = 35786, orbitalVelocity = 3.07, carbonDioxidePpm = 423, airQualityIndex = 2,
            situationDescription = "High pressure migrating from northern China producing pristine autumn skies ('Cheongmyeong') over the Han River basin. Gentle northerly dry airflow.",
            imageUrl = "https://images.unsplash.com/photo-1538485399081-7191377e8241?q=80&w=800",
            localTime = "21:30 KST",
            hourlyForecast = listOf(
                HourlySituation("Now", 19.2, "Clear", 0, 10.0),
                HourlySituation("+2h", 17.5, "Clear", 0, 9.0),
                HourlySituation("+4h", 16.0, "Clear", 0, 8.0),
                HourlySituation("+6h", 14.5, "Clear", 0, 7.0),
                HourlySituation("+8h", 14.0, "Sunny", 0, 8.5)
            )
        ),
        CityWeather(
            name = "Mexico City", country = "Mexico", region = "North America",
            latitude = 19.4326, longitude = -99.1332,
            temperature = 22.0, minTemp = 12.5, maxTemp = 25.0, feelsLike = 21.5, dewPoint = 11.0,
            condition = "Partly Cloudy", humidity = 50, windSpeed = 12.0, windDirection = 40.0,
            pressure = 1022, uvIndex = 8, visibility = 10.0, satelliteCloudCover = 35,
            liveSatelliteName = "GOES-16", satelliteSignalStrength = -80,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 424, airQualityIndex = 3,
            situationDescription = "High-altitude Anahuac Valley plateau (2,240m) experiencing sunny morning warming transitioning to afternoon thermal cumulus development around Popocatépetl volcano.",
            imageUrl = "https://images.unsplash.com/photo-1518638150340-f706e86654de?q=80&w=800",
            localTime = "06:30 CST",
            hourlyForecast = listOf(
                HourlySituation("Now", 22.0, "Partly Cloudy", 15, 12.0),
                HourlySituation("+2h", 24.5, "Sunny", 10, 14.0),
                HourlySituation("+4h", 23.0, "Afternoon Cloud", 30, 15.0),
                HourlySituation("+6h", 19.5, "Light Rain", 40, 12.0),
                HourlySituation("+8h", 16.0, "Clear", 10, 8.0)
            )
        ),
        CityWeather(
            name = "McMurdo Station", country = "Antarctica", region = "Polar",
            latitude = -77.8419, longitude = 166.6863,
            temperature = -18.5, minTemp = -26.0, maxTemp = -14.0, feelsLike = -28.0, dewPoint = -22.0,
            condition = "Blizzard", humidity = 70, windSpeed = 48.0, windDirection = 210.0,
            pressure = 988, uvIndex = 0, visibility = 0.8, satelliteCloudCover = 95,
            liveSatelliteName = "NOAA-20", satelliteSignalStrength = -90,
            orbitalHeight = 824, orbitalVelocity = 7.42, carbonDioxidePpm = 412, airQualityIndex = 1,
            situationDescription = "Catabatic polar winds barreling down from the polar plateau onto the Ross Ice Shelf. Severe wind chill and zero ground contrast in freezing drift.",
            imageUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=800",
            localTime = "00:30 NZST",
            hourlyForecast = listOf(
                HourlySituation("Now", -18.5, "Blizzard", 90, 48.0),
                HourlySituation("+2h", -19.0, "Severe Drift", 85, 45.0),
                HourlySituation("+4h", -20.5, "Freezing Wind", 70, 40.0),
                HourlySituation("+6h", -22.0, "Ice Fog", 40, 32.0),
                HourlySituation("+8h", -23.0, "Overcast", 20, 28.0)
            )
        )
    )

    fun getWeatherDataFlow(): Flow<List<CityWeather>> = flow {
        var dynamicCities = staticCities.map { it.copy() }

        while (true) {
            dynamicCities = dynamicCities.map { city ->
                val tempDelta = Random.nextDouble(-0.2, 0.2)
                val newTemp = ((city.temperature + tempDelta) * 10.0).roundToInt() / 10.0
                val signalFluctuation = Random.nextInt(-2, 2)
                val rawSignal = city.satelliteSignalStrength + signalFluctuation
                val newSignal = rawSignal.coerceIn(-95, -70)

                val satelliteCloudCoverDelta = Random.nextInt(-2, 2)
                val newCloud = (city.satelliteCloudCover + satelliteCloudCoverDelta).coerceIn(0, 100)

                city.copy(
                    temperature = newTemp,
                    satelliteSignalStrength = newSignal,
                    satelliteCloudCover = newCloud
                )
            }
            emit(dynamicCities)
            delay(4000)
        }
    }

    fun setPythonBackendState(isConnected: Boolean) {
        isSatelliteNetworkOnline = isConnected
    }

    /**
     * Search any world location by city name, country, or geographical region.
     */
    fun searchLocations(query: String): List<CityWeather> {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return staticCities

        return staticCities.filter {
            it.name.lowercase(Locale.ROOT).contains(q) ||
            it.country.lowercase(Locale.ROOT).contains(q) ||
            it.region.lowercase(Locale.ROOT).contains(q) ||
            it.condition.lowercase(Locale.ROOT).contains(q)
        }
    }

    /**
     * Synthesizes realistic meteorological conditions for any custom GPS coordinate on Earth!
     */
    fun findByCoordinates(lat: Double, lon: Double): CityWeather {
        val clampedLat = lat.coerceIn(-90.0, 90.0)
        val clampedLon = ((lon + 180.0) % 360.0) - 180.0

        // Find nearest known city for climate correlation
        val nearest = staticCities.minByOrNull { city ->
            val dLat = city.latitude - clampedLat
            val dLon = city.longitude - clampedLon
            (dLat * dLat) + (dLon * dLon)
        } ?: staticCities.first()

        // Temperature varies by latitude (warmer at equator, colder at poles)
        val latFactor = Math.cos(Math.toRadians(clampedLat))
        val estTemp = ((-20.0 + (latFactor * 48.0)) * 10.0).roundToInt() / 10.0

        val regionName = when {
            clampedLat > 60.0 || clampedLat < -60.0 -> "Polar"
            clampedLon in -170.0..-30.0 && clampedLat >= 15.0 -> "North America"
            clampedLon in -90.0..-30.0 && clampedLat < 15.0 -> "South America"
            clampedLon in -20.0..40.0 && clampedLat in 35.0..70.0 -> "Europe"
            clampedLon in -20.0..55.0 && clampedLat in -35.0..35.0 -> "Africa"
            clampedLon in 40.0..150.0 && clampedLat in 0.0..70.0 -> "Asia"
            clampedLon in 110.0..180.0 && clampedLat in -50.0..0.0 -> "Oceania"
            else -> "Global Marine Basin"
        }

        val condition = when {
            estTemp < 0.0 -> "Subzero Snow"
            latFactor > 0.8 && nearest.humidity > 70 -> "Tropical Rain"
            latFactor > 0.6 -> "Partly Cloudy"
            else -> "Clear Skies"
        }

        return CityWeather(
            name = String.format(Locale.US, "Coord (%.2f°, %.2f°)", clampedLat, clampedLon),
            country = regionName,
            region = regionName,
            latitude = clampedLat,
            longitude = clampedLon,
            temperature = estTemp,
            minTemp = estTemp - 4.0,
            maxTemp = estTemp + 5.0,
            feelsLike = estTemp,
            dewPoint = estTemp - 5.0,
            condition = condition,
            humidity = nearest.humidity,
            windSpeed = nearest.windSpeed,
            windDirection = (Math.abs(clampedLon * 2) % 360),
            pressure = nearest.pressure,
            uvIndex = if (clampedLat in -40.0..40.0) 8 else 3,
            visibility = 10.0,
            satelliteCloudCover = nearest.satelliteCloudCover,
            liveSatelliteName = nearest.liveSatelliteName,
            satelliteSignalStrength = nearest.satelliteSignalStrength,
            orbitalHeight = nearest.orbitalHeight,
            orbitalVelocity = nearest.orbitalVelocity,
            carbonDioxidePpm = 419,
            airQualityIndex = 2,
            situationDescription = "Atmospheric station telemetry derived from satellite orbital scan pass at coordinates ${String.format(Locale.US, "%.3f°N, %.3f°E", clampedLat, clampedLon)}.",
            imageUrl = nearest.imageUrl,
            localTime = "Active Scan"
        )
    }

    fun getSatellitesFlow(): Flow<List<OrbitSatPoint>> = flow {
        val satPoints = listOf(
            OrbitSatPoint("sat_1", "SENTINEL-6", 0.0, -180.0, androidx.compose.ui.graphics.Color(0xFF00FFCC), "🛰️"),
            OrbitSatPoint("sat_2", "GOES-16", 45.0, -90.0, androidx.compose.ui.graphics.Color(0xFFFFAA00), "📡"),
            OrbitSatPoint("sat_3", "METEOSAT", -30.0, 0.0, androidx.compose.ui.graphics.Color(0xFFFF00CC), "🛰️"),
            OrbitSatPoint("sat_4", "HIMAWARI", 20.0, 140.0, androidx.compose.ui.graphics.Color(0xFF00AAFF), "📡"),
            OrbitSatPoint("sat_5", "NOAA-20", -50.0, 60.0, androidx.compose.ui.graphics.Color(0xFF00FF57), "🛰️")
        )

        var frame = 0L
        while (true) {
            val movedPoints = satPoints.map { sat ->
                val velocityFactor = when (sat.id) {
                    "sat_1" -> 1.8
                    "sat_2" -> 1.2
                    "sat_5" -> 1.5
                    else -> 0.8
                }
                val startLongitude = sat.lon
                val currentLong = (startLongitude + (frame * velocityFactor)) % 360
                val normalizedLong = if (currentLong > 180) currentLong - 360 else currentLong

                val latPeriod = if (sat.id == "sat_3") 120.0 else 240.0
                val maxLat = if (sat.id == "sat_3") 40.0 else 60.0
                val latVal = Math.sin(frame.toDouble() / latPeriod) * maxLat

                sat.copy(lat = latVal, lon = normalizedLong)
            }
            emit(movedPoints)
            frame++
            delay(1500)
        }
    }

    fun getHistoricalWeather(cityName: String, daysCount: Int): List<HistoricalWeatherDay> {
        val city = staticCities.firstOrNull { it.name.equals(cityName, ignoreCase = true) }
            ?: staticCities.first()

        val baseTemp = city.temperature
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

        val cityHash = city.name.hashCode()
        val records = mutableListOf<HistoricalWeatherDay>()

        for (i in (daysCount - 1) downTo 0) {
            val date = today.minusDays(i.toLong())
            val daySeed = cityHash + (date.toEpochDay() * 31).toInt()
            val rng = kotlin.random.Random(daySeed)

            val wave = Math.sin((i.toDouble() / 5.0) + (cityHash % 7)) * 3.5
            val dailyNoise = (rng.nextDouble() - 0.5) * 2.8
            val avgTemp = ((baseTemp + wave + dailyNoise) * 10.0).roundToInt() / 10.0
            val diurnalRange = 4.0 + (rng.nextDouble() * 4.5)
            val maxTemp = ((avgTemp + (diurnalRange * 0.55)) * 10.0).roundToInt() / 10.0
            val minTemp = ((avgTemp - (diurnalRange * 0.45)) * 10.0).roundToInt() / 10.0

            val climateRainFactor = when (city.name) {
                "London", "Singapore", "Rio de Janeiro" -> 0.45
                "Cairo", "Dubai" -> 0.04
                "Anchorage", "McMurdo Station" -> 0.40
                "Tokyo", "Paris" -> 0.28
                "New York" -> 0.30
                else -> 0.22
            }

            val hasRain = rng.nextDouble() < climateRainFactor
            val precipMm = if (hasRain) {
                val baseMm = 1.0 + (rng.nextDouble() * 18.0)
                (baseMm * 10.0).roundToInt() / 10.0
            } else 0.0

            val precipChance = if (hasRain) rng.nextInt(60, 95) else rng.nextInt(5, 30)

            val baseWind = city.windSpeed
            val windNoise = (rng.nextDouble() - 0.3) * 10.0
            val windSpeedAvg = ((baseWind + windNoise).coerceAtLeast(3.0) * 10.0).roundToInt() / 10.0
            val gustMultiplier = 1.35 + (rng.nextDouble() * 0.45)
            val windSpeedMax = ((windSpeedAvg * gustMultiplier) * 10.0).roundToInt() / 10.0
            val windDirDeg = ((city.windDirection + (rng.nextInt(-45, 45) + 360)) % 360).toDouble()
            val windDirText = getCompassDirection(windDirDeg)

            val condition = when {
                precipMm > 12.0 && baseTemp <= 1.0 -> "Blizzard & Snow"
                precipMm > 14.0 -> "Heavy Thunderstorm"
                precipMm > 5.0 -> "Rain Showers"
                precipMm > 0.5 -> "Light Drizzle"
                precipChance > 40 -> "Overcast Clouds"
                precipChance > 20 -> "Partly Cloudy"
                city.name == "Reykjavik" && rng.nextDouble() < 0.3 -> "Clear & Aurora"
                else -> "Clear Skies"
            }

            val humidity = ((city.humidity + (if (hasRain) 15 else -5) + rng.nextInt(-8, 8)).coerceIn(25, 98))
            val pressure = city.pressure + (if (hasRain) -8 else 4) + rng.nextInt(-3, 3)

            val formatted = if (i == 0) "Today" else if (i == 1) "Yesterday" else date.format(dateFormatter)

            records.add(
                HistoricalWeatherDay(
                    date = date,
                    dateFormatted = formatted,
                    tempMax = maxTemp,
                    tempMin = minTemp,
                    tempAvg = avgTemp,
                    condition = condition,
                    precipitationMm = precipMm,
                    precipitationChance = precipChance,
                    windSpeedAvg = windSpeedAvg,
                    windSpeedMax = windSpeedMax,
                    windDirection = windDirDeg,
                    windDirectionText = windDirText,
                    humidityAvg = humidity,
                    pressureHpa = pressure
                )
            )
        }

        return records
    }

    fun computeSummary(records: List<HistoricalWeatherDay>): HistoricalSummary {
        if (records.isEmpty()) {
            return HistoricalSummary(0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0, "Clear")
        }

        val avgTemp = ((records.map { it.tempAvg }.average()) * 10.0).roundToInt() / 10.0
        val maxTemp = records.maxOfOrNull { it.tempMax } ?: 0.0
        val minTemp = records.minOfOrNull { it.tempMin } ?: 0.0
        val totalPrecip = ((records.sumOf { it.precipitationMm }) * 10.0).roundToInt() / 10.0
        val rainyDays = records.count { it.precipitationMm > 0.0 }
        val avgWind = ((records.map { it.windSpeedAvg }.average()) * 10.0).roundToInt() / 10.0
        val maxGust = records.maxOfOrNull { it.windSpeedMax } ?: 0.0

        val predominant = records.groupingBy { it.condition }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Clear Skies"

        return HistoricalSummary(
            avgTemp = avgTemp,
            maxTemp = maxTemp,
            minTemp = minTemp,
            totalPrecipitation = totalPrecip,
            rainyDaysCount = rainyDays,
            avgWindSpeed = avgWind,
            maxWindGust = maxGust,
            predominantCondition = predominant
        )
    }

    private fun getCompassDirection(deg: Double): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((deg + 22.5) % 360) / 45.0).toInt()
        return directions[index.coerceIn(0, 7)]
    }
}
