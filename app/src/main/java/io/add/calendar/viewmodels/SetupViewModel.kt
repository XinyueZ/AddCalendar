package io.add.calendar.viewmodels

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel
import io.add.calendar.BuildConfig
import io.add.calendar.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SetupViewModel(app: Application) : AndroidViewModel(app) {
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
        getModels()
    }

    /**
     * Download language model which the device's setting.
     */
    private fun getModels() {

        fun createTranslateRemoteModel(
            forceDownload: Boolean,
            langId: Int
        ): FirebaseTranslateRemoteModel = if (forceDownload) {
            // These languages must be downloaded.
            FirebaseTranslateRemoteModel.Builder(langId).build()
        } else {
            // These languages can be downloaded under some hardware, environment conditions.
            FirebaseTranslateRemoteModel.Builder(langId).setDownloadConditions(
                FirebaseModelDownloadConditions.Builder().requireWifi().build()
            ).build()
        }

        fun downloadModels(forceDownload: Boolean, vararg langNames: String) {
            langNames.forEach { langName ->
                val langId =
                    FirebaseTranslateLanguage.languageForLanguageCode(langName) ?: -1
                if (langId > 0) {
                    val langModel: FirebaseTranslateRemoteModel =
                        createTranslateRemoteModel(forceDownload, langId)
                    val download =
                        FirebaseTranslateModelManager.getInstance()
                            .downloadRemoteModelIfNeeded(langModel)
                    while (!download.isComplete) continue
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            downloadModels(
                true,
                Locale.getDefault().language,
                "en"
            )
            downloadModels(
                false,
                "de"
            )
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

// "af",
// "ar",
// "be",
// "bg",
// "bn",
// "ca",
// "cs",
// "cy",
// "da",
// "de",
// "el",
// "en",
// "eo",
// "es",
// "et",
// "fa",
// "fi",
// "fr",
// "ga",
// "gl",
// "gu",
// "he",
// "hi",
// "hr",
// "ht",
// "hu",
// "id",
// "is",
// "it",
// "ja",
// "ka",
// "kn",
// "ko",
// "lt",
// "lv",
// "mk",
// "mr",
// "ms",
// "mt",
// "nl",
// "no",
// "pl",
// "pt",
// "ro",
// "ru",
// "sk",
// "sl",
// "sq",
// "sv",
// "sw",
// "ta",
// "te",
// "th",
// "tl",
// "tr",
// "uk",
// "ur",
// "vi",
// "zh"
