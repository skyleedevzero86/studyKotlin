package com.kochat.adapter.outbound.persistence.survey

import com.kochat.adapter.inbound.web.survey.dto.AssignRandomParticipantsRequest
import com.kochat.adapter.inbound.web.survey.dto.CreateSurveyRequest
import com.kochat.adapter.inbound.web.survey.dto.ParticipantAnswerDto
import com.kochat.adapter.inbound.web.survey.dto.ParticipantUploadResultDto
import com.kochat.adapter.inbound.web.survey.dto.ParticipantUploadRowResult
import com.kochat.adapter.inbound.web.survey.dto.SubmitSurveyResponseRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyAnswerItemRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyDetailDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyOptionDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyOptionRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyParticipantDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyParticipantStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyQuestionDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyQuestionRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyQuestionStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyRoomStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyRoomStatisticsListResponse
import com.kochat.adapter.inbound.web.survey.dto.SurveyStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveySummaryDto
import com.kochat.adapter.inbound.web.survey.dto.UpdateSurveyRequest
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaEntity
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaEntity
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.survey.model.ParticipantStatus
import com.kochat.domain.survey.model.QuestionType
import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
import com.kochat.domain.survey.service.SurveyService
import com.kochat.domain.user.model.UserRole
import com.kochat.domain.user.model.UserStatus
import com.kochat.global.application.admin.SurveyExportService
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
@Transactional
class SurveyServiceImpl(
    private val surveyJpaRepository: SurveyJpaRepository,
    private val surveyQuestionJpaRepository: SurveyQuestionJpaRepository,
    private val surveyOptionJpaRepository: SurveyOptionJpaRepository,
    private val surveyParticipantJpaRepository: SurveyParticipantJpaRepository,
    private val surveyAnswerJpaRepository: SurveyAnswerJpaRepository,
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val userJpaRepository: UserJpaRepository,
    private val surveyExportService: SurveyExportService,
) : SurveyService {

    override fun listRoomSurveys(
        roomId: Long,
        userId: Long,
        includeAll: Boolean,
        pageable: Pageable,
    ): Page<SurveySummaryDto> {
        val room = requireRoom(roomId)
        requireMemberOrAdmin(roomId, userId)
        val surveys = if (includeAll && (isRoomOwner(room, userId) || isAdminUser(userId))) {
            surveyJpaRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable)
        } else {
            surveyJpaRepository.findByChatRoomIdAndStatusOrderByCreatedAtDesc(
                roomId,
                SurveyStatus.ACTIVE,
                pageable,
            )
        }
        return surveys.map { toSummary(it, userId) }
    }

    override fun getSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto {
        val survey = requireSurvey(roomId, surveyId)
        requireMemberOrAdmin(roomId, userId)
        return toDetail(survey, userId)
    }

    override fun getSurveyById(surveyId: Long, userId: Long): SurveyDetailDto {
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        val isParticipant = surveyParticipantJpaRepository.existsBySurveyIdAndUserId(surveyId, userId)
        val isAdmin = isAdminUser(userId)
        require(isParticipant || isAdmin) { "해당 설문에 접근 권한이 없습니다." }
        return toDetail(survey, userId)
    }

    override fun createSurvey(roomId: Long, userId: Long, request: CreateSurveyRequest): SurveyDetailDto {
        val room = requireRoom(roomId)
        requireRoomOwner(room, userId)
        validateSurveyRequest(request)
        val creator = requireUser(userId)
        val survey = SurveyJpaEntity().apply {
            chatRoom = room
            title = request.title.trim()
            description = request.description?.trim()
            status = SurveyStatus.DRAFT
            targetMode = request.targetMode
            randomTargetCount = request.randomTargetCount
            startAt = request.startAt
            endAt = request.endAt
            createdBy = creator
        }
        val saved = surveyJpaRepository.save(survey)
        saveQuestions(saved, request.questions)
        assignParticipants(saved, request.targetUserIds)
        return toDetail(saved, userId)
    }

    override fun updateSurvey(
        roomId: Long,
        surveyId: Long,
        userId: Long,
        request: UpdateSurveyRequest,
    ): SurveyDetailDto {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        require(survey.status == SurveyStatus.DRAFT) { "진행 전 설문만 수정할 수 있습니다." }
        require(!surveyAnswerJpaRepository.existsBySurveyId(surveyId)) { "응답이 있는 설문은 수정할 수 없습니다." }
        validateSurveyRequest(request)
        survey.title = request.title.trim()
        survey.description = request.description?.trim()
        survey.targetMode = request.targetMode
        survey.randomTargetCount = request.randomTargetCount
        survey.startAt = request.startAt
        survey.endAt = request.endAt
        clearQuestions(surveyId)
        saveQuestions(survey, request.questions)
        surveyParticipantJpaRepository.deleteBySurveyId(surveyId)
        assignParticipants(survey, request.targetUserIds)
        return toDetail(survey, userId)
    }

    override fun publishSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        require(survey.status == SurveyStatus.DRAFT) { "이미 게시된 설문입니다." }
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        require(questions.isNotEmpty()) { "문항이 없는 설문은 게시할 수 없습니다." }
        if (survey.targetMode == TargetMode.RANDOM) {
            val count = survey.randomTargetCount ?: 0
            require(count > 0) { "랜덤 대상자 수를 지정해 주세요." }
            if (surveyParticipantJpaRepository.countBySurveyId(surveyId) == 0L) {
                assignRandomParticipants(survey, count)
            }
        } else if (survey.targetMode == TargetMode.ALL_MEMBERS) {
            assignAllMembers(survey)
        }
        survey.status = SurveyStatus.ACTIVE
        if (survey.startAt == null) {
            survey.startAt = LocalDateTime.now()
        }
        return toDetail(survey, userId)
    }

    override fun closeSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        require(survey.status == SurveyStatus.ACTIVE) { "진행 중인 설문만 종료할 수 있습니다." }
        survey.status = SurveyStatus.CLOSED
        if (survey.endAt == null) {
            survey.endAt = LocalDateTime.now()
        }
        return toDetail(survey, userId)
    }

    override fun deleteSurvey(roomId: Long, surveyId: Long, userId: Long) {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        require(!surveyAnswerJpaRepository.existsBySurveyId(surveyId)) { "응답이 있는 설문은 삭제할 수 없습니다." }
        clearSurveyData(surveyId)
        surveyJpaRepository.delete(survey)
    }

    override fun submitResponse(
        roomId: Long,
        surveyId: Long,
        userId: Long,
        request: SubmitSurveyResponseRequest,
    ): SurveyDetailDto {
        val survey = requireSurvey(roomId, surveyId)
        require(survey.status == SurveyStatus.ACTIVE) { "진행 중인 설문만 응답할 수 있습니다." }
        require(isWithinSchedule(survey)) { "설문 응답 기간이 아닙니다." }
        requireMember(roomId, userId)
        require(canRespond(survey, userId)) { "설문 대상자가 아닙니다." }
        val existing = surveyAnswerJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
        require(existing.isEmpty()) { "이미 응답한 설문입니다." }
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        val questionMap = questions.associateBy { it.id!! }
        val user = requireUser(userId)
        request.answers.forEach { answer ->
            val question = questionMap[answer.questionId]
                ?: throw IllegalArgumentException("유효하지 않은 문항입니다.")
            validateAnswer(question, answer)
            when (question.questionType) {
                QuestionType.TEXT -> {
                    surveyAnswerJpaRepository.save(
                        SurveyAnswerJpaEntity().apply {
                            this.survey = survey
                            this.question = question
                            this.user = user
                            textAnswer = answer.textAnswer?.trim()
                        },
                    )
                }
                QuestionType.SINGLE_CHOICE -> {
                    val optionId = answer.optionIds.single()
                    val option = surveyOptionJpaRepository.findById(optionId)
                        .orElseThrow { IllegalArgumentException("유효하지 않은 보기입니다.") }
                    surveyAnswerJpaRepository.save(
                        SurveyAnswerJpaEntity().apply {
                            this.survey = survey
                            this.question = question
                            this.option = option
                            this.user = user
                        },
                    )
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    answer.optionIds.forEach { optionId ->
                        val option = surveyOptionJpaRepository.findById(optionId)
                            .orElseThrow { IllegalArgumentException("유효하지 않은 보기입니다.") }
                        surveyAnswerJpaRepository.save(
                            SurveyAnswerJpaEntity().apply {
                                this.survey = survey
                                this.question = question
                                this.option = option
                                this.user = user
                            },
                        )
                    }
                }
            }
        }
        val participant = surveyParticipantJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
            ?: if (survey.targetMode == TargetMode.ALL_MEMBERS) {
                surveyParticipantJpaRepository.save(
                    SurveyParticipantJpaEntity().apply {
                        this.survey = survey
                        this.user = user
                    },
                )
            } else {
                null
            }
        if (participant != null) {
            participant.status = ParticipantStatus.COMPLETED
            participant.completedAt = LocalDateTime.now()
        }
        return toDetail(survey, userId)
    }

    @Transactional
    override fun submitResponseById(
        surveyId: Long,
        userId: Long,
        request: SubmitSurveyResponseRequest,
    ): SurveyDetailDto {
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        require(survey.status == SurveyStatus.ACTIVE) { "진행 중인 설문만 응답할 수 있습니다." }
        require(isWithinSchedule(survey)) { "설문 응답 기간이 아닙니다." }
        require(canRespond(survey, userId)) { "설문 대상자가 아닙니다." }
        val existing = surveyAnswerJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
        require(existing.isEmpty()) { "이미 응답한 설문입니다." }
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        val questionMap = questions.associateBy { it.id!! }
        val user = requireUser(userId)
        request.answers.forEach { answer ->
            val question = questionMap[answer.questionId]
                ?: throw IllegalArgumentException("유효하지 않은 문항입니다.")
            validateAnswer(question, answer)
            when (question.questionType) {
                QuestionType.TEXT -> {
                    surveyAnswerJpaRepository.save(
                        SurveyAnswerJpaEntity().apply {
                            this.survey = survey
                            this.question = question
                            this.user = user
                            textAnswer = answer.textAnswer?.trim()
                        },
                    )
                }
                QuestionType.SINGLE_CHOICE -> {
                    val optionId = answer.optionIds.single()
                    val option = surveyOptionJpaRepository.findById(optionId)
                        .orElseThrow { IllegalArgumentException("유효하지 않은 보기입니다.") }
                    surveyAnswerJpaRepository.save(
                        SurveyAnswerJpaEntity().apply {
                            this.survey = survey
                            this.question = question
                            this.option = option
                            this.user = user
                        },
                    )
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    answer.optionIds.forEach { optionId ->
                        val option = surveyOptionJpaRepository.findById(optionId)
                            .orElseThrow { IllegalArgumentException("유효하지 않은 보기입니다.") }
                        surveyAnswerJpaRepository.save(
                            SurveyAnswerJpaEntity().apply {
                                this.survey = survey
                                this.question = question
                                this.option = option
                                this.user = user
                            },
                        )
                    }
                }
            }
        }
        val participant = surveyParticipantJpaRepository.findBySurveyIdAndUserId(surveyId, userId)
        if (participant != null) {
            participant.status = ParticipantStatus.COMPLETED
            participant.completedAt = LocalDateTime.now()
        }
        return toDetail(survey, userId)
    }

    override fun getStatistics(roomId: Long, surveyId: Long, userId: Long): SurveyStatisticsDto {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        return buildStatistics(survey)
    }

    override fun exportStatistics(roomId: Long, surveyId: Long, userId: Long, format: String): ByteArray {
        val statistics = getStatistics(roomId, surveyId, userId)
        return exportStatistics(statistics, format)
    }

    override fun uploadParticipants(
        roomId: Long,
        surveyId: Long,
        userId: Long,
        file: MultipartFile,
    ): ParticipantUploadResultDto {
        val survey = requireSurvey(roomId, surveyId)
        requireRoomOwner(survey.chatRoom!!, userId)
        require(survey.status == SurveyStatus.DRAFT || survey.status == SurveyStatus.ACTIVE) {
            "종료된 설문에는 대상자를 추가할 수 없습니다."
        }
        survey.targetMode = TargetMode.SELECTED
        return parseAndAssignParticipants(survey, file)
    }

    override fun listAdminSurveys(
        status: SurveyStatus?,
        chatRoomId: Long?,
        targetMode: TargetMode?,
        title: String?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<SurveySummaryDto> =
        surveyJpaRepository.findForAdmin(status, chatRoomId, targetMode, title?.trim(), from, to, pageable)
            .map { toSummary(it, null) }

    override fun adminCreateSurvey(
        roomId: Long,
        adminUserId: Long,
        request: CreateSurveyRequest,
    ): SurveyDetailDto {
        requireAdmin(adminUserId)
        val room = requireRoom(roomId)
        validateSurveyRequest(request)
        val creator = requireUser(adminUserId)
        val survey = SurveyJpaEntity().apply {
            chatRoom = room
            title = request.title.trim()
            description = request.description?.trim()
            status = SurveyStatus.DRAFT
            targetMode = request.targetMode
            randomTargetCount = request.randomTargetCount
            startAt = request.startAt
            endAt = request.endAt
            createdBy = creator
        }
        val saved = surveyJpaRepository.save(survey)
        saveQuestions(saved, request.questions)
        if (request.targetMode == TargetMode.SELECTED) {
            assignParticipants(saved, request.targetUserIds)
        }
        return toDetail(saved, adminUserId)
    }

    override fun adminCreateSurveyWithoutRoom(
        adminUserId: Long,
        request: CreateSurveyRequest,
    ): SurveyDetailDto {
        requireAdmin(adminUserId)
        validateSurveyRequest(request)
        val creator = requireUser(adminUserId)
        val survey = SurveyJpaEntity().apply {
            chatRoom = null
            title = request.title.trim()
            description = request.description?.trim()
            status = SurveyStatus.DRAFT
            targetMode = request.targetMode
            randomTargetCount = request.randomTargetCount
            startAt = request.startAt
            endAt = request.endAt
            createdBy = creator
        }
        val saved = surveyJpaRepository.save(survey)
        saveQuestions(saved, request.questions)
        if (request.targetMode == TargetMode.SELECTED && request.targetUserIds.isNotEmpty()) {
            assignParticipantsDirect(saved, request.targetUserIds)
        }
        return toDetail(saved, adminUserId)
    }

    override fun adminPublishSurvey(surveyId: Long, adminUserId: Long): SurveyDetailDto {
        requireAdmin(adminUserId)
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        val roomId = survey.chatRoom?.id
        return if (roomId != null) {
            publishSurvey(roomId, surveyId, adminUserId)
        } else {
            require(survey.status == SurveyStatus.DRAFT) { "작성중 상태의 설문만 게시할 수 있습니다." }

            if (survey.targetMode == TargetMode.ALL_MEMBERS &&
                surveyParticipantJpaRepository.countBySurveyId(surveyId) == 0L
            ) {
                val allActiveUsers = userJpaRepository.findByStatusOrderByUsernameAsc(UserStatus.ACTIVE)
                allActiveUsers.forEach { user ->
                    val uid = user.id ?: return@forEach
                    if (!surveyParticipantJpaRepository.existsBySurveyIdAndUserId(surveyId, uid)) {
                        surveyParticipantJpaRepository.save(
                            SurveyParticipantJpaEntity().apply {
                                this.survey = survey
                                this.user = user
                            },
                        )
                    }
                }
            }

            survey.status = SurveyStatus.ACTIVE
            surveyJpaRepository.save(survey)
            toDetail(survey, adminUserId)
        }
    }

    override fun adminCloseSurvey(surveyId: Long, adminUserId: Long): SurveyDetailDto {
        requireAdmin(adminUserId)
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        val roomId = survey.chatRoom?.id
        return if (roomId != null) {
            closeSurvey(roomId, surveyId, adminUserId)
        } else {
            require(survey.status == SurveyStatus.ACTIVE) { "진행중인 설문만 종료할 수 있습니다." }
            survey.status = SurveyStatus.CLOSED
            surveyJpaRepository.save(survey)
            toDetail(survey, adminUserId)
        }
    }

    override fun adminAssignRandomParticipants(
        surveyId: Long,
        adminUserId: Long,
        request: AssignRandomParticipantsRequest,
    ): SurveyDetailDto {
        requireAdmin(adminUserId)
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        require(request.count > 0) { "랜덤 대상자 수는 1 이상이어야 합니다." }
        survey.targetMode = TargetMode.RANDOM
        survey.randomTargetCount = request.count
        assignRandomParticipants(survey, request.count)
        return toDetail(survey, adminUserId)
    }

    override fun adminUploadParticipants(
        surveyId: Long,
        adminUserId: Long,
        file: MultipartFile,
    ): ParticipantUploadResultDto {
        requireAdmin(adminUserId)
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        require(survey.status == SurveyStatus.DRAFT || survey.status == SurveyStatus.ACTIVE) {
            "종료된 설문에는 대상자를 추가할 수 없습니다."
        }
        survey.targetMode = TargetMode.SELECTED
        return parseAndAssignParticipants(survey, file)
    }

    override fun adminGetStatistics(surveyId: Long, adminUserId: Long): SurveyStatisticsDto {
        requireAdmin(adminUserId)
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        return buildStatistics(survey)
    }

    override fun adminExportStatistics(surveyId: Long, adminUserId: Long, format: String): ByteArray {
        val statistics = adminGetStatistics(surveyId, adminUserId)
        return exportStatistics(statistics, format)
    }

    override fun adminRoomStatistics(surveyId: Long?): SurveyRoomStatisticsListResponse {
        val surveys = if (surveyId != null) {
            listOf(
                surveyJpaRepository.findById(surveyId)
                    .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") },
            )
        } else {
            surveyJpaRepository.findAll()
        }
        val roomIds = surveys.mapNotNull { it.chatRoom?.id }.distinct()
        val rows = roomIds.map { roomId ->
            val room = chatRoomJpaRepository.findById(roomId).orElseThrow()
            val roomSurveys = surveys.filter { it.chatRoom?.id == roomId }
            val surveyIds = roomSurveys.mapNotNull { it.id }
            val completedCount = surveyIds.sumOf { id ->
                surveyParticipantJpaRepository.countBySurveyIdAndStatus(id, ParticipantStatus.COMPLETED)
            }
            SurveyRoomStatisticsDto(
                chatRoomId = roomId,
                chatRoomName = room.name,
                surveyCount = roomSurveys.size.toLong(),
                respondentCount = completedCount,
                completedCount = completedCount,
            )
        }
        return SurveyRoomStatisticsListResponse(rows)
    }

    private fun requireRoom(roomId: Long): ChatRoomJpaEntity =
        chatRoomJpaRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("채팅방을 찾을 수 없습니다.") }

    private fun requireSurvey(roomId: Long, surveyId: Long): SurveyJpaEntity {
        val survey = surveyJpaRepository.findById(surveyId)
            .orElseThrow { IllegalArgumentException("설문을 찾을 수 없습니다.") }
        require(survey.chatRoom?.id == roomId) { "해당 채팅방의 설문이 아닙니다." }
        return survey
    }

    private fun requireUser(userId: Long): UserJpaEntity =
        userJpaRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

    private fun requireMember(roomId: Long, userId: Long) {
        require(chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            "채팅방 멤버만 참여할 수 있습니다."
        }
    }

    private fun requireMemberOrAdmin(roomId: Long, userId: Long) {
        if (!isAdminUser(userId)) {
            requireMember(roomId, userId)
        }
    }

    private fun requireRoomOwner(room: ChatRoomJpaEntity, userId: Long) {
        require(isRoomOwner(room, userId) || isAdminUser(userId)) {
            "채팅방 개설자 또는 관리자만 설문을 관리할 수 있습니다."
        }
    }

    private fun requireAdmin(userId: Long) {
        require(isAdminUser(userId)) { "관리자만 사용할 수 있습니다." }
    }

    private fun isRoomOwner(room: ChatRoomJpaEntity, userId: Long): Boolean =
        room.createdBy?.id == userId

    private fun isAdminUser(userId: Long): Boolean =
        userJpaRepository.findById(userId).orElse(null)?.role == UserRole.ADMIN

    private fun validateSurveyRequest(request: CreateSurveyRequest) {
        require(request.questions.isNotEmpty()) { "문항을 1개 이상 등록해 주세요." }
        request.questions.forEach { validateQuestion(it) }
        validateSchedule(request.startAt, request.endAt)
        if (request.targetMode == TargetMode.RANDOM) {
            require((request.randomTargetCount ?: 0) > 0) { "랜덤 대상자 수를 지정해 주세요." }
        }
        if (request.targetMode == TargetMode.SELECTED) {
            require(request.targetUserIds.isNotEmpty()) { "대상자를 선택해 주세요." }
        }
    }

    private fun validateSurveyRequest(request: UpdateSurveyRequest) {
        require(request.questions.isNotEmpty()) { "문항을 1개 이상 등록해 주세요." }
        request.questions.forEach { validateQuestion(it) }
        validateSchedule(request.startAt, request.endAt)
        if (request.targetMode == TargetMode.RANDOM) {
            require((request.randomTargetCount ?: 0) > 0) { "랜덤 대상자 수를 지정해 주세요." }
        }
        if (request.targetMode == TargetMode.SELECTED) {
            require(request.targetUserIds.isNotEmpty()) { "대상자를 선택해 주세요." }
        }
    }

    private fun validateQuestion(question: SurveyQuestionRequest) {
        require(question.questionText.isNotBlank()) { "문항 내용을 입력해 주세요." }
        when (question.questionType) {
            QuestionType.TEXT -> Unit
            QuestionType.SINGLE_CHOICE, QuestionType.MULTIPLE_CHOICE -> {
                require(question.options.size >= 2) { "객관식 문항은 보기를 2개 이상 등록해 주세요." }
            }
        }
    }

    private fun validateSchedule(startAt: LocalDateTime?, endAt: LocalDateTime?) {
        if (startAt != null && endAt != null) {
            require(!endAt.isBefore(startAt)) { "종료일은 시작일보다 빠를 수 없습니다." }
        }
    }

    private fun validateAnswer(question: SurveyQuestionJpaEntity, answer: SurveyAnswerItemRequest) {
        when (question.questionType) {
            QuestionType.TEXT -> {
                require(!answer.textAnswer.isNullOrBlank()) { "주관식 답변을 입력해 주세요." }
            }
            QuestionType.SINGLE_CHOICE -> {
                require(answer.optionIds.size == 1) { "단일 선택 문항은 하나만 선택해 주세요." }
            }
            QuestionType.MULTIPLE_CHOICE -> {
                require(answer.optionIds.isNotEmpty()) { "복수 선택 문항은 하나 이상 선택해 주세요." }
            }
        }
    }

    private fun saveQuestions(survey: SurveyJpaEntity, questions: List<SurveyQuestionRequest>) {
        questions.forEachIndexed { index, questionRequest ->
            val question = SurveyQuestionJpaEntity().apply {
                this.survey = survey
                questionNo = index + 1
                questionType = questionRequest.questionType
                questionText = questionRequest.questionText.trim()
            }
            val savedQuestion = surveyQuestionJpaRepository.save(question)
            questionRequest.options.forEachIndexed { optionIndex, optionRequest ->
                surveyOptionJpaRepository.save(
                    SurveyOptionJpaEntity().apply {
                        this.question = savedQuestion
                        optionNo = optionIndex + 1
                        optionText = optionRequest.optionText.trim()
                    },
                )
            }
        }
    }

    private fun clearQuestions(surveyId: Long) {
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        questions.forEach { question ->
            surveyOptionJpaRepository.deleteByQuestionId(question.id!!)
        }
        surveyQuestionJpaRepository.deleteBySurveyId(surveyId)
    }

    private fun clearSurveyData(surveyId: Long) {
        surveyAnswerJpaRepository.deleteBySurveyId(surveyId)
        surveyParticipantJpaRepository.deleteBySurveyId(surveyId)
        clearQuestions(surveyId)
    }

    private fun assignParticipants(survey: SurveyJpaEntity, targetUserIds: List<Long>) {
        if (survey.targetMode != TargetMode.SELECTED) return
        val roomId = survey.chatRoom?.id ?: return
        targetUserIds.distinct().forEach { targetUserId ->
            requireMember(roomId, targetUserId)
            if (!surveyParticipantJpaRepository.existsBySurveyIdAndUserId(survey.id!!, targetUserId)) {
                surveyParticipantJpaRepository.save(
                    SurveyParticipantJpaEntity().apply {
                        this.survey = survey
                        user = requireUser(targetUserId)
                    },
                )
            }
        }
    }

    private fun assignParticipantsDirect(survey: SurveyJpaEntity, targetUserIds: List<Long>) {
        targetUserIds.distinct().forEach { targetUserId ->
            if (!surveyParticipantJpaRepository.existsBySurveyIdAndUserId(survey.id!!, targetUserId)) {
                surveyParticipantJpaRepository.save(
                    SurveyParticipantJpaEntity().apply {
                        this.survey = survey
                        user = requireUser(targetUserId)
                    },
                )
            }
        }
    }

    private fun assignAllMembers(survey: SurveyJpaEntity) {
        val roomId = survey.chatRoom?.id ?: return
        val members = chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId)
        members.forEach { member ->
            val userId = member.user?.id ?: return@forEach
            if (!surveyParticipantJpaRepository.existsBySurveyIdAndUserId(survey.id!!, userId)) {
                surveyParticipantJpaRepository.save(
                    SurveyParticipantJpaEntity().apply {
                        this.survey = survey
                        user = member.user
                    },
                )
            }
        }
    }

    private fun assignRandomParticipants(survey: SurveyJpaEntity, count: Int) {
        val roomId = survey.chatRoom?.id ?: return
        val members = chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId)
        val existingIds = surveyParticipantJpaRepository.findUserIdsBySurveyId(survey.id!!).toSet()
        val candidates = members
            .mapNotNull { it.user }
            .filter { it.id !in existingIds }
            .shuffled()
            .take(count)
        require(candidates.isNotEmpty()) { "랜덤 배정할 대상자가 없습니다." }
        candidates.forEach { user ->
            surveyParticipantJpaRepository.save(
                SurveyParticipantJpaEntity().apply {
                    this.survey = survey
                    this.user = user
                },
            )
        }
    }

    private fun canRespond(survey: SurveyJpaEntity, userId: Long): Boolean =
        when (survey.targetMode) {
            TargetMode.ALL_MEMBERS -> {
                val roomId = survey.chatRoom?.id ?: return true
                chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
            }
            TargetMode.SELECTED, TargetMode.RANDOM -> {
                surveyParticipantJpaRepository.existsBySurveyIdAndUserId(survey.id!!, userId)
            }
        }

    private fun toSummary(survey: SurveyJpaEntity, viewerUserId: Long?): SurveySummaryDto {
        val surveyId = survey.id!!
        val questionCount = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId).size
        val participantCount = surveyParticipantJpaRepository.countBySurveyId(surveyId)
        val completedCount = surveyParticipantJpaRepository.countBySurveyIdAndStatus(
            surveyId,
            ParticipantStatus.COMPLETED,
        )
        val hasResponded = viewerUserId?.let {
            surveyAnswerJpaRepository.findBySurveyIdAndUserId(surveyId, it).isNotEmpty()
        } ?: false
        val canRespondNow = viewerUserId?.let {
            survey.status == SurveyStatus.ACTIVE &&
                isWithinSchedule(survey) &&
                canRespond(survey, it) &&
                !hasResponded
        } ?: false
        return SurveySummaryDto(
            id = surveyId,
            chatRoomId = survey.chatRoom?.id,
            chatRoomName = survey.chatRoom?.name ?: "",
            title = survey.title,
            description = survey.description,
            status = survey.status,
            targetMode = survey.targetMode,
            randomTargetCount = survey.randomTargetCount,
            startAt = survey.startAt,
            endAt = survey.endAt,
            questionCount = questionCount,
            participantCount = participantCount,
            completedCount = completedCount,
            createdByUserId = survey.createdBy?.id!!,
            createdByUsername = survey.createdBy?.username ?: "",
            createdAt = survey.createdAt,
            canRespond = canRespondNow,
            hasResponded = hasResponded,
        )
    }

    private fun toDetail(survey: SurveyJpaEntity, viewerUserId: Long): SurveyDetailDto {
        val surveyId = survey.id!!
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        val optionCounts = surveyAnswerJpaRepository.countByOptionForSurvey(surveyId)
            .associate { (it[0] as Long) to (it[1] as Long) }
        val questionDtos = questions.map { question ->
            val options = surveyOptionJpaRepository.findByQuestionIdOrderByOptionNoAsc(question.id!!)
                .map { option ->
                    SurveyOptionDto(
                        id = option.id!!,
                        optionNo = option.optionNo,
                        optionText = option.optionText,
                        selectCount = optionCounts[option.id] ?: 0L,
                    )
                }
            SurveyQuestionDto(
                id = question.id!!,
                questionNo = question.questionNo,
                questionType = question.questionType,
                questionText = question.questionText,
                options = options,
            )
        }
        val participants = surveyParticipantJpaRepository.findBySurveyIdOrderByAssignedAtAsc(surveyId)
            .map { participant ->
                SurveyParticipantDto(
                    userId = participant.user?.id!!,
                    username = participant.user?.username ?: "",
                    displayName = participant.user?.displayName,
                    status = participant.status,
                    assignedAt = participant.assignedAt,
                    completedAt = participant.completedAt,
                )
            }
        val hasResponded = surveyAnswerJpaRepository.findBySurveyIdAndUserId(surveyId, viewerUserId).isNotEmpty()
        return SurveyDetailDto(
            id = surveyId,
            chatRoomId = survey.chatRoom?.id,
            chatRoomName = survey.chatRoom?.name ?: "",
            title = survey.title,
            description = survey.description,
            status = survey.status,
            targetMode = survey.targetMode,
            randomTargetCount = survey.randomTargetCount,
            startAt = survey.startAt,
            endAt = survey.endAt,
            questions = questionDtos,
            participants = participants,
            createdByUserId = survey.createdBy?.id!!,
            createdAt = survey.createdAt,
            canRespond = survey.status == SurveyStatus.ACTIVE &&
                isWithinSchedule(survey) &&
                canRespond(survey, viewerUserId) &&
                !hasResponded,
            hasResponded = hasResponded,
        )
    }

    private fun buildStatistics(survey: SurveyJpaEntity): SurveyStatisticsDto {
        val surveyId = survey.id!!
        val questions = surveyQuestionJpaRepository.findBySurveyIdOrderByQuestionNoAsc(surveyId)
        val optionCounts = surveyAnswerJpaRepository.countByOptionForSurvey(surveyId)
            .associate { (it[0] as Long) to (it[1] as Long) }
        val respondentByQuestion = surveyAnswerJpaRepository.countRespondentsByQuestion(surveyId)
            .associate { (it[0] as Long) to (it[1] as Long) }
        val byQuestion = questions.map { question ->
            val options = surveyOptionJpaRepository.findByQuestionIdOrderByOptionNoAsc(question.id!!)
                .map { option ->
                    SurveyOptionDto(
                        id = option.id!!,
                        optionNo = option.optionNo,
                        optionText = option.optionText,
                        selectCount = optionCounts[option.id] ?: 0L,
                    )
                }
            val textAnswers = if (question.questionType == QuestionType.TEXT) {
                surveyAnswerJpaRepository.findBySurveyId(surveyId)
                    .filter { it.question?.id == question.id }
                    .mapNotNull { it.textAnswer }
            } else {
                emptyList()
            }
            SurveyQuestionStatisticsDto(
                questionId = question.id!!,
                questionNo = question.questionNo,
                questionText = question.questionText,
                questionType = question.questionType,
                respondentCount = respondentByQuestion[question.id] ?: 0L,
                options = options,
                textAnswers = textAnswers,
            )
        }
        val participants = surveyParticipantJpaRepository.findBySurveyIdOrderByAssignedAtAsc(surveyId)
        val allAnswers = surveyAnswerJpaRepository.findBySurveyId(surveyId)
        val byParticipant = participants.map { participant ->
            val userId = participant.user?.id!!
            val userAnswers = allAnswers.filter { it.user?.id == userId }
            val answers = questions.map { question ->
                val questionAnswers = userAnswers.filter { it.question?.id == question.id }
                ParticipantAnswerDto(
                    questionId = question.id!!,
                    questionText = question.questionText,
                    optionTexts = questionAnswers.mapNotNull { it.option?.optionText },
                    textAnswer = questionAnswers.firstOrNull()?.textAnswer,
                )
            }
            SurveyParticipantStatisticsDto(
                userId = userId,
                username = participant.user?.username ?: "",
                displayName = participant.user?.displayName,
                status = participant.status,
                answers = answers,
            )
        }
        val totalParticipants = surveyParticipantJpaRepository.countBySurveyId(surveyId)
        val completedParticipants = surveyParticipantJpaRepository.countBySurveyIdAndStatus(
            surveyId,
            ParticipantStatus.COMPLETED,
        )
        return SurveyStatisticsDto(
            surveyId = surveyId,
            title = survey.title,
            totalParticipants = totalParticipants,
            completedParticipants = completedParticipants,
            byQuestion = byQuestion,
            byParticipant = byParticipant,
        )
    }

    private fun isWithinSchedule(survey: SurveyJpaEntity): Boolean {
        val now = LocalDateTime.now()
        val afterStart = survey.startAt == null || !now.isBefore(survey.startAt)
        val beforeEnd = survey.endAt == null || !now.isAfter(survey.endAt)
        return afterStart && beforeEnd
    }

    private fun exportStatistics(statistics: SurveyStatisticsDto, format: String): ByteArray =
        when (format.lowercase()) {
            "pdf" -> surveyExportService.exportPdf(statistics)
            else -> surveyExportService.exportExcel(statistics)
        }

    private fun parseAndAssignParticipants(
        survey: SurveyJpaEntity,
        file: MultipartFile,
    ): ParticipantUploadResultDto {
        require(!file.isEmpty) { "업로드할 파일을 선택해 주세요." }
        val roomId = survey.chatRoom?.id ?: throw IllegalArgumentException("설문 채팅방이 없습니다.")
        val surveyId = survey.id!!
        val formatter = DataFormatter()
        val rows = mutableListOf<ParticipantUploadRowResult>()
        var successCount = 0

        XSSFWorkbook(file.inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            sheet.forEachIndexed { index, row ->
                if (index == 0) {
                    val firstCell = formatter.formatCellValue(row.getCell(0)).trim()
                    if (firstCell.equals("username", true) ||
                        firstCell.equals("아이디", true) ||
                        firstCell.equals("userid", true)
                    ) {
                        return@forEachIndexed
                    }
                }
                val identifier = formatter.formatCellValue(row.getCell(0)).trim()
                if (identifier.isBlank()) return@forEachIndexed

                val user = userJpaRepository.findByUsername(identifier)
                    ?: identifier.toLongOrNull()?.let { userJpaRepository.findById(it).orElse(null) }
                if (user == null) {
                    rows.add(
                        ParticipantUploadRowResult(
                            row = index + 1,
                            identifier = identifier,
                            success = false,
                            message = "사용자를 찾을 수 없습니다.",
                        ),
                    )
                    return@forEachIndexed
                }
                if (!chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, user.id!!)) {
                    rows.add(
                        ParticipantUploadRowResult(
                            row = index + 1,
                            identifier = identifier,
                            success = false,
                            message = "채팅방 멤버가 아닙니다.",
                        ),
                    )
                    return@forEachIndexed
                }
                if (surveyParticipantJpaRepository.existsBySurveyIdAndUserId(surveyId, user.id!!)) {
                    rows.add(
                        ParticipantUploadRowResult(
                            row = index + 1,
                            identifier = identifier,
                            success = false,
                            message = "이미 배정된 대상자입니다.",
                        ),
                    )
                    return@forEachIndexed
                }
                surveyParticipantJpaRepository.save(
                    SurveyParticipantJpaEntity().apply {
                        this.survey = survey
                        this.user = user
                    },
                )
                successCount += 1
                rows.add(
                    ParticipantUploadRowResult(
                        row = index + 1,
                        identifier = identifier,
                        success = true,
                        message = "배정 완료",
                    ),
                )
            }
        }

        return ParticipantUploadResultDto(
            totalRows = rows.size,
            successCount = successCount,
            failureCount = rows.size - successCount,
            rows = rows,
        )
    }
}
