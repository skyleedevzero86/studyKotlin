package com.kominioai.domain.survey.adapter.out.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class QuizParticipationMetrics(
    private val meterRegistry: MeterRegistry
) {

    private val participationCounter = Counter.builder("quiz.participation.total")
        .description("Total quiz participations")
        .register(meterRegistry)

    private val participationTimer = Timer.builder("quiz.participation.duration")
        .description("Quiz participation duration")
        .register(meterRegistry)

    private val answerSubmissionCounter = Counter.builder("quiz.answer.submission.total")
        .description("Total answer submissions")
        .register(meterRegistry)

    private val participationCompletionCounter = Counter.builder("quiz.participation.completion.total")
        .description("Total completed participations")
        .register(meterRegistry)

    private val timeExpiredCounter = Counter.builder("quiz.participation.time.expired.total")
        .description("Total time expired participations")
        .register(meterRegistry)

    fun recordParticipation() {
        participationCounter.increment()
    }

    fun recordParticipationTime(duration: Duration) {
        participationTimer.record(duration)
    }

    fun recordAnswerSubmission() {
        answerSubmissionCounter.increment()
    }

    fun recordParticipationCompletion() {
        participationCompletionCounter.increment()
    }

    fun recordTimeExpired() {
        timeExpiredCounter.increment()
    }
}