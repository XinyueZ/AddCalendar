package io.add.calendar.viewmodels

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel
import io.add.calendar.utils.Event
import java.util.Locale

class SetupViewModel(app: Application) : AndroidViewModel(app) {
    val setupInProgress = ObservableBoolean(false)

    private val _onSetupCompleted = MutableLiveData<Event<Unit>>()
    val onSetupCompleted: LiveData<Event<Unit>> = _onSetupCompleted

    fun setup() {
        if (setupInProgress.get()) return // Ignore any intercept while setup is in progress.

        setupStart()
        getModels()
    }

    /**
     * Download language model which the device's setting.
     */
    private fun getModels() {
        val langId =
            FirebaseTranslateLanguage.languageForLanguageCode(Locale.getDefault().language) ?: -1
        if (langId > 0) {
            val langModel: FirebaseTranslateRemoteModel =
                FirebaseTranslateRemoteModel.Builder(langId).build()
            val modelManager: FirebaseTranslateModelManager =
                FirebaseTranslateModelManager.getInstance()
            modelManager.downloadRemoteModelIfNeeded(langModel)
                .addOnSuccessListener {
                    setupCompleted()
                }
                .addOnFailureListener {
                    setupCompleted()
                }
        } else {
            setupCompleted()
        }
    }

    private fun setupStart() {
        setupInProgress.set(true)
    }

    private fun setupCompleted() {
        setupInProgress.set(false)
        _onSetupCompleted.value = Event(Unit)
    }
}
