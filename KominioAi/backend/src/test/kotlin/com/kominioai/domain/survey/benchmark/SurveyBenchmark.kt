package com.kominioai.domain.survey.benchmark

import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.openjdk.jmh.annotations.*
import java.time.Instant
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
                createdBy = UserId("benchmarkuser")
            )
        }
    }

    @Benchmark
    fun benchmarkSurveyCreation(): Survey {
        val command = testData[0]
        return Survey(
            id = SurveyId.generate(),
            title = command.title,
            description = command.description,
            status = SurveyStatus.DRAFT,
            createdBy = command.createdBy,
            createdAt = Instant.now()
        )
    }
}