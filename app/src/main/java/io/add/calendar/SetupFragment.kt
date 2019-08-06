package io.add.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.add.calendar.databinding.SetupFragmentBinding
import io.add.calendar.extensions.shareCompat
import io.add.calendar.utils.EventObserver
import io.add.calendar.viewmodels.SetupViewModel
import io.add.calendar.viewmodels.SetupViewModelFactory

class SetupFragment : Fragment() {

    private val viewModel: SetupViewModel by viewModels({ requireActivity() }) {
        SetupViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: SetupFragmentBinding =
            SetupFragmentBinding.inflate(inflater, container, false).apply {
                lifecycleOwner = viewLifecycleOwner
            }
        binding.viewModel = viewModel
        subscribeUi()

        Log.d("+Calendar", "vm address: $viewModel")
        return binding.root
    }

    private fun subscribeUi() {
        viewModel.echo.observe(viewLifecycleOwner, EventObserver { echoMsg ->
            Toast.makeText(requireContext(), echoMsg, Toast.LENGTH_LONG).show()
        })
        viewModel.onSetupCompleted.observe(viewLifecycleOwner, EventObserver {
            requireActivity().finish()
        })
        viewModel.onShareApp.observe(viewLifecycleOwner, EventObserver { shareText ->
            requireActivity().shareCompat(shareText)
        })
    }
}
