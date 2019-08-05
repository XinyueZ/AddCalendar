package io.add.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.add.calendar.databinding.MainActivityBinding
import io.add.calendar.utils.getScreenHeight
import io.add.calendar.utils.getScreenWidth
import io.add.calendar.viewmodels.MainViewModel
import io.add.calendar.viewmodels.MainViewModelFactory
import kotlin.math.roundToInt
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.activity_main).apply {
            lifecycleOwner = this@MainActivity
        }
        Log.d("+Calendar", "vm address: $viewModel")
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val text = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        text?.let { selectedText ->
            viewModel.selectedDatetimeText.set(selectedText.toString())
        }
        updateEditorSize()
    }

    private fun updateEditorSize() {
        add_calendar_main.layoutParams.run {
            width =
                (getScreenWidth() - resources.getDimension(R.dimen.main_view_margin_outside) * 2).roundToInt()
            height = getScreenHeight() / 2
        }
    }
}
