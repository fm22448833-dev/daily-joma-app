package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.BengaliNumberUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Daily Joma", appName)
  }

  @Test
  fun `bengali number converter test`() {
    val result = BengaliNumberUtils.toBangla(100)
    assertEquals("১০০", result)

    val taka = BengaliNumberUtils.formatTaka(100.0)
    assertEquals("৳ ১০০", taka)
  }
}
