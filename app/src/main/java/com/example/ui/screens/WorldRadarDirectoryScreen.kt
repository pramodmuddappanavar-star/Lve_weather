package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityWeather
import com.example.data.repository.WeatherRepository
import com.example.ui.theme.*

enum class SortMode(val label: String) {
    DEFAULT("Alphabetical"),
    WARMEST("Warmest"),
    COLDEST("Coldest"),
    WINDIEST("Windiest")
}

@Composable
fun WorldRadarDirectoryScreen(
    repository: WeatherRepository,
    onSelectCity: (CityWeather) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }

    val allCities = remember { repository.staticCities }
    val regions = remember { listOf("All", "Asia", "Europe", "North America", "South America", "Africa", "Middle East", "Oceania", "Polar") }

    val processedCities = remember(searchQuery, selectedRegion, sortMode) {
        val searched = repository.searchLocations(searchQuery)
        val filtered = if (selectedRegion == "All") searched else searched.filter { it.region == selectedRegion }

        when (sortMode) {
            SortMode.DEFAULT -> filtered.sortedBy { it.name }
            SortMode.WARMEST -> filtered.sortedByDescending { it.temperature }
            SortMode.COLDEST -> filtered.sortedBy { it.temperature }
            SortMode.WINDIEST -> filtered.sortedByDescending { it.windSpeed }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkNavy, DeepSpace)
                )
            )
            .testTag("world_radar_directory_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "GLOBAL METEOROLOGICAL DIRECTORY",
                color = GlowingCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "All World Observation Stations",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter all world cities...", color = SoftGrayText, fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GlowingCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlowingCyan,
                    unfocusedBorderColor = GlowingCyan.copy(alpha = 0.25f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CosmicSurface,
                    unfocusedContainerColor = CosmicSurface
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Region Filter Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(regions) { reg ->
                val isSel = selectedRegion == reg
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) GlowingCyan.copy(alpha = 0.2f) else CosmicSurface)
                        .border(1.dp, if (isSel) GlowingCyan else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedRegion = reg }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reg,
                        color = if (isSel) GlowingCyan else SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sorting Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${processedCities.size} STATIONS ONLINE",
                color = RadarGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SortMode.values().forEach { mode ->
                    val isSel = sortMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) DiagnosticOrange.copy(alpha = 0.2f) else DarkNavy)
                            .clickable { sortMode = mode }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSel) DiagnosticOrange else SoftGrayText,
                            fontSize = 9.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of World Stations
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(processedCities) { city ->
                WorldStationCard(city = city, onClick = { onSelectCity(city) })
            }
        }
    }
}

@Composable
fun WorldStationCard(
    city: CityWeather,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicSurface)
            .border(1.dp, GlowingCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("station_card_${city.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (city.temperature > 25) DiagnosticOrange else if (city.temperature < 5) Color(0xFF00E5FF) else RadarGreen)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkNavy)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = city.region, color = SoftGrayText, fontSize = 8.sp)
                        }
                    }
                    Text(
                        text = "${city.country} • Lat: ${city.latitude}° / Lon: ${city.longitude}°",
                        color = SoftGrayText,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${city.temperature}°C",
                    color = GlowingCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${city.condition} • Wind ${city.windSpeed} km/h",
                    color = SoftGrayText,
                    fontSize = 10.sp
                )
            }
        }
    }
}
