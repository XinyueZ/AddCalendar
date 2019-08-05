package io.add.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import io.add.calendar.databinding.MainFragmentBinding
import io.add.calendar.viewmodels.MainViewModel
import io.add.calendar.viewmodels.MainViewModelFactory

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels({ requireActivity() }) {
        MainViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: MainFragmentBinding =
            MainFragmentBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
            }
        binding.viewModel = viewModel
        subscribeUi()

        Log.d("+Calendar", "vm address: $viewModel")
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.onInferenceFinished.observe(viewLifecycleOwner) { evInferenceFinished ->
            if (evInferenceFinished()) {
                requireActivity().finish()
            }
        }
        viewModel.onPanic.observe(viewLifecycleOwner) { evPanic ->
            if (evPanic()) {
                Toast.makeText(
                    requireContext(),
                    R.string.add_to_calendar_toolbar_wrong_datetime_format,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
