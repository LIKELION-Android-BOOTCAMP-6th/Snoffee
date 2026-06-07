package com.snoffee.wear.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.snoffee.wear.domain.usecase.CalculateResidualUseCase
import com.snoffee.wear.service.WearNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CaffeineCutoffWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val calculateResidualUseCase: CalculateResidualUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. 필요한 데이터 계산
        // val residual = calculateResidualUseCase(...)
        val residual = 60.0

        return if (residual > 50.0) {
            WearNotificationHelper.sendCaffeineCutoffNotification(applicationContext, residual)
            Result.success()
        } else {
            Result.success()
        }
    }
}