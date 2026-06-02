package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineSleepReport
import com.snoffee.app.domain.model.DailyCorrelation
import com.snoffee.app.domain.model.ImpactLevel
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

// Gemini 인사이트를 사용할 수 없을 때, 분석 결과(CaffeineSleepReport)를 바탕으로 규칙 기반 멘트를 직접 만든다.
// 우리 알고리즘이 뽑은 수치 + 판정(ImpactLevel)으로 문장을 만든다.
class GenerateFallbackInsightUseCase @Inject constructor() {

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)
    }

    fun execute(report: CaffeineSleepReport): String {
        // 분석 가능한 데이터가 부족한 경우
        if (!report.isSufficientData || report.dailyCorrelations.isEmpty()) {
            return "아직 분석할 카페인·수면 데이터가 충분하지 않아요. " +
                    "카페인 기록과 수면 데이터가 같은 날에 쌓이면 인사이트를 보여드릴게요!"
        }

        val worst = report.worstDay
            ?: return "데이터를 분석했지만 카페인이 수면에 준 영향을 찾기 어려웠어요."

        return buildMessageForDay(worst)
    }

    private fun buildMessageForDay(day: DailyCorrelation): String {
        val dateText = day.date.format(DATE_FORMAT)
        val residualMg = day.residualCaffeineAtSleep.roundToInt()
        val sleepHours = day.sleepDurationMinutes / 60
        val sleepMinutes = day.sleepDurationMinutes % 60

        return when (day.impactLevel) {
            ImpactLevel.HIGH -> buildString {
                append("${dateText}은 취침 시점에 카페인이 약 ${residualMg}mg이나 남아 있었어요. ")
                if (day.beforeSleepCaffeineCount > 0) {
                    append("취침 3시간 이내에 ${day.beforeSleepCaffeineCount}번이나 드셨네요. ")
                }
                append("그래서 깊은 잠을 자기 어려우셨을 거예요")
                append("다음엔 늦은 시간 카페인을 조금만 줄여보는 건 어떨까요?")
            }

            ImpactLevel.MODERATE -> buildString {
                append("${dateText}은 취침 시점에 카페인이 약 ${residualMg}mg 남아 있었어요. ")
                append("수면에 약간 영향을 줬을 수 있어요. ")
                append("총 ${sleepHours}시간 ${sleepMinutes}분 주무셨네요.")
            }

            ImpactLevel.LOW -> buildString {
                append("${dateText}은 카페인 관리를 잘 하셨어요! 👍 ")
                append("취침 시점 잔류량도 약 ${residualMg}mg으로 낮은 편이라 ")
                append("수면에 큰 영향은 없었을 거예요.")
            }
        }
    }
}