package com.snoffee.app.data.datasource.remote

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GeminiRemoteDataSource 구현체
 * Firebase AI Logic을 통해 Gemini API 호출
 */
@Singleton
class GeminiRemoteDataSourceImpl @Inject constructor() : GeminiRemoteDataSource {

    private val model by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel(
            modelName = "gemini-2.5-flash"
        )
    }

    override suspend fun generateWeeklyInsight(
        averageSleepTime: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {
        val prompt =
            HealthInsightPromptTemplate.buildWeeklyPrompt(
                averageSleepTime = averageSleepTime,
                lateCaffeineCount = lateCaffeineCount,
                bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
            )

        return generateGeminiText(prompt)
    }

    override suspend fun generateTrendInsight(
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepTime: String
    ): String {
        val prompt =
            HealthInsightPromptTemplate.buildTrendPrompt(
                monthlyCaffeineTrend = monthlyCaffeineTrend,
                monthlySleepTrend = monthlySleepTrend,
                totalAvgSleepTime = totalAvgSleepTime
            )

        return generateGeminiText(prompt)
    }

    override suspend fun generateMonthlyInsight(
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepTime: String,
        highCaffeineDaySleepTime: String,
        lowCaffeineDaySleepTime: String
    ): String {
        val prompt =
            HealthInsightPromptTemplate.buildMonthlyPrompt(
                monthlyAvgCaffeine = monthlyAvgCaffeine,
                monthlyAvgSleepTime = monthlyAvgSleepTime,
                highCaffeineDaySleepTime = highCaffeineDaySleepTime,
                lowCaffeineDaySleepTime = lowCaffeineDaySleepTime
            )

        return generateGeminiText(prompt)
    }

    override suspend fun generatePeriodInsight(
        periodTotalCaffeine: Int,
        periodAvgCaffeine: Int,
        periodTotalSleepTime: String,
        periodAvgSleepTime: String
    ): String {
        val prompt =
            HealthInsightPromptTemplate.buildPeriodPrompt(
                periodTotalCaffeine = periodTotalCaffeine,
                periodAvgCaffeine = periodAvgCaffeine,
                periodTotalSleepTime = periodTotalSleepTime,
                periodAvgSleepTime = periodAvgSleepTime
            )

        return generateGeminiText(prompt)
    }

    override suspend fun generateHomeInsight(
        recentDays: Int,
        beforeSleepHours: Int,
        lateCaffeineCount: Int,
        averageSleepTime: String,
        averageSleepScore: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {
        val prompt =
            HealthInsightPromptTemplate.buildHomePrompt(
                recentDays = recentDays,
                beforeSleepHours = beforeSleepHours,
                lateCaffeineCount = lateCaffeineCount,
                averageSleepTime = averageSleepTime,
                averageSleepScore = averageSleepScore,
                bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
            )

        return generateGeminiText(prompt)
    }

    private suspend fun generateGeminiText(
        prompt: String
    ): String {
        return runCatching {
            val response = model.generateContent(prompt)

            response.text
                ?.trim()
                ?.lineSequence()
                ?.filter { it.isNotBlank() }
                ?.take(3)
                ?.joinToString("\n")
                ?.takeIf { it.isNotBlank() }
                ?: HealthInsightPromptTemplate.fallbackMessage()
        }.getOrElse { throwable ->
            android.util.Log.e(
                "GeminiInsight",
                "Gemini 호출 실패: ${throwable.message}",
                throwable
            )
            HealthInsightPromptTemplate.fallbackMessage()
        }
    }
}