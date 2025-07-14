package com.kominioai.global.exception

class SurveyNotFoundException(surveyId: String) : SurveyDomainException("설문을 찾을 수 없습니다: $surveyId")