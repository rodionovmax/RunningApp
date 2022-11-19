package com.rodionovmax.runningapp.repo

import com.rodionovmax.runningapp.db.RunDao
import com.rodionovmax.runningapp.db.RunEntity
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao
) {
    suspend fun insertRun(runEntity: RunEntity) = runDao.insertRun(runEntity)

    suspend fun deleteRun(runEntity: RunEntity) = runDao.deleteRun(runEntity)

    /* this function is not suspended because getAllRunsSortedByDate() returns LiveData
    * and LiveData is async by default */
    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCalorieBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
}