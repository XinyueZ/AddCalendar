package io.add.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import io.add.calendar.databinding.SetupFragmentBinding
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
        viewModel.onSetupCompleted.observe(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }
}
