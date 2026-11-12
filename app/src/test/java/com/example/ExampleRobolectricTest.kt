package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ColombianCities
import com.example.manager.AppManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("J.A.R.V.I.S. HOME", appName)
    }

    @Test
    fun `verify default colombian city is Bogota`() {
        val defaultCity = ColombianCities.defaultCity
        assertEquals("Bogotá", defaultCity.name)
        assertEquals("Cundinamarca", defaultCity.department)
        assertTrue(ColombianCities.list.isNotEmpty())
    }

    @Test
    fun `verify AppManager primary apps configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appManager = AppManager(context)
        val primaryApps = appManager.getPrimaryApps()

        assertEquals(5, primaryApps.size)
        val appIds = primaryApps.map { it.id }
        assertTrue(appIds.contains("youtube"))
        assertTrue(appIds.contains("netflix"))
        assertTrue(appIds.contains("prime_video"))
        assertTrue(appIds.contains("play_store"))
        assertTrue(appIds.contains("chatgpt"))
    }
}
