package com.rodionovmax.runningapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodionovmax.runningapp.db.RunEntity
import com.rodionovmax.runningapp.repo.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    fun insertRun(runEntity: RunEntity) = viewModelScope.launch {
        mainRepository.insertRun(runEntity)
    }
}