package io.add.calendar.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat

inline fun <reified T : Activity> Activity.startSingleTopActivity() {
    val intent = Intent(this, T::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    ActivityCompat.startActivity(this, intent, Bundle.EMPTY)
}
