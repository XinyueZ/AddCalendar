package io.add.calendar.domain

import android.util.Log
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateModelManager
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel

class Setup : AbstractSetup() {
    override suspend fun downloadModels(
        forceDownload: Boolean,
        vararg langNames: String
    ) {
        langNames.forEach { langName ->
            val langId =
                FirebaseTranslateLanguage.languageForLanguageCode(langName) ?: -1
            if (langId > 0) {
                val langModel: FirebaseRemoteModel =
                    createRemoteModel(forceDownload, langId)
                val download =
                    FirebaseTranslateModelManager.getInstance()
                        .downloadRemoteModelIfNeeded(langModel)
                while (!download.isComplete) continue
                Log.d(
                    "+Calendar",
                    "language download $langName successfully: ${download.isSuccessful}"
                )
            }
        }
    }

    override suspend fun createRemoteModel(
        forceDownload: Boolean,
        langId: Int
    ): FirebaseRemoteModel =
        if (forceDownload) {
            // These languages must be downloaded.
            FirebaseTranslateRemoteModel.Builder(langId).build()
        } else {
            // These languages can be downloaded under some hardware, environment conditions.
            FirebaseTranslateRemoteModel.Builder(langId).setDownloadConditions(
                FirebaseModelDownloadConditions.Builder().requireWifi().build()
            ).build()
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
