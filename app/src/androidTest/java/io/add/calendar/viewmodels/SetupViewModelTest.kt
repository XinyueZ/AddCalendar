package io.add.calendar.viewmodels

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import io.add.calendar.utils.getLiveDataValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
class SetupViewModelTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var setupViewModel: SetupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        setupViewModel = SetupViewModel(
            context.applicationContext as Application,
            mock()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun shouldSeeShareTextWhenCallShareApp() {
        val expectText = "hello,world"
        setupViewModel.shareApp(expectText)
        val onShareText = getLiveDataValue(setupViewModel.onShareApp)
        assertThat(onShareText()).isEqualTo(expectText)
    }

    @Test
    fun shouldEchoSetupInProgress() = runBlocking {
        setupViewModel.setup()
        assertThat(setupViewModel.setupInProgress.get()).isTrue()
    }

    @Test
    fun shouldIgnoreSetupIfSetupIsInProcess() = runBlocking {
        val setupJob1 = setupViewModel.setup()
        assertThat(setupViewModel.setupInProgress.get()).isTrue()
        val setupJob2 = setupViewModel.setup()
        assertThat(setupViewModel.setupInProgress.get()).isTrue()
        assertThat(setupJob1).isEqualTo(setupJob2)
    }

    @Test
    fun shouldEchoSetupComplete() = runBlocking {
        setupViewModel
            .setup()
            .join()
        assertThat(setupViewModel.setupInProgress.get()).isFalse()
        val onSetupCompleted = getLiveDataValue(setupViewModel.onSetupCompleted)
        assertThat(onSetupCompleted()).isEqualTo(Unit)
    }
}
