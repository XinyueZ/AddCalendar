package io.add.calendar.viewmodels

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.add.calendar.BuildConfig
import io.add.calendar.domain.ISetup
import io.add.calendar.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupViewModel(
    app: Application,
    delegate: ISetup
) : AndroidViewModel(app),
    ISetup by delegate {
    val setupInProgress = ObservableBoolean(false)
    val appVersion =
        ObservableField("v${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}")

    private val _onSetupCompleted = MutableLiveData<Event<Unit>>()
    val onSetupCompleted: LiveData<Event<Unit>> = _onSetupCompleted

    private val _onShareApp = MutableLiveData<Event<String>>()
    val onShareApp: LiveData<Event<String>> = _onShareApp

    fun setup() {
        if (setupInProgress.get()) return // Ignore any intercept while setup is in progress.

        setupStart()
        fetchModels()
    }

    /**
     * Download language model which the device's setting.
     */
    private fun fetchModels() {
        viewModelScope.launch(Dispatchers.IO) {
            getModels()
            withContext(Dispatchers.Main) {
                setupCompleted()
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }

    private fun setupStart() {
        setupInProgress.set(true)
    }

    private fun setupCompleted() {
        setupInProgress.set(false)
        _onSetupCompleted.value = Event(Unit)
    }

    fun shareApp(shareText: String) {
        _onShareApp.value = Event(shareText)
    }
}