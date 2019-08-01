package io.add.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.add.calendar.databinding.MainActivityBinding2
import io.add.calendar.viewmodels.MainViewModel2
import io.add.calendar.viewmodels.MainViewModelFactory2
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity2 : AppCompatActivity() {

    private val viewModel: MainViewModel2 by viewModels {
        MainViewModelFactory2(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding2>(this, R.layout.activity_main2).apply {
            lifecycleOwner = this@MainActivity2
        }
        Log.d("+Calendar", "vm address: $viewModel")
        handleIntent(intent)
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

    private fun handleIntent(intent: Intent?) {
        val text = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        viewModel.selectedText = text?.toString() ?: ""

        updateEditorSize()
    }

    private fun updateEditorSize() {
        add_calendar_main.layoutParams.run {
            width = 400
            height = 50
        }
    }
}
