package io.add.calendar.domain

import android.content.Context
import android.icu.util.Calendar.DAY_OF_MONTH
import android.icu.util.Calendar.HOUR_OF_DAY
import android.icu.util.Calendar.JULY
import android.icu.util.Calendar.MINUTE
import android.icu.util.Calendar.MONTH
import android.icu.util.Calendar.YEAR
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import io.add.calendar.TEST_CASE_1
import io.add.calendar.TEST_CASE_2
import io.add.calendar.getRandomBoolean
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
        get() = when (getRandomBoolean()) {
            true -> TEST_CASE_1
            false -> TEST_CASE_2
        }

    @Test
    fun shouldGiveCalendarAsResultWithInputDateTime() = runBlocking {
        /**
         *  "July 20, 1969, 20:17"
         *  "July 20, 1969, at 20:17"
         */
        launch(Dispatchers.Main) {
            inference = DatetimeInference(context, testCase)
            val result = inference.getResult()
            Truth.assertThat(result).isNotNull()
            requireNotNull(result)
            Truth.assertThat(result.get(YEAR)).isEqualTo(1969)
            Truth.assertThat(result.get(MONTH)).isEqualTo(JULY)
            Truth.assertThat(result.get(DAY_OF_MONTH)).isEqualTo(20)
            Truth.assertThat(result.get(HOUR_OF_DAY)).isEqualTo(20)
            Truth.assertThat(result.get(MINUTE)).isEqualTo(17)
        }.join()
    }
}