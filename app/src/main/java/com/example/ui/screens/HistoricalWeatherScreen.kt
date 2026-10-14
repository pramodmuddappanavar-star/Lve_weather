package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.WeatherRepository
import com.example.ui.components.HistoricalWeatherChart
import com.example.ui.theme.*

@Composable
fun HistoricalWeatherScreen(
    repository: WeatherRepository,
    initialCityName: String = "Tokyo",
    onNavigateToLive: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val availableCities = remember { repository.staticCities }
    var selectedCityName by remember { mutableStateOf(initialCityName) }
    var selectedRange by remember { mutableStateOf(DateRangePreset.LAST_7_DAYS) }
    var selectedMetric by remember { mutableStateOf(MetricType.TEMPERATURE) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var expandedDayIndex by remember { mutableStateOf<Int?>(null) }

    // Fetch historical data whenever city or range changes
    val historicalDays = remember(selectedCityName, selectedRange) {
        val daysToFetch = if (selectedRange == DateRangePreset.SPECIFIC_DATE) 14 else selectedRange.days
        repository.getHistoricalWeather(selectedCityName, daysToFetch)
    }

    val summary = remember(historicalDays) {
        repository.computeSummary(historicalDays)
    }

    // Auto-select latest day if specific date mode
    LaunchedEffect(selectedRange, historicalDays) {
        if (selectedRange == DateRangePreset.SPECIFIC_DATE && historicalDays.isNotEmpty()) {
            selectedDayIndex = historicalDays.lastIndex
        } else {
            selectedDayIndex = null
        }
    }

    val selectedCity = availableCities.firstOrNull { it.name == selectedCityName } ?: availableCities.first()

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
            .testTag("historical_weather_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTORICAL METEOROLOGY",
                        color = GlowingCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Past Weather & Climate Archive",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Quick jump to 3D Globe for current city
                Button(
                    onClick = { onNavigateToLive(selectedCityName) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlowingCyan.copy(alpha = 0.15f),
                        contentColor = GlowingCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_view_live_3d")
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Live 3D View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Live 3D", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Inspect verified atmospheric archives across worldwide ground observation stations.",
                color = SoftGrayText,
                fontSize = 11.sp
            )
        }

        // 1. City / Location Selector Horizontal Chips
        Text(
            text = "OBSERVATION STATION / LOCATION",
            color = SoftGrayText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("historical_location_selector")
        ) {
            items(availableCities) { city ->
                val isSelected = city.name == selectedCityName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GlowingCyan.copy(alpha = 0.2f) else CosmicSurface)
                        .border(
                            1.dp,
                            if (isSelected) GlowingCyan else GlowingCyan.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedCityName = city.name
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("station_chip_${city.name.lowercase()}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GlowingCyan else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = city.name,
                                color = if (isSelected) Color.White else SoftGrayText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = city.country,
                                color = SoftGrayText,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Date Range Filter Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DATE RANGE",
                color = SoftGrayText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${historicalDays.size} data points loaded",
                color = RadarGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DateRangePreset.values().forEach { preset ->
                val isSelected = selectedRange == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DiagnosticOrange.copy(alpha = 0.2f) else DarkNavy)
                        .border(
                            1.dp,
                            if (isSelected) DiagnosticOrange else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedRange = preset }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.shortLabel,
                        color = if (isSelected) Color.White else SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // If Specific Date is chosen: show a carousel of dates to pick a single day
        AnimatedVisibility(visible = selectedRange == DateRangePreset.SPECIFIC_DATE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "SELECT SPECIFIC DAY TO INSPECT",
                    color = DiagnosticOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(historicalDays) { idx, day ->
                        val isSelected = selectedDayIndex == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DiagnosticOrange else CosmicSurface)
                                .clickable {
                                    selectedDayIndex = idx
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.dateFormatted.take(3),
                                    color = if (isSelected) Color.Black else SoftGrayText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${day.date.dayOfMonth}",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Metric Selector Tabs (Temperature / Precipitation / Wind)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkNavy)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricType.values().forEach { metric ->
                val isSelected = selectedMetric == metric
                val activeColor = when (metric) {
                    MetricType.TEMPERATURE -> DiagnosticOrange
                    MetricType.PRECIPITATION -> GlowingCyan
                    MetricType.WIND -> RadarGreen
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) activeColor.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) activeColor.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedMetric = metric }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = metric.label,
                        color = if (isSelected) Color.White else SoftGrayText,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Interactive Graphical Chart
        HistoricalWeatherChart(
            days = historicalDays,
            selectedMetric = selectedMetric,
            selectedIndex = selectedDayIndex,
            onSelectDay = { idx ->
                selectedDayIndex = idx
                expandedDayIndex = idx
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Climate Aggregate Summary Cards
        Text(
            text = "PERIOD STATISTICAL SUMMARY (${selectedRange.label})",
            color = SoftGrayText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = "Average Temp",
                value = "${summary.avgTemp}°C",
                subtext = "High ${summary.maxTemp}° • Low ${summary.minTemp}°",
                icon = Icons.Default.Thermostat,
                color = DiagnosticOrange,
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                title = "Total Rainfall",
                value = "${summary.totalPrecipitation} mm",
                subtext = "${summary.rainyDaysCount} rainy days recorded",
                icon = Icons.Default.WaterDrop,
                color = GlowingCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = "Avg Wind Speed",
                value = "${summary.avgWindSpeed} km/h",
                subtext = "Max gust ${summary.maxWindGust} km/h",
                icon = Icons.Default.Air,
                color = RadarGreen,
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                title = "Primary Weather",
                value = summary.predominantCondition,
                subtext = "${selectedCity.name} microclimate",
                icon = Icons.Default.WbSunny,
                color = RadiantPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Day-by-Day Historical Log
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAY-BY-DAY HISTORICAL LOG",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap day for full details",
                color = SoftGrayText,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            historicalDays.reversed().forEachIndexed { index, day ->
                val originalIndex = historicalDays.size - 1 - index
                val isExpanded = expandedDayIndex == originalIndex || selectedDayIndex == originalIndex

                HistoricalDayCard(
                    day = day,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedDayIndex = if (isExpanded) null else originalIndex
                        selectedDayIndex = originalIndex
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicSurface)
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = SoftGrayText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = SoftGrayText,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun HistoricalDayCard(
    day: HistoricalWeatherDay,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val weatherIcon = when {
        day.condition.contains("Blizzard") || day.condition.contains("Snow") -> Icons.Default.AcUnit
        day.condition.contains("Thunderstorm") -> Icons.Default.FlashOn
        day.condition.contains("Rain") || day.condition.contains("Drizzle") -> Icons.Default.WaterDrop
        day.condition.contains("Cloud") || day.condition.contains("Overcast") -> Icons.Default.Cloud
        else -> Icons.Default.WbSunny
    }

    val iconTint = when {
        day.condition.contains("Rain") -> GlowingCyan
        day.condition.contains("Thunderstorm") -> SatelliteHot
        day.condition.contains("Snow") -> Color(0xFF80D8FF)
        else -> DiagnosticOrange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isExpanded) DarkNavy else CosmicSurface)
            .border(
                1.dp,
                if (isExpanded) GlowingCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggleExpand() }
            .padding(12.dp)
            .testTag("day_card_${day.date}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Date & Condition
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = weatherIcon,
                            contentDescription = day.condition,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = day.dateFormatted,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = day.condition,
                            color = SoftGrayText,
                            fontSize = 11.sp
                        )
                    }
                }

                // Right: Temperature extremes & expand arrow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${day.tempMax.toInt()}°",
                                color = DiagnosticOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " / ${day.tempMin.toInt()}°C",
                                color = SoftGrayText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (day.precipitationMm > 0) {
                            Text(
                                text = "${day.precipitationMm} mm rain",
                                color = GlowingCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = SoftGrayText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded deep metrics breakdown
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailMetricItem(
                            label = "Precipitation",
                            value = "${day.precipitationMm} mm (${day.precipitationChance}%)",
                            icon = Icons.Default.WaterDrop,
                            color = GlowingCyan
                        )
                        DetailMetricItem(
                            label = "Wind Speed",
                            value = "${day.windSpeedAvg} km/h (${day.windDirectionText})",
                            icon = Icons.Default.Air,
                            color = RadarGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailMetricItem(
                            label = "Peak Wind Gust",
                            value = "${day.windSpeedMax} km/h",
                            icon = Icons.Default.Speed,
                            color = SatelliteHot
                        )
                        DetailMetricItem(
                            label = "Avg Humidity",
                            value = "${day.humidityAvg}%",
                            icon = Icons.Default.Opacity,
                            color = RadiantPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailMetricItem(
                            label = "Barometric Pressure",
                            value = "${day.pressureHpa} hPa",
                            icon = Icons.Default.CompassCalibration,
                            color = Color.LightGray
                        )
                        DetailMetricItem(
                            label = "Daily Mean Temp",
                            value = "${day.tempAvg}°C",
                            icon = Icons.Default.Thermostat,
                            color = DiagnosticOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, color = SoftGrayText, fontSize = 8.sp)
            Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
