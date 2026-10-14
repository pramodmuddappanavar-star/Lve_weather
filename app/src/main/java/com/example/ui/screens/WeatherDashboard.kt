package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.CityWeather
import com.example.data.model.OrbitSatPoint
import com.example.data.model.SatelliteFilter
import com.example.data.repository.WeatherRepository
import com.example.ui.components.Globe3D
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboard(
    repository: WeatherRepository,
    onNavigateToSituation: (String) -> Unit = {},
    onNavigateToHistory: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cities = remember { mutableStateListOf<CityWeather>() }
    val satellites = remember { mutableStateListOf<OrbitSatPoint>() }
    
    var selectedCity by remember { mutableStateOf<CityWeather?>(null) }
    var currentFilter by remember { mutableStateOf(SatelliteFilter.CLOUD_DENSITY) }
    var isPythonMockActive by remember { mutableStateOf(true) }
    var globeSearchQuery by remember { mutableStateOf("") }
    
    // Listen to repository weather data (updates live every 4 seconds)
    LaunchedEffect(Unit) {
        repository.getWeatherDataFlow().collectLatest { data ->
            cities.clear()
            cities.addAll(data)
            
            // Re-sync selected city metadata to get live updates on pressure/signals
            if (selectedCity == null) {
                selectedCity = data.firstOrNull { it.name == "Tokyo" }
            } else {
                selectedCity = data.firstOrNull { it.name == selectedCity?.name }
            }
        }
    }

    // Listen to satellite orbital progression
    LaunchedEffect(Unit) {
        repository.getSatellitesFlow().collectLatest { satData ->
            satellites.clear()
            satellites.addAll(satData)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepSpace,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Radar Global",
                            tint = GlowingCyan,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = "ORBITWEATHER 3D",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = GlowingCyan.copy(alpha = 0.5f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 12f
                                )
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkNavy.copy(alpha = 0.85f),
                ),
                actions = {
                    // History quick button
                    IconButton(
                        onClick = { onNavigateToHistory(selectedCity?.name ?: "Tokyo") },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Historical Weather",
                            tint = GlowingCyan
                        )
                    }

                    // Satellite Network Status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .border(1.dp, GlowingCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .background(CosmicSurface.copy(alpha = 0.6f))
                            .clickable {
                                isPythonMockActive = !isPythonMockActive
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RadarGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NETWORK ONLINE",
                            color = RadarGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkNavy, DeepSpace)
                    )
                )
        ) {
            // High-tech satellite telemetry operational info strip
            AnimatedVisibility(
                visible = isPythonMockActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, RadarGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .background(Color(0x1A00FF57))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Network status",
                                tint = RadarGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GLOBAL SATELLITE NETWORK ACTIVE",
                                color = RadarGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "TAP TO DISMISS",
                            color = SoftGrayText,
                            fontSize = 8.sp,
                            modifier = Modifier.clickable { isPythonMockActive = false }
                        )
                    }
                    Text(
                        text = "Real-time telemetry stream synchronized with GOES-16, METEOSAT-11, SENTINEL-6A & HIMAWARI-9 atmospheric sensors. 10 world observation stations online.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Interactive 3D Search & Fly-To Bar
            OutlinedTextField(
                value = globeSearchQuery,
                onValueChange = { globeSearchQuery = it },
                placeholder = { Text("Search any location to spin 3D Earth (e.g. Paris, Dubai, Sydney)...", color = SoftGrayText, fontSize = 11.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GlowingCyan, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (globeSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { globeSearchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = SoftGrayText, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlowingCyan,
                    unfocusedBorderColor = GlowingCyan.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = CosmicSurface,
                    unfocusedContainerColor = CosmicSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (globeSearchQuery.isNotBlank()) {
                val matches = cities.filter {
                    it.name.contains(globeSearchQuery, ignoreCase = true) ||
                    it.country.contains(globeSearchQuery, ignoreCase = true) ||
                    it.region.contains(globeSearchQuery, ignoreCase = true)
                }
                if (matches.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(matches) { match ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkNavy)
                                    .border(1.dp, GlowingCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedCity = match
                                        globeSearchQuery = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✈️ Fly to ${match.name} (${match.temperature}°C)",
                                    color = GlowingCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 1. Core 3D Interactive Screen View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GlowingCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            0.0f to CosmicSurface,
                            1.0f to DeepSpace
                        )
                    )
            ) {
                // Interactive Globe Canvas Renderer
                Globe3D(
                    cities = cities,
                    selectedCity = selectedCity,
                    satellites = satellites,
                    satelliteFilter = currentFilter,
                    onCitySelected = { selectedCity = it }
                )

                // High-fidelity heads-up visual instrumentation on globe
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "TRACKING SCAN: LIVE", color = GlowingCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "SWIPE TO SPIN GLOBE", color = SoftGrayText, fontSize = 9.sp)
                }

                // Satellite Scan Overlap Filters in Bottom Center of globe panel
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkNavy.copy(alpha = 0.85f))
                        .border(1.dp, GlowingCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SatelliteFilterButton(
                            title = "Clouds",
                            icon = Icons.Outlined.Cloud,
                            isActive = currentFilter == SatelliteFilter.CLOUD_DENSITY,
                            onClick = { currentFilter = SatelliteFilter.CLOUD_DENSITY }
                        )
                        SatelliteFilterButton(
                            title = "Thermal",
                            icon = Icons.Outlined.DeviceThermostat,
                            isActive = currentFilter == SatelliteFilter.ATMOSPHERE_THERMAL,
                            onClick = { currentFilter = SatelliteFilter.ATMOSPHERE_THERMAL }
                        )
                        SatelliteFilterButton(
                            title = "Wind",
                            icon = Icons.Outlined.Air,
                            isActive = currentFilter == SatelliteFilter.WIND_STREAM,
                            onClick = { currentFilter = SatelliteFilter.WIND_STREAM }
                        )
                        SatelliteFilterButton(
                            title = "CO₂",
                            icon = Icons.Outlined.Co2,
                            isActive = currentFilter == SatelliteFilter.CO2_EMISSIONS,
                            onClick = { currentFilter = SatelliteFilter.CO2_EMISSIONS }
                        )
                    }
                }
            }

            // 2. City Selector Horizontal Strip
            Text(
                text = "WORLD MET DATA RADIUS NODES",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cities) { city ->
                    val isSelected = selectedCity?.name == city.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GlowingCyan.copy(alpha = 0.15f) else CosmicSurface)
                            .border(
                                1.dp,
                                if (isSelected) GlowingCyan else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCity = city }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = city.name, color = if (isSelected) GlowingCyan else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${city.temperature}°C • ${city.condition}", color = if (isSelected) Color.White else SoftGrayText, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 3. Highlighted Selected City Telemetry instruments
            selectedCity?.let { city ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GlowingCyan.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .background(CosmicSurface)
                        .padding(16.dp)
                ) {
                    // Header Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${city.name.uppercase()}, ${city.country.uppercase()}", color = GlowingCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${city.latitude}°N, ${city.longitude}°E Grid", color = SoftGrayText, fontSize = 10.sp)
                        }
                        
                        // Large Glowing Temp value
                        Text(
                            text = "${city.temperature}°C",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            style = LocalTextStyle.current.copy(
                                shadow = Shadow(
                                    color = GlowingCyan.copy(alpha = 0.35f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 16f
                                )
                            )
                        )
                    }

                    HorizontalDivider(color = GlowingCyan.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    // Secondary Core Parameters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Humidity Sensor",
                            value = "${city.humidity}%",
                            icon = Icons.Default.WaterDrop,
                            color = GlowingCyan
                        )
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Wind Stream",
                            value = "${city.windSpeed} km/h",
                            icon = Icons.Default.Air,
                            color = RadarGreen
                        )
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "UV Radiation",
                            value = "UV ${city.uvIndex}",
                            icon = Icons.Default.WbSunny,
                            color = DiagnosticOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Carbon Monitor",
                            value = "${city.carbonDioxidePpm} ppm",
                            icon = Icons.Default.Shield,
                            color = RadiantPurple
                        )
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Atm Pressure",
                            value = "${city.pressure} hPa",
                            icon = Icons.Default.Speed,
                            color = Color.LightGray
                        )
                        InstrumentGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Visibility Range",
                            value = "${city.visibility} km",
                            icon = Icons.Default.RemoveRedEye,
                            color = Color(0xFF00AAFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Satellite Scan diagnostics label
                    Text(
                        text = "SPACE FEED ANALYST OVERHEAD",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkNavy)
                            .border(1.dp, GlowingCyan.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SatelliteAlt,
                                        contentDescription = "Active Sat",
                                        tint = GlowingCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = city.liveSatelliteName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GlowingCyan.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${city.satelliteSignalStrength} dBm Signal",
                                        color = GlowingCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MetadataLine(title = "Orbital Alt", value = "${city.orbitalHeight} km")
                                MetadataLine(title = "Orbital Velocity", value = "${city.orbitalVelocity} km/s")
                                MetadataLine(title = "Cloud Scan Max", value = "${city.satelliteCloudCover}%")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigateToSituation(city.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiagnosticOrange.copy(alpha = 0.2f),
                            contentColor = DiagnosticOrange
                        ),
                        border = BorderStroke(1.dp, DiagnosticOrange.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Weather Situation & Images",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIEW ${city.name.uppercase()} SITUATION & LIVE IMAGERY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onNavigateToHistory(city.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlowingCyan.copy(alpha = 0.15f),
                            contentColor = GlowingCyan
                        ),
                        border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Historical Weather Data",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIEW ${city.name.uppercase()} HISTORICAL DATA & TRENDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 4. Live Satellite Images direct feed from Space
            Text(
                text = "REAL-TIME WORLD SATELLITE IMAGERY FEED",
                color = SoftGrayText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GlowingCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                // Outer image rendering loaded via Coil
                SubcomposeAsyncImage(
                    model = "https://photojournal.jpl.nasa.gov/jpeg/PIA18033.jpg", // Earth atmospheric moisture NASA/JPL
                    contentDescription = "Global Satellite scanning camera direct feed",
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GlowingCyan)
                        }
                    },
                    error = {
                        // High fidelity local fallbacks container
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CosmicSurface)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Fallback scan image",
                                    tint = GlowingCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "NASA METEOROLOGICAL LIVE SCAN IMAGERY",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Authentic Cloud cover, Infrared maps loading over active port",
                                    color = SoftGrayText,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High fidelity HUD scanner lines overlaid on raw camera feed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            // Draw horizontal telemetry guidelines
                            val step = size.height / 5
                            for (i in 1..4) {
                                val y = step * i
                                drawLine(
                                    color = GlowingCyan.copy(alpha = 0.15f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1f
                                )
                            }
                            // Vertical grid lines
                            val stepW = size.width / 5
                            for (j in 1..4) {
                                val x = stepW * j
                                drawLine(
                                    color = GlowingCyan.copy(alpha = 0.15f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1f
                                )
                            }
                        }
                )

                // Scanner target indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SatelliteHot)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NASA METEOSAT IR SCAN 8-MICRON FEED",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Aim target",
                        tint = GlowingCyan,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LAT: ${selectedCity?.latitude ?: 0.0}° / LON: ${selectedCity?.longitude ?: 0.0}°",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SatelliteFilterButton(
    title: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) GlowingCyan.copy(alpha = 0.22f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) GlowingCyan else SoftGrayText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isActive) Color.White else SoftGrayText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InstrumentGridCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkNavy)
            .border(0.5.dp, color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, color = SoftGrayText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun MetadataLine(title: String, value: String) {
    Column {
        Text(text = title.lowercase().replaceFirstChar { it.uppercase() }, color = SoftGrayText, fontSize = 8.sp)
        Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
