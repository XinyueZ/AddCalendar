package io.add.calendar.domain

import android.icu.util.Calendar
import androidx.textclassifier.TextClassification

/**
 * An inference of different format of date&time to a unified  [Calendar].
 * The inference will be based on an English translation on [source].
 *
 * The [getResult] giving the inference result has two different solutions.
 * See [doTranslationBeforeClassification] and [doClassificationBeforeTranslation].
 */
interface IDatetimeInference {
    /**
     * A data&time text which shall be classified.
     */
    val source: String

    /**
     * A translated text which shall be used to build final [Calendar] by [getResult].
     */
    val translated: String

    /**
     * Translation to English only when [isAlreadyEnglish] return false.
     */
    val isAlreadyEnglish: Boolean

    /**
     * Get result of inference.
     */
    suspend fun getResult(): Calendar?

    /**
     * Do translation on the [text] to [translated] firstly.
     * Use [translated] to complete [TextClassification].
     * Return [Calendar] which is based on [translated].
     */
    suspend fun doTranslationBeforeClassification(text: String): Calendar?

    /**
     * Do classification with [text].
     * Do translation on the [text] to [translated].
     * Return [Calendar] which is based on [translated].
     */
    suspend fun doClassificationBeforeTranslation(text: String): Calendar?

    /**
     * Create an instance of [TextClassification] based on [text].
     */
    fun createTextClassification(text: String): TextClassification

    /**
     * Find language of [text].
     */
    suspend fun findLanguageId(text: String)

    /**
     * Provide solution to find language of the [source] when it is impossible to detect
     * language of [source].
     */
    suspend fun supportFallbackLanguageIdIfNeeded()

    /**
     * Translate [text] to [translated] in English.
     */
    suspend fun translate(text: String)

    /**
     * Build the result [Calendar] object based on English.
     */
    suspend fun buildResult(text: String, needTranslation: Boolean): Calendar?

    /**
     * Dispose all
     */
    fun release()
}
