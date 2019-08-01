package io.add.calendar.adapters

import androidx.databinding.BindingAdapter
import com.chinalwb.are.AREditor

@BindingAdapter("editorContent")
fun AREditor.editorContent(editorContent: String?) {
    editorContent?.let { str -> this.fromHtml(str) }
}
