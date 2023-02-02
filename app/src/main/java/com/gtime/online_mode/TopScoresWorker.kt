package com.gtime.online_mode

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gtime.general.Cache
import com.gtime.general.model.UsageTimeRepository
import com.gtime.online_mode.data.TopScoresSource
import javax.inject.Inject

class TopScoresWorker @Inject constructor(
    context: Context,
    parameters: WorkerParameters,
    private val cache: Cache,
    private val topScoresSource: TopScoresSource,
    private val usageTimeRepository: UsageTimeRepository
) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (cache.isOnline()) {
            if (usageTimeRepository.isNotInit()) {
                usageTimeRepository.refreshAll()
                usageTimeRepository.fromDataBaseToRep()
            }
            usageTimeRepository.refreshUsageApps()
            if (topScoresSource.updateForCurrentUser(
                    usageTimeRepository.uiGeneralScores.value ?: 0
                )
            )
                return Result.success()
        }
        return Result.failure()
    }
}