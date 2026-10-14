package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OrbitWeather 3D", appName)
  }

  @Test
  fun `verify historical weather generation and statistical summary`() {
    val repository = WeatherRepository()
    val history7Days = repository.getHistoricalWeather("Tokyo", 7)
    assertEquals(7, history7Days.size)

    val history30Days = repository.getHistoricalWeather("London", 30)
    assertEquals(30, history30Days.size)

    val summary = repository.computeSummary(history7Days)
    org.junit.Assert.assertTrue("Summary max temp should be >= min temp", summary.maxTemp >= summary.minTemp)
    org.junit.Assert.assertTrue("Summary total precipitation should be non-negative", summary.totalPrecipitation >= 0.0)
  }

  @Test
  fun `verify world locations search and coordinate solver`() {
    val repository = WeatherRepository()
    val searchParis = repository.searchLocations("Paris")
    assertEquals(1, searchParis.size)
    assertEquals("France", searchParis.first().country)

    val searchEurope = repository.searchLocations("Europe")
    org.junit.Assert.assertTrue("Should find multiple European cities", searchEurope.size >= 4)

    val customCoord = repository.findByCoordinates(48.8566, 2.3522)
    org.junit.Assert.assertNotNull(customCoord)
    assertEquals("Europe", customCoord.country)
  }
}
