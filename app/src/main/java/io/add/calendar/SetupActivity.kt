package io.add.calendar

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.add.calendar.databinding.SetupActivityBinding
import io.add.calendar.viewmodels.SetupViewModel
import io.add.calendar.viewmodels.SetupViewModelFactory

class SetupActivity : AppCompatActivity() {

    private val viewModel: SetupViewModel by viewModels {
        SetupViewModelFactory(application)
    }

    override fun onBackPressed() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<SetupActivityBinding>(this, R.layout.activity_setup).apply {
            lifecycleOwner = this@SetupActivity
        }
        Log.d("+Calendar", "vm address: $viewModel")
    }
}
