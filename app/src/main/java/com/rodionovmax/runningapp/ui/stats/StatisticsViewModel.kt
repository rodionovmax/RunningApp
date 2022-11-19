package com.rodionovmax.runningapp.ui.stats

import androidx.lifecycle.ViewModel
import com.rodionovmax.runningapp.repo.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

}