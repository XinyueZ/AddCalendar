package io.add.calendar.viewmodels

import android.app.Application
import android.content.ContentUris
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import io.add.calendar.R
import io.add.calendar.domain.DatetimeInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var datetimeInference: DatetimeInference? = null

    val openDateChooser = View.OnClickListener {
        it.findNavController().navigate(R.id.date_fragment)
    }

    val openTimeChooser = View.OnClickListener {
        it.findNavController().navigate(R.id.time_fragment)
    }

    val calendarChooser = View.OnClickListener {
        val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(builder, Calendar.getInstance().timeInMillis)
        val intent = Intent(Intent.ACTION_EDIT).apply {
            data = builder.build()
            type = "vnd.android.cursor.item/event"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, Calendar.getInstance().timeInMillis)
            putExtra(CalendarContract.Events.TITLE, "For some testings")
            putExtra(CalendarContract.Events.DESCRIPTION, "bla bla bla testings")
        }
        startActivity(it.context, intent, Bundle.EMPTY)
    }

    val useEditorToolbar = ObservableBoolean(false)

    val editorContent = ObservableField<String>()

    val selectedDatetimeText = ObservableField<String>()

    private val onSelectedDatetimeTextChangedCallback: OnPropertyChangedCallback =
        object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                selectedDatetimeText.get()?.let { datetimeText ->
                    datetimeInference = DatetimeInference(getApplication(), datetimeText)
                    viewModelScope.launch(Dispatchers.IO) {
                        val calendar: Calendar? = datetimeInference?.getResult()
                        withContext(Dispatchers.Main) {
                            calendar?.let {
                                Log.d(
                                    "+Calendar",
                                    "classification Calendar: ${calendar.time}"
                                )
                            } ?: Log.d(
                                "+Calendar",
                                "classification Calendar: $calendar"
                            )
                        }
                    }
                } ?: Log.d(
                    "+Calendar",
                    "classification Calendar: empty selected datetime"
                )
            }
        }

    init {
        selectedDatetimeText.addOnPropertyChangedCallback(onSelectedDatetimeTextChangedCallback)
    }

    override fun onCleared() {
        selectedDatetimeText.removeOnPropertyChangedCallback(onSelectedDatetimeTextChangedCallback)
        viewModelScope.cancel()
        datetimeInference?.release()
        super.onCleared()
    }

    fun toolbarToggle(isOpen: Boolean) {
        useEditorToolbar.set(isOpen)
    }
}
