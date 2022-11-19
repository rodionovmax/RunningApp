package com.rodionovmax.runningapp.ui.run

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.databinding.FragmentRunBinding
import com.rodionovmax.runningapp.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RunFragment : Fragment() {

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}