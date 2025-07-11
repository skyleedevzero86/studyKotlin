package com.kominioai.domain.survey.benchmark

import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import org.openjdk.jmh.annotations.*
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class SurveyBenchmark {

    private lateinit var testData: List<CreateSurveyCommand>

    @Setup
    fun setup() {
        testData = (1..1000).map { i ->
            CreateSurveyCommand(
                title = "Benchmark Survey $i",
                description = "Benchmark test description",
                createdBy = UserId.from("benchmarkuser"),
                settings = SurveySettings()
            )
        }
    }

    @Benchmark
    fun benchmarkSurveyCreation(): Survey {
        val command = testData[0]
        return Survey.create(
            title = command.title,
            description = command.description,
            createdBy = command.createdBy,
            settings = command.settings
        )
    }
}