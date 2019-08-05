package io.add.calendar.adapters

import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.databinding.BindingAdapter

@BindingAdapter("src")
fun VideoView.setSource(filename: String) {
    val srcUri = Uri.parse("android.resource://${context.packageName}/raw/$filename")
    setMediaController(MediaController(context).apply { this.visibility = View.GONE })
    setVideoURI(Uri.parse(srcUri.toString()))
    seekTo(0)
    requestFocus()
    setOnPreparedListener {
        it.isLooping = true
        it.setVolume(0f, 0f)
        it.isLooping = true
        start()
    }
}

@BindingAdapter("startAnimation")
fun ImageView.startAnimation(start: Boolean) {
    if (start) {
        (this.drawable as? AnimatedVectorDrawable)?.start()
    }
}
