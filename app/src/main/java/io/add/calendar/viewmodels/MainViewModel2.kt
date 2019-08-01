package io.add.calendar.viewmodels

import android.app.Application
import android.content.ContentUris
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.add.calendar.domain.DatetimeInference
import io.add.calendar.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class MainViewModel2(app: Application) : AndroidViewModel(app) {

    private var datetimeInference: DatetimeInference? = null

    private var _onInferenceFinished = MutableLiveData<Event<Boolean>>()
    val onInferenceFinished: LiveData<Event<Boolean>> = _onInferenceFinished

    private var _onPanic = MutableLiveData<Event<Boolean>>()
    val onPanic: LiveData<Event<Boolean>> = _onPanic

    var selectedText: String by Delegates.observable("") { _, _, newValue ->
        if (newValue.isNotBlank()) inference(newValue)
        else _onPanic.value = Event(true)
    }

    private fun openCalendar(calendar: Calendar) {
        val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(builder, calendar.timeInMillis)
        val intent = Intent(Intent.ACTION_EDIT).apply {
            data = builder.build()
            type = "vnd.android.cursor.item/event"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
            putExtra(CalendarContract.Events.TITLE, "")
            putExtra(CalendarContract.Events.DESCRIPTION, "")
        }
        ContextCompat.startActivity(getApplication(), intent, Bundle.EMPTY)
    }

    private fun inference(text: String) {
        datetimeInference = DatetimeInference(getApplication(), text)
        viewModelScope.launch(Dispatchers.IO) {
            val calendar: Calendar? = datetimeInference?.getResult()
            withContext(Dispatchers.Main) {
                calendar?.let {
                    openCalendar(calendar)
                } ?: run {
                    _onPanic.value = Event(true)
                }
                _onInferenceFinished.value = Event(true)
            }
        }
    }

    fun cancel() {
        viewModelScope.cancel()
        datetimeInference?.release()
        _onInferenceFinished.value = Event(true)
    }

    override fun onCleared() {
        cancel()
        super.onCleared()
    }
}
