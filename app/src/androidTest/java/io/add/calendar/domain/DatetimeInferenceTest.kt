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
import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.add.calendar.TEST_CASE_1
import io.add.calendar.TEST_CASE_2
import io.add.calendar.TEST_CASE_3
import io.add.calendar.TEST_CASE_4
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
    private lateinit var mockDelegate: IDatetimeInference

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var inference: IDatetimeInference

    @Before
    fun setup() {
        mockDelegate = mock()
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
            Truth.assertThat(result).isNotNull()
            requireNotNull(result)
            Truth.assertThat(result.get(YEAR)).isEqualTo(1969)
            Truth.assertThat(result.get(MONTH)).isEqualTo(JUNE)
            Truth.assertThat(result.get(DAY_OF_MONTH)).isEqualTo(20)
            Truth.assertThat(result.get(HOUR_OF_DAY)).isEqualTo(20)
            Truth.assertThat(result.get(MINUTE)).isEqualTo(17)
        }.join()
    }

    @Test
    fun shouldDoTranslationBeforeClassification() = runBlocking {
        launch(Dispatchers.Main) {
            val case = testCase
            inference = DatetimeInference(context, case, mockDelegate)
            whenever(mockDelegate.doClassificationBeforeTranslation((case))).thenReturn(
                null
            )
            inference.getResult()
            verify(mockDelegate, times(1)).doTranslationBeforeClassification(case)
        }.join()
    }
}