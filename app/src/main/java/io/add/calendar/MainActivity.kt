package io.add.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.add.calendar.databinding.MainActivityBinding
import io.add.calendar.utils.EventObserver
import io.add.calendar.utils.startSingleTopActivity
import io.add.calendar.viewmodels.MainViewModel
import io.add.calendar.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.activity_main).apply {
            lifecycleOwner = this@MainActivity
        }
        subscribeUi()
        Log.d("+Calendar", "vm address: $viewModel")
        handleIntent(intent)
    }

    private fun subscribeUi() {
        viewModel.navigateToAppSetup.observe(this, EventObserver {
            startSingleTopActivity<SetupActivity>()
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onPause() {
        viewModel.cancel()
        super.onPause()
    }

    override fun onDestroy() {
        clearFindViewByIdCache()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        val text = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        viewModel.selectedText = text?.toString() ?: ""

        updateEditorSize()
    }

    private fun updateEditorSize() {
        add_calendar_main.layoutParams.run {
            width = 600
            height = 50
        }
    }
}
