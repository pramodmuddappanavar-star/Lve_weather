package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.CityWeather
import com.example.data.model.HourlySituation
import com.example.data.repository.WeatherRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceWeatherSituationScreen(
    repository: WeatherRepository,
    initialCityName: String = "Tokyo",
    onNavigateToGlobe: (String) -> Unit = {},
    onNavigateToHistory: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityName by remember { mutableStateOf(initialCityName) }
    var selectedRegion by remember { mutableStateOf("All") }
    var isCoordFinderOpen by remember { mutableStateOf(false) }
    var inputLat by remember { mutableStateOf("") }
    var inputLon by remember { mutableStateOf("") }

    val allCities = remember { repository.staticCities }
    val regions = remember { listOf("All", "Asia", "Europe", "North America", "South America", "Africa", "Middle East", "Oceania", "Polar") }

    val filteredCities = remember(searchQuery, selectedRegion) {
        val queryMatches = repository.searchLocations(searchQuery)
        if (selectedRegion == "All") queryMatches else queryMatches.filter { it.region == selectedRegion }
    }

    // Currently selected city or custom coordinate location
    var customCity by remember { mutableStateOf<CityWeather?>(null) }
    val currentCity = customCity ?: allCities.firstOrNull { it.name.equals(selectedCityName, ignoreCase = true) } ?: allCities.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkNavy, DeepSpace)
                )
            )
            .padding(bottom = 32.dp)
            .testTag("place_weather_situation_screen")
    ) {
        // Top Search Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "WORLDWIDE WEATHER SITUATION",
                color = GlowingCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Live Location Atmospheric Situation & Visuals",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    customCity = null
                },
                placeholder = {
                    Text(
                        text = "Search any city, country, or region (e.g. Paris, Dubai, Tokyo)...",
                        color = SoftGrayText,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GlowingCyan
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = SoftGrayText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = { isCoordFinderOpen = !isCoordFinderOpen }) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Coordinate Finder",
                                tint = if (isCoordFinderOpen) DiagnosticOrange else SoftGrayText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlowingCyan,
                    unfocusedBorderColor = GlowingCyan.copy(alpha = 0.25f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CosmicSurface,
                    unfocusedContainerColor = CosmicSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_location_search")
            )
        }

        // Custom GPS Coordinate Finder Expandable Box
        AnimatedVisibility(visible = isCoordFinderOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkNavy)
                    .border(1.dp, DiagnosticOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "GPS COORDINATE WEATHER FINDER",
                    color = DiagnosticOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enter any latitude and longitude to retrieve satellite atmospheric telemetry for that exact point on Earth:",
                    color = SoftGrayText,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputLat,
                        onValueChange = { inputLat = it },
                        label = { Text("Lat (-90 to 90)", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = inputLon,
                        onValueChange = { inputLon = it },
                        label = { Text("Lon (-180 to 180)", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val latVal = inputLat.toDoubleOrNull() ?: 0.0
                        val lonVal = inputLon.toDoubleOrNull() ?: 0.0
                        customCity = repository.findByCoordinates(latVal, lonVal)
                        isCoordFinderOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiagnosticOrange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compute Satellite Weather Situation", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
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

        Spacer(modifier = Modifier.height(10.dp))

        // Search Result City Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredCities) { city ->
                val isSelected = currentCity.name == city.name && customCity == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GlowingCyan.copy(alpha = 0.25f) else CosmicSurface)
                        .border(1.dp, if (isSelected) GlowingCyan else GlowingCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable {
                            selectedCityName = city.name
                            customCity = null
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("situation_city_${city.name.lowercase()}")
                ) {
                    Column {
                        Text(
                            text = city.name,
                            color = if (isSelected) GlowingCyan else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${city.temperature}°C • ${city.condition}",
                            color = SoftGrayText,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // HERO CARD: Current Place Weather Situation with Real Imagery
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.25f))
        ) {
            Column {
                // High-Resolution Place Imagery with Weather Situation Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(Color.Black)
                ) {
                    SubcomposeAsyncImage(
                        model = currentCity.imageUrl,
                        contentDescription = "Current weather situation in ${currentCity.name}",
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GlowingCyan, modifier = Modifier.size(32.dp))
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(DarkNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Landscape, contentDescription = null, tint = GlowingCyan, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "${currentCity.name} Live Weather Cam", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim for readable text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Top Place Badge & Local Time
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${currentCity.region.uppercase()} REGION",
                                color = GlowingCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LOCAL: ${currentCity.localTime}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom City Title & Weather Condition Tag
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "${currentCity.name}, ${currentCity.country}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentCity.temperature}°C",
                                color = GlowingCyan,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${currentCity.condition} (Feels like ${currentCity.feelsLike}°C)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Deep Atmospheric Situation Narrative Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Atmospheric Situation",
                            tint = DiagnosticOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ATMOSPHERIC SITUATION ANALYSIS",
                            color = DiagnosticOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentCity.situationDescription,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(color = GlowingCyan.copy(alpha = 0.15f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Microclimate Key Parameters Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SituationMetricBox(
                            title = "Wind & Direction",
                            value = "${currentCity.windSpeed} km/h",
                            subtext = "${currentCity.windDirection.toInt()}° Compass",
                            icon = Icons.Default.Air,
                            color = RadarGreen,
                            modifier = Modifier.weight(1f)
                        )
                        SituationMetricBox(
                            title = "Relative Humidity",
                            value = "${currentCity.humidity}%",
                            subtext = "Dew point ${currentCity.dewPoint}°C",
                            icon = Icons.Default.WaterDrop,
                            color = GlowingCyan,
                            modifier = Modifier.weight(1f)
                        )
                        SituationMetricBox(
                            title = "Air Quality (AQI)",
                            value = "AQI ${currentCity.airQualityIndex}",
                            subtext = if (currentCity.airQualityIndex <= 2) "Good Air" else "Moderate",
                            icon = Icons.Default.Shield,
                            color = RadiantPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SituationMetricBox(
                            title = "Atm Pressure",
                            value = "${currentCity.pressure} hPa",
                            subtext = "Barometric",
                            icon = Icons.Default.Speed,
                            color = Color.LightGray,
                            modifier = Modifier.weight(1f)
                        )
                        SituationMetricBox(
                            title = "UV Radiation",
                            value = "UV ${currentCity.uvIndex}",
                            subtext = if (currentCity.uvIndex >= 6) "High Intensity" else "Moderate",
                            icon = Icons.Default.WbSunny,
                            color = DiagnosticOrange,
                            modifier = Modifier.weight(1f)
                        )
                        SituationMetricBox(
                            title = "Satellite Scan",
                            value = "${currentCity.satelliteCloudCover}% Cover",
                            subtext = currentCity.liveSatelliteName,
                            icon = Icons.Default.SatelliteAlt,
                            color = GlowingCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 24-Hour Situation Forecast Progression
                    if (currentCity.hourlyForecast.isNotEmpty()) {
                        Text(
                            text = "24-HOUR SITUATION PROGRESSION TIMELINE",
                            color = SoftGrayText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentCity.hourlyForecast) { slot ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkNavy)
                                        .border(1.dp, GlowingCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = slot.timeLabel, color = SoftGrayText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "${slot.temperature}°", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = slot.condition, color = GlowingCyan, fontSize = 9.sp)
                                        if (slot.rainProbability > 0) {
                                            Text(text = "${slot.rainProbability}% Rain", color = Color(0xFF00AAFF), fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Navigation Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToGlobe(currentCity.name) },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan.copy(alpha = 0.15f), contentColor = GlowingCyan),
                            border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "View on 3D Earth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigateToHistory(currentCity.name) },
                            colors = ButtonDefaults.buttonColors(containerColor = DiagnosticOrange.copy(alpha = 0.15f), contentColor = DiagnosticOrange),
                            border = BorderStroke(1.dp, DiagnosticOrange.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "View Past History", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SituationMetricBox(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkNavy)
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, color = SoftGrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = subtext, color = SoftGrayText, fontSize = 8.sp)
        }
    }
}
