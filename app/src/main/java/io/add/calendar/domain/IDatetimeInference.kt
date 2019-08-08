package io.add.calendar.domain

import android.icu.util.Calendar

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