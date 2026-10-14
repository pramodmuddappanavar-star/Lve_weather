package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherRepository
import com.example.ui.screens.HistoricalWeatherScreen
import com.example.ui.screens.PlaceWeatherSituationScreen
import com.example.ui.screens.WeatherDashboard
import com.example.ui.screens.WorldRadarDirectoryScreen
import com.example.ui.theme.*

enum class AppScreen {
    LIVE_ORBIT,
    SITUATION,
    WORLD_DATA,
    HISTORICAL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val repository = remember { WeatherRepository() }
                var currentScreen by remember { mutableStateOf(AppScreen.LIVE_ORBIT) }
                var activeCityName by remember { mutableStateOf("Tokyo") }

                // System BackHandler returns to the 3D Live Orbit screen
                BackHandler(enabled = currentScreen != AppScreen.LIVE_ORBIT) {
                    currentScreen = AppScreen.LIVE_ORBIT
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepSpace,
                    bottomBar = {
                        NavigationBar(
                            containerColor = DarkNavy,
                            contentColor = Color.White,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .border(1.dp, GlowingCyan.copy(alpha = 0.15f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.LIVE_ORBIT,
                                onClick = { currentScreen = AppScreen.LIVE_ORBIT },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.LIVE_ORBIT) Icons.Filled.Public else Icons.Outlined.Public,
                                        contentDescription = "Live 3D Earth Globe"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "3D Earth",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentScreen == AppScreen.LIVE_ORBIT) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GlowingCyan,
                                    selectedTextColor = GlowingCyan,
                                    indicatorColor = GlowingCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = SoftGrayText,
                                    unselectedTextColor = SoftGrayText
                                ),
                                modifier = Modifier.testTag("nav_live_orbit")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.SITUATION,
                                onClick = { currentScreen = AppScreen.SITUATION },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.SITUATION) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                                        contentDescription = "Weather Situation & Images"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Situation",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentScreen == AppScreen.SITUATION) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DiagnosticOrange,
                                    selectedTextColor = DiagnosticOrange,
                                    indicatorColor = DiagnosticOrange.copy(alpha = 0.15f),
                                    unselectedIconColor = SoftGrayText,
                                    unselectedTextColor = SoftGrayText
                                ),
                                modifier = Modifier.testTag("nav_weather_situation")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.WORLD_DATA,
                                onClick = { currentScreen = AppScreen.WORLD_DATA },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.WORLD_DATA) Icons.Filled.TravelExplore else Icons.Outlined.TravelExplore,
                                        contentDescription = "All World Data Directory"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "World Data",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentScreen == AppScreen.WORLD_DATA) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RadarGreen,
                                    selectedTextColor = RadarGreen,
                                    indicatorColor = RadarGreen.copy(alpha = 0.15f),
                                    unselectedIconColor = SoftGrayText,
                                    unselectedTextColor = SoftGrayText
                                ),
                                modifier = Modifier.testTag("nav_world_data")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HISTORICAL,
                                onClick = { currentScreen = AppScreen.HISTORICAL },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.HISTORICAL) Icons.Filled.Timeline else Icons.Outlined.Timeline,
                                        contentDescription = "Historical Climate Archive"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "History",
                                        fontSize = 10.sp,
                                        fontWeight = if (currentScreen == AppScreen.HISTORICAL) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RadiantPurple,
                                    selectedTextColor = RadiantPurple,
                                    indicatorColor = RadiantPurple.copy(alpha = 0.15f),
                                    unselectedIconColor = SoftGrayText,
                                    unselectedTextColor = SoftGrayText
                                ),
                                modifier = Modifier.testTag("nav_historical_archive")
                            )
                        }
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { screen ->
                        when (screen) {
                            AppScreen.LIVE_ORBIT -> {
                                WeatherDashboard(
                                    repository = repository,
                                    onNavigateToSituation = { cityName ->
                                        activeCityName = cityName
                                        currentScreen = AppScreen.SITUATION
                                    },
                                    onNavigateToHistory = { cityName ->
                                        activeCityName = cityName
                                        currentScreen = AppScreen.HISTORICAL
                                    }
                                )
                            }
                            AppScreen.SITUATION -> {
                                PlaceWeatherSituationScreen(
                                    repository = repository,
                                    initialCityName = activeCityName,
                                    onNavigateToGlobe = { cityName ->
                                        activeCityName = cityName
                                        currentScreen = AppScreen.LIVE_ORBIT
                                    },
                                    onNavigateToHistory = { cityName ->
                                        activeCityName = cityName
                                        currentScreen = AppScreen.HISTORICAL
                                    }
                                )
                            }
                            AppScreen.WORLD_DATA -> {
                                WorldRadarDirectoryScreen(
                                    repository = repository,
                                    onSelectCity = { city ->
                                        activeCityName = city.name
                                        currentScreen = AppScreen.SITUATION
                                    }
                                )
                            }
                            AppScreen.HISTORICAL -> {
                                HistoricalWeatherScreen(
                                    repository = repository,
                                    initialCityName = activeCityName,
                                    onNavigateToLive = {
                                        currentScreen = AppScreen.LIVE_ORBIT
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
