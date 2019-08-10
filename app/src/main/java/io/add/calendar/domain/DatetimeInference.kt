package io.add.calendar.domain

import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.textclassifier.TextClassification
import androidx.textclassifier.TextClassificationManager
import androidx.textclassifier.TextClassifier
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification
import com.google.firebase.ml.naturallanguage.languageid.IdentifiedLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.EN
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.Date
import java.util.Locale

const val UND = -1

class DatetimeInference(context: Context, _source: String) : IDatetimeInference {
    private lateinit var _translated: String
    private val _adjustedSource: String = _source.trim()
        .replace("\n", "")
        .replace("\t", "")

    override val source: String = _adjustedSource

    override var translated: String
        get() = _translated
        set(value) {
            _translated = value.replace("at", "") // TODO looking for nice solution, see #10
        }

    override val isAlreadyEnglish: Boolean get() = sourceLanguageId == EN

    private val textClassificationManager: TextClassificationManager =
        TextClassificationManager.of(context)

    private var sourceLanguageId: Int = UND

    private val languageIdentifier: FirebaseLanguageIdentification by lazy {
        FirebaseNaturalLanguage.getInstance().languageIdentification
    }

    private val translator: FirebaseTranslator by lazy {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageId)
            .setTargetLanguage(FirebaseTranslateLanguage.EN)
            .build()
        FirebaseNaturalLanguage.getInstance().getTranslator(options)
    }

    override fun release() {
        languageIdentifier.close()
        translator.close()
    }

    override suspend fun doTranslationBeforeClassification(text: String): Calendar? {
        findLanguageId(text)
        translate(text)
        /**
         * Pass translated [text], no more translation more in the [buildResult].
         */
        return buildResult(translated, false)
    }

    override suspend fun doClassificationBeforeTranslation(text: String): Calendar? {
        /**
         * The text will be passed, let [buildResult] do translation before creating [Calendar].
         */
        return buildResult(text, true)
    }

    override suspend fun findLanguageId(text: String) {
        Log.d("+Calendar", "classification finding lang-id on: $text")

        val searchingLanguageId: Task<MutableList<IdentifiedLanguage>> =
            languageIdentifier.identifyPossibleLanguages(text)
        while (!searchingLanguageId.isComplete) continue

        searchingLanguageId.result?.let { langCodeList ->
            val langCode: IdentifiedLanguage? = langCodeList.maxBy { it.confidence }
            sourceLanguageId = langCode?.let {
                FirebaseTranslateLanguage.languageForLanguageCode(it.languageCode)
            } ?: UND
        } ?: run {
            sourceLanguageId = UND
        }
        supportFallbackLanguageIdIfNeeded()
    }

    override suspend fun supportFallbackLanguageIdIfNeeded() {
        if (sourceLanguageId == UND) {
            val fallbackLang = Locale.getDefault().language
            sourceLanguageId =
                FirebaseTranslateLanguage.languageForLanguageCode(fallbackLang) ?: UND

            Log.d("+Calendar", "classification fallback lang: $fallbackLang")
            Log.d("+Calendar", "classification fallback lang-id: $sourceLanguageId")
        } else Log.d("+Calendar", "classification lang-id: $sourceLanguageId")
    }

    override suspend fun translate(text: String) {
        if (isAlreadyEnglish) {
            Log.d(
                "+Calendar",
                "classification avoid translating: $text, detect English: $isAlreadyEnglish"
            )
            translated = text
            return
        }
        Log.d("+Calendar", "classification translating: $text")

        val download = translator.downloadModelIfNeeded()
        while (!download.isComplete) continue

        val translating = translator.translate(text)
        while (!translating.isComplete) continue

        translated = translating.result ?: text

        Log.d("+Calendar", "classification translated: $translated")
    }

    override fun createTextClassification(text: String): TextClassification {
        val classifier: TextClassifier = textClassificationManager.textClassifier
        val builder: TextClassification.Request.Builder =
            TextClassification.Request
                .Builder(text, 0, text.length)
                .setDefaultLocales(
                    LocaleListCompat.getAdjustedDefault()
                )

        return classifier.classifyText(builder.build())
    }

    override suspend fun buildResult(text: String, needTranslation: Boolean): Calendar? {
        val calendar = Calendar.getInstance()
        val classification: TextClassification = createTextClassification(text)

        if (classification.entityTypeCount < 1) return null
        val entity: String = classification.getEntityType(0)
        val score: Float = classification.getConfidenceScore(entity)
        Log.d("+Calendar", "classification Entity: $entity")
        Log.d("+Calendar", "classification Score: $score")
        if (entity == TextClassifier.TYPE_DATE || entity == TextClassifier.TYPE_DATE_TIME) {
            calendar.apply {
                if (needTranslation) {
                    Log.d("+Calendar", "classification need translating: $needTranslation")
                    findLanguageId(text)
                    translate(text)
                }
                @Suppress("DEPRECATION")
                timeInMillis = Date.parse(translated)
            }
            return calendar
        }
        return null
    }
}
