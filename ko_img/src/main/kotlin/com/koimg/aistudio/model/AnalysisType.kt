package com.koimg.aistudio.model

enum class AnalysisType(val displayName: String, val prompt: String) {
    AGE_ESTIMATION("나이 추정", "이 이미지에서 보이는 사람의 나이를 추정해주세요. 그렇게 생각한 이유를 자세히 설명해주세요."),
    EMOTION_ANALYSIS("감정 분석", "이 사진 속 사람의 감정 상태를 분석해주세요. 표정, 눈빛, 자세 등을 종합적으로 판단하여 주요 감정과 그 이유를 설명해주세요."),
    FASHION_ANALYSIS("패션 분석", "이 사진의 패션 스타일을 분석하고, 스타일의 특징과 어울리는 상황을 설명해주세요."),
    SCENE_DESCRIPTION("장면 설명", "이 이미지를 자세히 묘사해주세요. 배경, 사물, 분위기, 색감 등을 포함하여 종합적으로 설명해주세요."),
    STORY_GENERATION("이야기 생성", "이 이미지를 보고 창의적인 500자 내외의 짧은 이야기를 만들어주세요."),
    RECIPE_SUGGESTION("레시피 추천", "이 음식 사진을 보고 만드는 방법과 필요한 재료를 추정하여 레시피를 제안해주세요.");

    companion object {
        fun fromString(value: String): AnalysisType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}