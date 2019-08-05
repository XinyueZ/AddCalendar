package io.add.calendar.extensions

import android.app.Activity
import android.content.Intent
import androidx.core.app.ShareCompat

fun Activity.shareCompat(shareText: String) {
    val shareIntent = ShareCompat.IntentBuilder.from(this)
        .setText(shareText)
        .setType("text/plain")
        .createChooserIntent()
        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK) }
    startActivity(shareIntent)
}
