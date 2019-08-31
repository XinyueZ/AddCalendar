package io.add.calendar.domain

import android.content.Context
import android.icu.util.Calendar
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.getSystemService
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

/**
 * Factory gives fallback to identify the language to inference.
 */
typealias FallbackLanguageProvider = () -> String

/**
 * [DatetimeInference] gives English based data&time format inference on [_source].
 * [fallbackLanguage] is a factory which gives chance to fallback language-id on [_source].
 * Default of [fallbackLanguage] is the language of the user's sim provider.
 */
class DatetimeInference(
    context: Context,
    _source: String,
    private val fallbackLanguage: FallbackLanguageProvider = {
        val telephonyManager: TelephonyManager? = context.getSystemService()
        telephonyManager?.simCountryIso ?: Locale.getDefault().language
    }
) : IDatetimeInference {

    private val _adjustedSource: String = _source
        .trim(',', '#', ';', ' ', '\t', '\n')
        .replace("\n", "")
        .replace("\t", "")
    override val source: String = _adjustedSource

    private var _sourceLanguageId: Int = UND
    override var sourceLanguageId: Int
        get() = _sourceLanguageId
        set(value) {
            _sourceLanguageId = value
        }

    private lateinit var _translated: String
    override var translated: String
        get() = _translated
        set(value) {
            _translated = value.replace("at", "") // TODO looking for nice solution, see #10
        }

    override val isAlreadyEnglish: Boolean get() = sourceLanguageId == EN

    private val textClassificationManager: TextClassificationManager =
        TextClassificationManager.of(context)

    private val languageIdentifier: FirebaseLanguageIdentification by lazy {
        FirebaseNaturalLanguage.getInstance().languageIdentification
    }

    private val translator: FirebaseTranslator by lazy {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageId)
            .setTargetLanguage(EN)
            .build()
        FirebaseNaturalLanguage.getInstance().getTranslator(options)
    }

    override fun release() {
        languageIdentifier.close()
        translator.close()
    }

    override suspend fun getResult(): Calendar? =
        doClassificationBeforeTranslation(source)
            ?: doTranslationBeforeClassification(source)

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

    override suspend fun findLanguageId(text: String, supportFallback: Boolean) {
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

        if (supportFallback) {
            supportFallbackLanguageIdIfNeeded()
        }
    }

    override suspend fun supportFallbackLanguageIdIfNeeded() {
        if (sourceLanguageId == UND) {
            sourceLanguageId =
                FirebaseTranslateLanguage.languageForLanguageCode(fallbackLanguage()) ?: UND

            Log.d("+Calendar", "classification fallback lang: $fallbackLanguage")
            Log.d("+Calendar", "classification fallback lang-id: $sourceLanguageId")
        } else Log.d("+Calendar", "classification lang-id: $sourceLanguageId")
    }

    override suspend fun translate(text: String) {
        if (sourceLanguageId == UND) {
            Log.d(
                "+Calendar",
                "classification avoid translating: $text, cannot detect language"
            )
            translated = text
            return
        }

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
        try {
            val calendar = Calendar.getInstance()
            val classification: TextClassification = createTextClassification(text)

            if (classification.entityTypeCount < 1) return null

            val entity: String = classification.getEntityType(0)
            val score: Float = classification.getConfidenceScore(entity)
            Log.d("+Calendar", "classification Entity: $entity")
            Log.d("+Calendar", "classification Score: $score")
            return if (entity == TextClassifier.TYPE_DATE || entity == TextClassifier.TYPE_DATE_TIME) {
                calendar.apply {
                    if (needTranslation) {
                        Log.d("+Calendar", "classification need translating: $needTranslation")
                        findLanguageId(text)
                        translate(text)
                    }
                    @Suppress("DEPRECATION")
                    timeInMillis = Date.parse(translated)
                    // timeInMillis = valueOf(LocalDateTime.parse(translated,    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.ENGLISH)).toString()).toString().toLong()
                }
            } else null
        } catch (ex: Exception) {
            return null
        }
    }
}
