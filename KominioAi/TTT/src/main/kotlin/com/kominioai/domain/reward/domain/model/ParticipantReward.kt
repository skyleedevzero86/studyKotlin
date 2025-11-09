package com.kominioai.domain.reward.domain.model

data class ParticipantReward(
    val enabled: Boolean,
    val type: RewardType,
    val value: Int,
    val description: String,
    val probability: Double,
    val imageUrl: String?
) {
    companion object {
        fun create(
            type: RewardType,
            value: Int,
            description: String,
            probability: Double = 1.0,
            imageUrl: String? = null
        ): ParticipantReward {
            return ParticipantReward(
                enabled = true,
                type = type,
                value = value,
                description = description,
                probability = probability,
                imageUrl = imageUrl
            )
        }
        
        fun disabled(): ParticipantReward {
            return ParticipantReward(
                enabled = false,
                type = RewardType.POINTS,
                value = 0,
                description = "",
                probability = 0.0,
                imageUrl = null
            )
        }
    }
    
    fun update(
        enabled: Boolean? = null,
        type: RewardType? = null,
        value: Int? = null,
        description: String? = null,
        probability: Double? = null,
        imageUrl: String? = null
    ): ParticipantReward {
        return copy(
            enabled = enabled ?: this.enabled,
            type = type ?: this.type,
            value = value ?: this.value,
            description = description ?: this.description,
            probability = probability ?: this.probability,
            imageUrl = imageUrl ?: this.imageUrl
        )
    }
}
