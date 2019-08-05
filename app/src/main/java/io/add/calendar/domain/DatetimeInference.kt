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
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import java.util.Date
import java.util.Locale

/**
 * An inference of different format of date-time to a unify format [Calendar].
 * This implementation is a English based format.
 *
 * 1. Find the language of date-time.
 * 1.1 When the language cannot be detected, use system language.
 * 2. Translate to English.
 * 3. Based on English format to complete inference.
 */
interface IDatetimeInference {
    /**
     * The source date-time for inference.
     */
    val source: String
    /**
     * The translated date-time based on English.
     */
    val translated: String

    /**
     * Get result of inference.
     */
    suspend fun getResult(): Calendar?

    /**
     * Find language of source.
     */
    suspend fun findLanguageId()

    /**
     * Provide solution to find language of the [source] when it is impossible to detect
     * language of [source].
     */
    suspend fun supportFallbackLanguageIdIfNeeded()

    /**
     * Translate [source] to [translated] in English.
     */
    suspend fun translate()

    /**
     * Build the result [Calendar] object based on English.
     */
    suspend fun buildResult(): Calendar?

    fun release()
}

const val UND = -1

class DatetimeInference(context: Context, private val _source: String) : IDatetimeInference {

    private var _translated: String = ""

    override val source: String
        get() = _source

    override var translated: String
        get() = _translated
        set(value) {
            _translated = value.replace("at", "") // TODO looking for nice solution, see #10
        }

    private val textClassificationManager: TextClassificationManager =
        TextClassificationManager.of(context)
    private val classifier: TextClassifier = textClassificationManager.textClassifier

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

    override suspend fun getResult(): Calendar? {
        findLanguageId()
        translate()
        return buildResult()
    }

    override suspend fun findLanguageId() {
        Log.d("+Calendar", "classification finding lang-id: $source")

        val searchingLanguageId: Task<MutableList<IdentifiedLanguage>> =
            languageIdentifier.identifyPossibleLanguages(source)
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

        Log.d("+Calendar", "classification lang-id: $sourceLanguageId")
    }

    override suspend fun supportFallbackLanguageIdIfNeeded() {
        if (sourceLanguageId == UND) {
            val fallbackLang = Locale.getDefault().language
            sourceLanguageId =
                FirebaseTranslateLanguage.languageForLanguageCode(fallbackLang) ?: UND

            Log.d("+Calendar", "classification fallback lang: $fallbackLang")
        }
    }

    override suspend fun translate() {
        Log.d("+Calendar", "classification translating: $source")

        val download = translator.downloadModelIfNeeded()
        while (!download.isComplete) continue

        val translating = translator.translate(source)
        while (!translating.isComplete) continue

        translated = translating.result ?: source

        Log.d("+Calendar", "classification translated: $translated")
    }

    override suspend fun buildResult(): Calendar? {
        val calendar = Calendar.getInstance()
        val builder: TextClassification.Request.Builder =
            TextClassification.Request
                .Builder(translated, 0, translated.length)
                .setDefaultLocales(
                    LocaleListCompat.getAdjustedDefault()
                )

        val classification: TextClassification = classifier.classifyText(builder.build())
        var entity: String
        var score: Float

        if (classification.entityTypeCount > 0) {
            /**
             * Suppose the datetime entity should have highest score.
             */
            entity = classification.getEntityType(0)
            score = classification.getConfidenceScore(entity)
            Log.d("+Calendar", "classification Entity: $entity")
            Log.d("+Calendar", "classification Score: $score")
            if (entity == TextClassifier.TYPE_DATE || entity == TextClassifier.TYPE_DATE_TIME) {
                calendar.apply {
                    @Suppress("DEPRECATION")
                    timeInMillis = Date.parse(translated)
                }
                return calendar
            } else {
                /**
                 * Looks that the highest score entity is not a datetime, then loop on the whole
                 * list of entity to find one.
                 */
                Log.d("+Calendar", "classification max entity is not datetime, looping to search")
                (0 until classification.entityTypeCount).forEach {
                    entity = classification.getEntityType(it)
                    score = classification.getConfidenceScore(entity)
                    Log.d("+Calendar", "classification Entity: $entity")
                    Log.d("+Calendar", "classification Score: $score")
                    if (entity == TextClassifier.TYPE_DATE || entity == TextClassifier.TYPE_DATE_TIME) {
                        calendar.apply {
                            @Suppress("DEPRECATION")
                            timeInMillis = Date.parse(translated)
                        }
                        return calendar
                    }
                }
                return null
            }
        } else {
            return null
        }
    }

    override fun release() {
        languageIdentifier.close()
        translator.close()
    }
}
