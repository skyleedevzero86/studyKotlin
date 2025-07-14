package com.kominioai.domain.survey.domain.model

data class ParticipantInfo(
    val userId: String?,
    val name: String?,
    val phone: String?,
    val authenticated: Boolean
) {
    init {
        if (!authenticated) {
            require(!name.isNullOrBlank()) { "이름은 필수입니다." }
            require(!phone.isNullOrBlank()) { "전화번호는 필수입니다." }
        }
    }

    fun maskedPhone(): String? =
        phone?.replace(Regex("(\\d{3})\\d{3,4}(\\d{4})"), "$1****$2")
}