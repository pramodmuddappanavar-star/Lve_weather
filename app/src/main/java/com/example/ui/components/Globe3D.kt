package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.CityWeather
import com.example.data.model.OrbitSatPoint
import com.example.data.model.SatelliteFilter
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Globe3D(
    cities: List<CityWeather>,
    selectedCity: CityWeather?,
    satellites: List<OrbitSatPoint>,
    satelliteFilter: SatelliteFilter,
    onCitySelected: (CityWeather) -> Unit,
    modifier: Modifier = Modifier
) {
    var yaw by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0.25f) }
    var isDragging by remember { mutableStateOf(false) }

    // Smooth camera flight when a city is selected
    val targetYaw = remember(selectedCity) {
        selectedCity?.let { Math.toRadians(-it.longitude).toFloat() }
    }
    val targetPitch = remember(selectedCity) {
        selectedCity?.let { Math.toRadians(it.latitude).toFloat().coerceIn(-1.2f, 1.2f) }
    }

    if (targetYaw != null && targetPitch != null) {
        LaunchedEffect(selectedCity) {
            val yawAnim = Animatable(yaw)
            val pitchAnim = Animatable(pitch)

            var dYaw = targetYaw - yaw
            while (dYaw < -Math.PI) dYaw += (2 * Math.PI).toFloat()
            while (dYaw > Math.PI) dYaw -= (2 * Math.PI).toFloat()

            launch {
                yawAnim.animateTo(
                    targetValue = yaw + dYaw,
                    animationSpec = tween(durationMillis = 1100, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.0f))
                ) { yaw = this.value }
            }
            launch {
                pitchAnim.animateTo(
                    targetValue = targetPitch,
                    animationSpec = tween(durationMillis = 1100, easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.0f))
                ) { pitch = this.value }
            }
        }
    }

    // Gentle ambient Earth planetary rotation
    if (!isDragging && selectedCity == null) {
        val infiniteTransition = rememberInfiniteTransition(label = "earth_ambient_spin")
        val ambientAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 40000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin_angle"
        )
        LaunchedEffect(ambientAngle) {
            yaw = (yaw + 0.0025f) % (2f * Math.PI).toFloat()
        }
    }

    // Animated pulse & cloud wave transitions
    val transition = rememberInfiniteTransition(label = "pulse_and_clouds")
    val pulseRadius by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val cloudWave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clouds"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(cities, yaw, pitch) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height
                    val cx = width / 2f
                    val cy = height / 2f
                    val radius = (Math.min(width, height) / 2f) * 0.82f

                    // Detect which city pin was tapped
                    for (city in cities) {
                        val theta = Math.toRadians(city.latitude)
                        val lambda = Math.toRadians(city.longitude)
                        val rx = radius * cos(theta) * sin(lambda)
                        val ry = -radius * sin(theta)
                        val rz = radius * cos(theta) * cos(lambda)
                        val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)
                        if (p.z > 0) {
                            val pinX = cx + p.x
                            val pinY = cy + p.y
                            val dist = sqrt((tapOffset.x - pinX) * (tapOffset.x - pinX) + (tapOffset.y - pinY) * (tapOffset.y - pinY))
                            if (dist < 28.dp.toPx()) {
                                onCitySelected(city)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x * 0.006f
                        val dy = dragAmount.y * 0.006f
                        yaw = (yaw + dx) % (2 * Math.PI).toFloat()
                        pitch = (pitch - dy).coerceIn(-1.25f, 1.25f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f
            val radius = (Math.min(width, height) / 2f) * 0.82f

            // 1. Atmospheric Outer Glow & Space Scattering
            drawAtmosphereBackdrop(cx, cy, radius)

            // 2. Realistic Oceanic Sphere Base with 3D Spherical Light
            drawRealisticOceanSphere(cx, cy, radius)

            // 3. 3D Coordinates Grid (Subtle latitude & longitude meridians)
            drawGlobeMesh(cx, cy, radius, yaw, pitch)

            // 4. Realistic Land Continents with Terrain Fills & Coastlines
            drawRealisticContinents(cx, cy, radius, yaw, pitch)

            // 5. Dynamic Cloud Layer Formations (drifting over Earth surface)
            drawAtmosphericClouds(cx, cy, radius, yaw, pitch, cloudWave)

            // 6. Selected Meteorological Filter Layers (Clouds/Thermal/Wind/CO2)
            drawSatelliteDataLayer(cx, cy, radius, yaw, pitch, satelliteFilter, framePulse = pulseRadius)

            // 7. Orbiting Satellites
            drawSatellites(cx, cy, radius, yaw, pitch, satellites, pulseRadius, (1f - (pulseRadius - 0.5f) / 0.9f).coerceIn(0f, 1f))

            // 8. Worldwide City Weather Beacons & Pins
            drawCities(
                cx = cx,
                cy = cy,
                radius = radius,
                yaw = yaw,
                pitch = pitch,
                cities = cities,
                selectedCity = selectedCity,
                pulseRadiusFactor = pulseRadius,
                pulseAlpha = (1f - (pulseRadius - 0.5f) / 0.9f).coerceIn(0f, 1f)
            )

            // 9. Outer Ring Horizon & Limb Lighting
            drawAtmosphericLimb(cx, cy, radius)
        }
    }
}

/**
 * Renders radiant Rayleigh atmospheric glow behind and around the Earth globe.
 */
fun DrawScope.drawAtmosphereBackdrop(cx: Float, cy: Float, radius: Float) {
    val atmosphereGradient = Brush.radialGradient(
        0.0f to GlowingCyan.copy(alpha = 0.45f),
        0.75f to Color(0xFF0077B6).copy(alpha = 0.2f),
        1.0f to Color.Transparent,
        center = Offset(cx, cy),
        radius = radius * 1.32f
    )
    drawCircle(
        brush = atmosphereGradient,
        radius = radius * 1.32f,
        center = Offset(cx, cy)
    )
}

/**
 * Realistic spherical ocean core with 3D day/night illumination gradient.
 */
fun DrawScope.drawRealisticOceanSphere(cx: Float, cy: Float, radius: Float) {
    // 3D Sunlit ocean sphere with diffuse highlight from top-left sun angle
    val oceanShading = Brush.radialGradient(
        0.0f to Color(0xFF1D4E89), // sunlit ocean highlight
        0.55f to Color(0xFF0B2545), // deep marine blue
        0.90f to Color(0xFF061426), // dark ocean abyss
        1.0f to Color(0xFF020914),  // outer terminator edge
        center = Offset(cx - (radius * 0.35f), cy - (radius * 0.35f)),
        radius = radius * 1.2f
    )
    drawCircle(
        brush = oceanShading,
        radius = radius,
        center = Offset(cx, cy)
    )
}

/**
 * Translucent 3D latitude and longitude coordinates grid.
 */
fun DrawScope.drawGlobeMesh(cx: Float, cy: Float, radius: Float, yaw: Float, pitch: Float) {
    val latInterval = 30
    val lonInterval = 30

    // Equator highlighted
    for (lat in -60..60 step latInterval) {
        val pathFront = Path()
        var firstFront = true
        val theta = Math.toRadians(lat.toDouble())

        for (lon in -180..180 step 6) {
            val lambda = Math.toRadians(lon.toDouble())
            val rx = radius * cos(theta) * sin(lambda)
            val ry = -radius * sin(theta)
            val rz = radius * cos(theta) * cos(lambda)
            val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

            if (p.z > 0) {
                val screenX = cx + p.x
                val screenY = cy + p.y
                if (firstFront) {
                    pathFront.moveTo(screenX, screenY)
                    firstFront = false
                } else {
                    pathFront.lineTo(screenX, screenY)
                }
            } else {
                firstFront = true
            }
        }

        drawPath(
            path = pathFront,
            color = if (lat == 0) GlowingCyan.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
            style = Stroke(width = if (lat == 0) 1.2.dp.toPx() else 0.7.dp.toPx())
        )
    }

    // Longitude Meridians
    for (lon in -180 until 180 step lonInterval) {
        val pathFront = Path()
        var firstFront = true
        val lambda = Math.toRadians(lon.toDouble())

        for (lat in -80..80 step 5) {
            val theta = Math.toRadians(lat.toDouble())
            val rx = radius * cos(theta) * sin(lambda)
            val ry = -radius * sin(theta)
            val rz = radius * cos(theta) * cos(lambda)
            val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

            if (p.z > 0) {
                val screenX = cx + p.x
                val screenY = cy + p.y
                if (firstFront) {
                    pathFront.moveTo(screenX, screenY)
                    firstFront = false
                } else {
                    pathFront.lineTo(screenX, screenY)
                }
            } else {
                firstFront = true
            }
        }

        drawPath(
            path = pathFront,
            color = if (lon == 0) GlowingCyan.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
            style = Stroke(width = if (lon == 0) 1.2.dp.toPx() else 0.7.dp.toPx())
        )
    }
}

/**
 * Realistic solid landmasses across all 7 continents with natural terrain coloring.
 */
fun DrawScope.drawRealisticContinents(cx: Float, cy: Float, radius: Float, yaw: Float, pitch: Float) {
    val continentPolygons = listOf(
        // North America Main Body
        ContinentData(
            name = "North America",
            fillColor = Color(0xFF2E6F40), // rich forest green
            borderColor = Color(0xFF52B788),
            points = listOf(
                Pair(70.0, -165.0), Pair(71.0, -135.0), Pair(68.0, -100.0), Pair(60.0, -75.0),
                Pair(52.0, -56.0), Pair(44.0, -64.0), Pair(35.0, -75.0), Pair(25.0, -80.0),
                Pair(20.0, -88.0), Pair(15.0, -90.0), Pair(18.0, -104.0), Pair(24.0, -110.0),
                Pair(32.0, -117.0), Pair(38.0, -123.0), Pair(48.0, -125.0), Pair(58.0, -137.0),
                Pair(60.0, -145.0), Pair(64.0, -165.0), Pair(70.0, -165.0)
            )
        ),
        // South America
        ContinentData(
            name = "South America",
            fillColor = Color(0xFF26532B), // lush Amazonian green
            borderColor = Color(0xFF40916C),
            points = listOf(
                Pair(12.0, -72.0), Pair(10.0, -62.0), Pair(5.0, -52.0), Pair(-3.0, -40.0),
                Pair(-8.0, -35.0), Pair(-18.0, -38.0), Pair(-23.0, -43.0), Pair(-34.0, -53.0),
                Pair(-45.0, -65.0), Pair(-55.0, -68.0), Pair(-50.0, -74.0), Pair(-38.0, -73.0),
                Pair(-22.0, -70.0), Pair(-5.0, -80.0), Pair(5.0, -77.0), Pair(12.0, -72.0)
            )
        ),
        // Europe
        ContinentData(
            name = "Europe",
            fillColor = Color(0xFF386641),
            borderColor = Color(0xFF74C69D),
            points = listOf(
                Pair(71.0, 26.0), Pair(60.0, 30.0), Pair(54.0, 24.0), Pair(45.0, 30.0),
                Pair(40.0, 28.0), Pair(36.0, 15.0), Pair(36.0, -5.0), Pair(43.0, -9.0),
                Pair(48.0, -5.0), Pair(54.0, 8.0), Pair(58.0, 10.0), Pair(66.0, 14.0),
                Pair(71.0, 26.0)
            )
        ),
        // British Isles
        ContinentData(
            name = "UK",
            fillColor = Color(0xFF386641),
            borderColor = Color(0xFF74C69D),
            points = listOf(
                Pair(58.0, -5.0), Pair(55.0, -1.0), Pair(51.0, 1.0), Pair(50.0, -5.0),
                Pair(54.0, -6.0), Pair(58.0, -5.0)
            )
        ),
        // Africa
        ContinentData(
            name = "Africa",
            fillColor = Color(0xFF8A6D3B), // savanna & Saharan golden-earth
            borderColor = Color(0xFFB58463),
            points = listOf(
                Pair(36.0, -5.0), Pair(37.0, 10.0), Pair(32.0, 25.0), Pair(31.0, 32.0),
                Pair(22.0, 38.0), Pair(12.0, 44.0), Pair(12.0, 51.0), Pair(0.0, 42.0),
                Pair(-12.0, 40.0), Pair(-25.0, 32.0), Pair(-34.0, 26.0), Pair(-34.0, 18.0),
                Pair(-22.0, 14.0), Pair(-5.0, 12.0), Pair(4.0, 8.0), Pair(5.0, -2.0),
                Pair(6.0, -12.0), Pair(15.0, -17.0), Pair(28.0, -12.0), Pair(36.0, -5.0)
            )
        ),
        // Asia Main Landmass
        ContinentData(
            name = "Asia",
            fillColor = Color(0xFF2D5A27),
            borderColor = Color(0xFF52B788),
            points = listOf(
                Pair(75.0, 100.0), Pair(70.0, 140.0), Pair(65.0, 170.0), Pair(60.0, 162.0),
                Pair(52.0, 142.0), Pair(40.0, 124.0), Pair(30.0, 122.0), Pair(22.0, 114.0),
                Pair(10.0, 106.0), Pair(8.0, 77.0), Pair(22.0, 68.0), Pair(25.0, 58.0),
                Pair(30.0, 48.0), Pair(38.0, 42.0), Pair(42.0, 50.0), Pair(50.0, 58.0),
                Pair(60.0, 70.0), Pair(72.0, 75.0), Pair(75.0, 100.0)
            )
        ),
        // Arabian Peninsula
        ContinentData(
            name = "Arabia",
            fillColor = Color(0xFF8A6D3B),
            borderColor = Color(0xFFB58463),
            points = listOf(
                Pair(30.0, 34.0), Pair(30.0, 48.0), Pair(24.0, 58.0), Pair(15.0, 52.0),
                Pair(12.0, 44.0), Pair(22.0, 38.0), Pair(30.0, 34.0)
            )
        ),
        // Indian Subcontinent
        ContinentData(
            name = "India",
            fillColor = Color(0xFF386641),
            borderColor = Color(0xFF74C69D),
            points = listOf(
                Pair(28.0, 70.0), Pair(25.0, 85.0), Pair(20.0, 88.0), Pair(14.0, 80.0),
                Pair(8.0, 77.0), Pair(16.0, 73.0), Pair(22.0, 68.0), Pair(28.0, 70.0)
            )
        ),
        // Japan Archipelago
        ContinentData(
            name = "Japan",
            fillColor = Color(0xFF2E6F40),
            borderColor = Color(0xFF52B788),
            points = listOf(
                Pair(45.0, 142.0), Pair(42.0, 145.0), Pair(36.0, 140.0), Pair(33.0, 131.0),
                Pair(35.0, 133.0), Pair(40.0, 139.0), Pair(45.0, 142.0)
            )
        ),
        // Australia & New Zealand
        ContinentData(
            name = "Australia",
            fillColor = Color(0xFF9C7A4A), // Australian Outback ochre-gold
            borderColor = Color(0xFFD4A373),
            points = listOf(
                Pair(-12.0, 130.0), Pair(-11.0, 142.0), Pair(-22.0, 150.0), Pair(-32.0, 153.0),
                Pair(-38.0, 148.0), Pair(-37.0, 138.0), Pair(-32.0, 128.0), Pair(-35.0, 116.0),
                Pair(-22.0, 114.0), Pair(-16.0, 122.0), Pair(-12.0, 130.0)
            )
        ),
        // Greenland Ice Sheet
        ContinentData(
            name = "Greenland",
            fillColor = Color(0xFFD8E2DC), // Glacier white-cyan
            borderColor = Color(0xFFE8F1F2),
            points = listOf(
                Pair(82.0, -30.0), Pair(75.0, -20.0), Pair(65.0, -40.0), Pair(60.0, -45.0),
                Pair(68.0, -52.0), Pair(76.0, -60.0), Pair(82.0, -30.0)
            )
        ),
        // Antarctica Polar Shelf
        ContinentData(
            name = "Antarctica",
            fillColor = Color(0xFFE2EAF0),
            borderColor = Color(0xFFFFFFFF),
            points = listOf(
                Pair(-70.0, -180.0), Pair(-70.0, -90.0), Pair(-65.0, -60.0), Pair(-72.0, 0.0),
                Pair(-68.0, 90.0), Pair(-75.0, 160.0), Pair(-70.0, 180.0), Pair(-85.0, 0.0),
                Pair(-70.0, -180.0)
            )
        )
    )

    continentPolygons.forEach { cont ->
        val pathFront = Path()
        var firstFront = true
        var visibleCount = 0

        cont.points.forEach { (lat, lon) ->
            val theta = Math.toRadians(lat)
            val lambda = Math.toRadians(lon)
            val rx = radius * cos(theta) * sin(lambda)
            val ry = -radius * sin(theta)
            val rz = radius * cos(theta) * cos(lambda)

            val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)
            val screenX = cx + p.x
            val screenY = cy + p.y

            if (p.z > 0) {
                visibleCount++
                if (firstFront) {
                    pathFront.moveTo(screenX, screenY)
                    firstFront = false
                } else {
                    pathFront.lineTo(screenX, screenY)
                }
            } else {
                firstFront = true
            }
        }

        if (visibleCount >= 3) {
            // Fill realistic land terrain
            drawPath(
                path = pathFront,
                color = cont.fillColor.copy(alpha = 0.85f),
                style = Fill
            )
            // Crisp natural coastline border
            drawPath(
                path = pathFront,
                color = cont.borderColor.copy(alpha = 0.95f),
                style = Stroke(width = 1.4.dp.toPx())
            )
        }
    }
}

/**
 * Realistic swirling 3D atmospheric cloud clusters floating above the Earth globe.
 */
fun DrawScope.drawAtmosphericClouds(
    cx: Float, cy: Float, radius: Float,
    yaw: Float, pitch: Float,
    cloudWave: Float
) {
    val cloudRadius = radius * 1.018f

    // Swirling atmospheric moisture bands
    val cloudBands = listOf(
        CloudCluster(lat = 42.0, lon = -45.0, size = 65f, density = 0.65f), // North Atlantic
        CloudCluster(lat = 52.0, lon = 5.0, size = 50f, density = 0.70f),   // Western Europe
        CloudCluster(lat = 28.0, lon = 135.0, size = 75f, density = 0.80f), // Western Pacific Typhoon
        CloudCluster(lat = 2.0, lon = 98.0, size = 60f, density = 0.75f),   // Intertropical ITCZ
        CloudCluster(lat = -24.0, lon = -45.0, size = 55f, density = 0.60f), // South Atlantic
        CloudCluster(lat = -42.0, lon = 125.0, size = 70f, density = 0.65f), // Southern Ocean Roaring 40s
        CloudCluster(lat = 15.0, lon = -75.0, size = 55f, density = 0.55f), // Caribbean
        CloudCluster(lat = 60.0, lon = -140.0, size = 65f, density = 0.85f) // Gulf of Alaska Front
    )

    cloudBands.forEach { cloud ->
        val dynamicLon = cloud.lon + (cloudWave * 18.0)
        val theta = Math.toRadians(cloud.lat)
        val lambda = Math.toRadians(dynamicLon)

        val rx = cloudRadius * cos(theta) * sin(lambda)
        val ry = -cloudRadius * sin(theta)
        val rz = cloudRadius * cos(theta) * cos(lambda)

        val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

        if (p.z > 0) {
            val screenX = cx + p.x
            val screenY = cy + p.y
            val puffRadius = (radius * 0.16f) * (cloud.size / 60f)

            // Soft realistic cloud puff with radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(alpha = 0.55f * cloud.density),
                    0.65f to Color(0xFFE2EAF0).copy(alpha = 0.25f * cloud.density),
                    1.0f to Color.Transparent,
                    center = Offset(screenX, screenY),
                    radius = puffRadius
                ),
                radius = puffRadius,
                center = Offset(screenX, screenY)
            )
        }
    }
}

/**
 * Renders atmospheric filters (infrared, wind streams, thermal, co2).
 */
fun DrawScope.drawSatelliteDataLayer(
    cx: Float, cy: Float, radius: Float,
    yaw: Float, pitch: Float,
    filter: SatelliteFilter,
    framePulse: Float
) {
    if (filter == SatelliteFilter.CLOUD_DENSITY) return // default realistic clouds already drawn

    val dataPoints = listOf(
        Pair(35.0, 138.0), Pair(48.0, 2.0), Pair(40.0, -74.0),
        Pair(25.0, 55.0), Pair(-33.0, 151.0), Pair(1.0, 103.0),
        Pair(-22.0, -43.0), Pair(30.0, 31.0), Pair(52.0, 13.0)
    )

    dataPoints.forEach { (lat, lon) ->
        val theta = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)
        val rx = radius * cos(theta) * sin(lambda)
        val ry = -radius * sin(theta)
        val rz = radius * cos(theta) * cos(lambda)
        val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

        if (p.z > 0) {
            val screenX = cx + p.x
            val screenY = cy + p.y

            when (filter) {
                SatelliteFilter.ATMOSPHERE_THERMAL -> {
                    drawCircle(
                        color = DiagnosticOrange.copy(alpha = 0.45f),
                        radius = 16.dp.toPx() * framePulse,
                        center = Offset(screenX, screenY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                SatelliteFilter.WIND_STREAM -> {
                    val arcR = 12.dp.toPx()
                    drawArc(
                        color = GlowingCyan.copy(alpha = 0.7f),
                        startAngle = framePulse * 360f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(screenX - arcR, screenY - arcR),
                        size = Size(arcR * 2, arcR * 2),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                SatelliteFilter.CO2_EMISSIONS -> {
                    drawCircle(
                        color = RadiantPurple.copy(alpha = 0.5f),
                        radius = 14.dp.toPx() * framePulse,
                        center = Offset(screenX, screenY),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * Draws orbiting satellites in outer space.
 */
fun DrawScope.drawSatellites(
    cx: Float, cy: Float, radius: Float,
    yaw: Float, pitch: Float,
    satellites: List<OrbitSatPoint>,
    pulseRadiusFactor: Float,
    pulseAlpha: Float
) {
    satellites.forEach { sat ->
        val theta = Math.toRadians(sat.lat)
        val lambda = Math.toRadians(sat.lon)
        val orbitRadius = radius * 1.28f

        val rx = orbitRadius * cos(theta) * sin(lambda)
        val ry = -orbitRadius * sin(theta)
        val rz = orbitRadius * cos(theta) * cos(lambda)
        val p = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

        val screenX = cx + p.x
        val screenY = cy + p.y
        val alphaMultiplier = if (p.z > 0) 1.0f else 0.2f
        val currentSize = if (p.z > 0) 7.dp.toPx() else 4.dp.toPx()

        drawCircle(
            color = sat.color.copy(alpha = 0.9f * alphaMultiplier),
            radius = currentSize,
            center = Offset(screenX, screenY)
        )

        if (p.z > 0) {
            drawCircle(
                color = sat.color.copy(alpha = pulseAlpha * 0.6f),
                radius = currentSize * 4 * pulseRadiusFactor,
                center = Offset(screenX, screenY),
                style = Stroke(width = 1.dp.toPx())
            )
            drawRect(
                color = sat.color.copy(alpha = 0.45f),
                topLeft = Offset(screenX - currentSize * 1.6f, screenY - currentSize * 1.6f),
                size = Size(currentSize * 3.2f, currentSize * 3.2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

/**
 * Worldwide City Weather Beacons: vertical glowing radar towers projected outward from Earth surface.
 */
fun DrawScope.drawCities(
    cx: Float, cy: Float, radius: Float,
    yaw: Float, pitch: Float,
    cities: List<CityWeather>,
    selectedCity: CityWeather?,
    pulseRadiusFactor: Float,
    pulseAlpha: Float
) {
    cities.forEach { city ->
        val theta = Math.toRadians(city.latitude)
        val lambda = Math.toRadians(city.longitude)

        // Surface coordinate
        val rx = radius * cos(theta) * sin(lambda)
        val ry = -radius * sin(theta)
        val rz = radius * cos(theta) * cos(lambda)
        val pSurface = projectPoint(rx.toFloat(), ry.toFloat(), rz.toFloat(), yaw, pitch)

        if (pSurface.z > 0) {
            val isSelected = selectedCity?.name == city.name
            val pinColor = if (isSelected) DiagnosticOrange else GlowingCyan

            // Tower height in 3D: normal vector elevated outward
            val towerHeight = if (isSelected) 24.dp.toPx() else 10.dp.toPx()
            val nx = rx / radius
            val ny = ry / radius
            val nz = rz / radius
            val pTip = projectPoint(
                (rx + (nx * towerHeight)).toFloat(),
                (ry + (ny * towerHeight)).toFloat(),
                (rz + (nz * towerHeight)).toFloat(),
                yaw, pitch
            )

            val baseScreenX = cx + pSurface.x
            val baseScreenY = cy + pSurface.y
            val tipScreenX = cx + pTip.x
            val tipScreenY = cy + pTip.y

            // Draw vertical beacon line
            drawLine(
                color = pinColor,
                start = Offset(baseScreenX, baseScreenY),
                end = Offset(tipScreenX, tipScreenY),
                strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.2.dp.toPx()
            )

            // Tip Beacon Node
            drawCircle(
                color = pinColor,
                radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                center = Offset(tipScreenX, tipScreenY)
            )

            // Radar pulse ring
            drawCircle(
                color = pinColor.copy(alpha = pulseAlpha),
                radius = (if (isSelected) 18.dp.toPx() else 9.dp.toPx()) * pulseRadiusFactor,
                center = Offset(tipScreenX, tipScreenY),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // If selected: draw leader callout tag
            if (isSelected) {
                val calloutEnd = Offset(tipScreenX + 35.dp.toPx(), tipScreenY - 20.dp.toPx())
                drawLine(
                    color = DiagnosticOrange,
                    start = Offset(tipScreenX, tipScreenY),
                    end = calloutEnd,
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = DiagnosticOrange,
                    start = calloutEnd,
                    end = Offset(calloutEnd.x + 25.dp.toPx(), calloutEnd.y),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

/**
 * Atmospheric limb Fresnel edge ring representing Rayleigh scattering.
 */
fun DrawScope.drawAtmosphericLimb(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0.88f to Color.Transparent,
            0.96f to GlowingCyan.copy(alpha = 0.5f),
            1.0f to Color(0xFF0077B6).copy(alpha = 0.8f),
            center = Offset(cx, cy),
            radius = radius * 1.025f
        ),
        radius = radius * 1.025f,
        center = Offset(cx, cy),
        style = Stroke(width = 3.5.dp.toPx())
    )
}

// Data structures
data class ContinentData(
    val name: String,
    val fillColor: Color,
    val borderColor: Color,
    val points: List<Pair<Double, Double>>
)

data class CloudCluster(
    val lat: Double,
    val lon: Double,
    val size: Float,
    val density: Float
)

data class Point3D(val x: Float, val y: Float, val z: Float)

fun projectPoint(rx: Float, ry: Float, rz: Float, yaw: Float, pitch: Float): Point3D {
    // Yaw around Y-axis
    val x1 = rx * cos(yaw) - rz * sin(yaw)
    val y1 = ry
    val z1 = rx * sin(yaw) + rz * cos(yaw)

    // Pitch around X-axis
    val x2 = x1
    val y2 = y1 * cos(pitch) - z1 * sin(pitch)
    val z2 = y1 * sin(pitch) + z1 * cos(pitch)

    return Point3D(x2, y2, z2)
}
