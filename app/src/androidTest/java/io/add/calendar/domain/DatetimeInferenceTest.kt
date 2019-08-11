package io.add.calendar.domain

import android.content.Context
import android.icu.util.Calendar.DAY_OF_MONTH
import android.icu.util.Calendar.HOUR_OF_DAY
import android.icu.util.Calendar.JUNE
import android.icu.util.Calendar.MINUTE
import android.icu.util.Calendar.MONTH
import android.icu.util.Calendar.YEAR
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.DE
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.EN
import io.add.calendar.TEST_CASE_1
import io.add.calendar.TEST_CASE_2
import io.add.calendar.TEST_CASE_3
import io.add.calendar.TEST_CASE_4
import io.add.calendar.TEST_CASE_5
import io.add.calendar.TEST_CASE_FLAG
import io.add.calendar.getRandomBoolean
import io.add.calendar.getRandomInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class DatetimeInferenceTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var inference: IDatetimeInference

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    private val testCase: String
        get() = when (getRandomInt(1, 5)) {
            1 -> TEST_CASE_1
            2 -> TEST_CASE_2
            3 -> TEST_CASE_3
            else -> TEST_CASE_4
        }

    @Test
    fun shouldGiveCalendarAsResultWithInputDateTime() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            val result = inference.getResult()

            assertThat(result).isNotNull()
            requireNotNull(result)

            assertThat(result.get(YEAR)).isEqualTo(1969)
            assertThat(result.get(MONTH)).isEqualTo(JUNE)
            assertThat(result.get(DAY_OF_MONTH)).isEqualTo(20)
            assertThat(result.get(HOUR_OF_DAY)).isEqualTo(20)
            assertThat(result.get(MINUTE)).isEqualTo(17)
        }.join()
    }

    @Test
    fun shouldFindEnglish() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.findLanguageId(TEST_CASE_1)
            assertThat(inference.sourceLanguageId).isEqualTo(EN)
            assertThat(inference.isAlreadyEnglish).isTrue()
        }.join()
    }

    @Test
    fun shouldNotFindLanguageId() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.findLanguageId(TEST_CASE_5, false)
            assertThat(inference.sourceLanguageId).isEqualTo(UND)
            assertThat(inference.isAlreadyEnglish).isFalse()
        }.join()
    }

    @Test
    fun shouldAvoidTranslatingWhenTheLanguageIsEnglish() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.findLanguageId(TEST_CASE_1)
            inference.translate(TEST_CASE_1)
            assertThat(inference.sourceLanguageId).isNotEqualTo(UND)
            assertThat(inference.isAlreadyEnglish).isTrue()
            /**
             * Because is already identified as English,
             * any language can avoid being translated.
             *
             * Only for test purpose.
             */
            inference.translate(TEST_CASE_FLAG)
            assertThat(inference.translated).isEqualTo(TEST_CASE_FLAG)
        }.join()
    }

    @Test
    fun shouldAvoidTranslatingWhenLanguageCannotBeDetected() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.findLanguageId(TEST_CASE_5, false)
            inference.translate(TEST_CASE_5)
            assertThat(inference.sourceLanguageId).isEqualTo(UND)
            assertThat(inference.isAlreadyEnglish).isFalse()
            assertThat(inference.translated).isEqualTo(TEST_CASE_5)
        }.join()
    }

    @Test
    fun shouldUseFallbackSupportToFindLanguage() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase) { "de" }
            inference.findLanguageId(TEST_CASE_5, false)
            assertThat(inference.sourceLanguageId).isEqualTo(UND)
            inference.findLanguageId(TEST_CASE_5)
            assertThat(inference.sourceLanguageId).isEqualTo(DE)
        }.join()
    }

    @Test
    fun shouldBuildResultNullWhenTheTranslatedIsBad() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.translated = TEST_CASE_5
            val result = inference.buildResult(TEST_CASE_5, getRandomBoolean())
            assertThat(result).isNull()
        }.join()
    }

    @Test
    fun shouldBuildResultWithoutTranslation() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            inference.translated = TEST_CASE_1

            val result = inference.buildResult(TEST_CASE_1, false)

            assertThat(inference.sourceLanguageId).isEqualTo(UND)
            assertThat(inference.isAlreadyEnglish).isFalse()

            requireNotNull(result)
            assertThat(result.get(YEAR)).isEqualTo(1969)
            assertThat(result.get(MONTH)).isEqualTo(JUNE)
            assertThat(result.get(DAY_OF_MONTH)).isEqualTo(20)
            assertThat(result.get(HOUR_OF_DAY)).isEqualTo(20)
            assertThat(result.get(MINUTE)).isEqualTo(17)
        }.join()
    }

    @Test
    fun shouldBuildResultWithTranslation() = runBlocking {
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase) { "de" }

            val result = inference.buildResult(TEST_CASE_4, true)

            assertThat(inference.sourceLanguageId).isNotEqualTo(DE)
            assertThat(inference.translated).isEqualTo(TEST_CASE_1)

            requireNotNull(result)
            assertThat(result.get(YEAR)).isEqualTo(1969)
            assertThat(result.get(MONTH)).isEqualTo(JUNE)
            assertThat(result.get(DAY_OF_MONTH)).isEqualTo(20)
            assertThat(result.get(HOUR_OF_DAY)).isEqualTo(20)
            assertThat(result.get(MINUTE)).isEqualTo(17)
        }.join()
    }
}
