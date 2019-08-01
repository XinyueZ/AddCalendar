package io.add.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        Log.d("+Calendar", "vm address: $viewModel")
        return binding.root
    }
}
