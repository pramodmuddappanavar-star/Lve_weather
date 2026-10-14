package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricalWeatherDay
import com.example.data.model.MetricType
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun HistoricalWeatherChart(
    days: List<HistoricalWeatherDay>,
    selectedMetric: MetricType,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, GlowingCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("historical_chart_container")
    ) {
        // Active metric indicator & scrubber tooltip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (selectedMetric) {
                    MetricType.TEMPERATURE -> Icons.Default.Thermostat
                    MetricType.PRECIPITATION -> Icons.Default.WaterDrop
                    MetricType.WIND -> Icons.Default.Air
                }
                val iconColor = when (selectedMetric) {
                    MetricType.TEMPERATURE -> DiagnosticOrange
                    MetricType.PRECIPITATION -> GlowingCyan
                    MetricType.WIND -> RadarGreen
                }
                Icon(
                    imageVector = icon,
                    contentDescription = selectedMetric.label,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${selectedMetric.label.uppercase()} ARCHIVE TREND",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Text(
                text = "TOUCH TO INSPECT",
                color = SoftGrayText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Day Scrubber Banner
        val activeDay = selectedIndex?.let { days.getOrNull(it) } ?: days.lastOrNull()
        if (activeDay != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavy)
                    .border(1.dp, GlowingCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activeDay.dateFormatted,
                            color = GlowingCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeDay.condition,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }

                    when (selectedMetric) {
                        MetricType.TEMPERATURE -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Max ${activeDay.tempMax}°",
                                    color = DiagnosticOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Min ${activeDay.tempMin}°",
                                    color = GlowingCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        MetricType.PRECIPITATION -> {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${activeDay.precipitationMm} mm Rain",
                                    color = GlowingCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeDay.precipitationChance}% chance",
                                    color = SoftGrayText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        MetricType.WIND -> {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${activeDay.windSpeedAvg} km/h • Gust ${activeDay.windSpeedMax}",
                                    color = RadarGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeDay.windDirectionText} (${activeDay.windDirection.toInt()}°)",
                                    color = SoftGrayText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Canvas Area
        AnimatedContent(
            targetState = selectedMetric,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "chart_content"
        ) { metric ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(days) {
                        detectTapGestures { offset ->
                            val n = days.size
                            if (n > 0) {
                                val step = size.width / (n - 1).coerceAtLeast(1)
                                val index = ((offset.x + (step / 2)) / step).toInt().coerceIn(0, n - 1)
                                onSelectDay(index)
                            }
                        }
                    }
                    .pointerInput(days) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val n = days.size
                            if (n > 0) {
                                val step = size.width / (n - 1).coerceAtLeast(1)
                                val index = ((change.position.x + (step / 2)) / step).toInt().coerceIn(0, n - 1)
                                onSelectDay(index)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val n = days.size
                    if (n <= 0) return@Canvas

                    // Draw subtle grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = (h / gridSteps) * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    when (metric) {
                        MetricType.TEMPERATURE -> {
                            // Compute global min and max temp for scaling
                            var minVal = days.minOf { it.tempMin }
                            var maxVal = days.maxOf { it.tempMax }
                            if (maxVal == minVal) {
                                maxVal += 5.0
                                minVal -= 5.0
                            }
                            // Add padding to range
                            val range = (maxVal - minVal) * 1.2
                            val baseMin = minVal - (range * 0.1)

                            fun toY(temp: Double): Float {
                                val norm = ((temp - baseMin) / range).coerceIn(0.0, 1.0)
                                return (h - (norm * h)).toFloat()
                            }

                            val stepX = if (n > 1) w / (n - 1) else w / 2

                            // Build paths
                            val maxPath = Path()
                            val minPath = Path()
                            val fillAreaPath = Path()

                            val maxPoints = mutableListOf<Offset>()
                            val minPoints = mutableListOf<Offset>()

                            for (i in 0 until n) {
                                val x = if (n > 1) i * stepX else w / 2
                                val yMax = toY(days[i].tempMax)
                                val yMin = toY(days[i].tempMin)
                                val maxPt = Offset(x, yMax)
                                val minPt = Offset(x, yMin)
                                maxPoints.add(maxPt)
                                minPoints.add(minPt)

                                if (i == 0) {
                                    maxPath.moveTo(maxPt.x, maxPt.y)
                                    minPath.moveTo(minPt.x, minPt.y)
                                } else {
                                    maxPath.lineTo(maxPt.x, maxPt.y)
                                    minPath.lineTo(minPt.x, minPt.y)
                                }
                            }

                            // Fill between max and min curves
                            fillAreaPath.moveTo(maxPoints.first().x, maxPoints.first().y)
                            for (pt in maxPoints) fillAreaPath.lineTo(pt.x, pt.y)
                            for (j in minPoints.indices.reversed()) fillAreaPath.lineTo(minPoints[j].x, minPoints[j].y)
                            fillAreaPath.close()

                            drawPath(
                                path = fillAreaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        DiagnosticOrange.copy(alpha = 0.25f),
                                        GlowingCyan.copy(alpha = 0.15f)
                                    )
                                )
                            )

                            // Draw min temp line (Cyan)
                            drawPath(
                                path = minPath,
                                color = GlowingCyan,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw max temp line (Orange)
                            drawPath(
                                path = maxPath,
                                color = DiagnosticOrange,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Draw data nodes
                            for (i in 0 until n) {
                                val isSel = selectedIndex == i
                                val maxPt = maxPoints[i]
                                val minPt = minPoints[i]

                                // Orange max point
                                drawCircle(
                                    color = DiagnosticOrange,
                                    radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx(),
                                    center = maxPt
                                )
                                // Cyan min point
                                drawCircle(
                                    color = GlowingCyan,
                                    radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx(),
                                    center = minPt
                                )

                                if (isSel) {
                                    // Highlight vertical scrubber bar
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.45f),
                                        start = Offset(maxPt.x, 0f),
                                        end = Offset(maxPt.x, h),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }
                        }

                        MetricType.PRECIPITATION -> {
                            val maxMm = (days.maxOfOrNull { it.precipitationMm } ?: 10.0).coerceAtLeast(5.0) * 1.2
                            val barWidth = (w / (n * 1.6f)).coerceIn(6.dp.toPx(), 22.dp.toPx())
                            val stepX = w / n

                            for (i in 0 until n) {
                                val day = days[i]
                                val barHeight = ((day.precipitationMm / maxMm) * h).toFloat().coerceAtLeast(3.dp.toPx())
                                val cx = (i * stepX) + (stepX / 2)
                                val left = cx - (barWidth / 2)
                                val top = h - barHeight
                                val isSel = selectedIndex == i

                                val barBrush = Brush.verticalGradient(
                                    colors = if (day.precipitationMm > 0) {
                                        listOf(GlowingCyan, Color(0xFF0055FF))
                                    } else {
                                        listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                                    },
                                    startY = top,
                                    endY = h
                                )

                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(left, top),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )

                                if (isSel) {
                                    // Bounding aura for selected bar
                                    drawRoundRect(
                                        color = GlowingCyan,
                                        topLeft = Offset(left - 2.dp.toPx(), top - 2.dp.toPx()),
                                        size = Size(barWidth + 4.dp.toPx(), barHeight + 2.dp.toPx()),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                }
                            }
                        }

                        MetricType.WIND -> {
                            val maxWind = (days.maxOfOrNull { it.windSpeedMax } ?: 30.0).coerceAtLeast(20.0) * 1.2
                            fun toY(speed: Double): Float {
                                val norm = (speed / maxWind).coerceIn(0.0, 1.0)
                                return (h - (norm * h)).toFloat()
                            }

                            val stepX = if (n > 1) w / (n - 1) else w / 2

                            val avgPath = Path()
                            val gustPath = Path()

                            for (i in 0 until n) {
                                val x = if (n > 1) i * stepX else w / 2
                                val yAvg = toY(days[i].windSpeedAvg)
                                val yGust = toY(days[i].windSpeedMax)

                                if (i == 0) {
                                    avgPath.moveTo(x, yAvg)
                                    gustPath.moveTo(x, yGust)
                                } else {
                                    avgPath.lineTo(x, yAvg)
                                    gustPath.lineTo(x, yGust)
                                }
                            }

                            // Peak gusts dashed/glow line
                            drawPath(
                                path = gustPath,
                                color = SatelliteHot.copy(alpha = 0.65f),
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Average wind speed line
                            drawPath(
                                path = avgPath,
                                color = RadarGreen,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            for (i in 0 until n) {
                                val isSel = selectedIndex == i
                                val x = if (n > 1) i * stepX else w / 2
                                val yAvg = toY(days[i].windSpeedAvg)

                                drawCircle(
                                    color = RadarGreen,
                                    radius = if (isSel) 6.dp.toPx() else 3.5.dp.toPx(),
                                    center = Offset(x, yAvg)
                                )

                                if (isSel) {
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.45f),
                                        start = Offset(x, 0f),
                                        end = Offset(x, h),
                                        strokeWidth = 1.5.dp.toPx()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart X-Axis Labels (Dates)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelCount = min(5, days.size)
            if (days.size <= 7) {
                // Display every day short name
                days.forEachIndexed { idx, day ->
                    val isSel = selectedIndex == idx
                    Text(
                        text = day.dateFormatted.take(3),
                        color = if (isSel) GlowingCyan else SoftGrayText,
                        fontSize = 9.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else {
                // Sample 5 evenly distributed dates
                for (k in 0 until labelCount) {
                    val idx = ((k.toDouble() / (labelCount - 1)) * (days.size - 1)).toInt()
                    val day = days[idx]
                    Text(
                        text = "${day.date.monthValue}/${day.date.dayOfMonth}",
                        color = if (selectedIndex == idx) GlowingCyan else SoftGrayText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
