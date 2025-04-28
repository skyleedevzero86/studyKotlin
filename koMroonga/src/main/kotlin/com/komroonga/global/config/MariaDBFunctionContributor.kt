package com.komroonga.global.config

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.boot.model.FunctionContributor
import org.hibernate.type.StandardBasicTypes

class MariaDBFunctionContributor : FunctionContributor {
    override fun contributeFunctions(functionContributions: FunctionContributions) {
        // MATCH ... AGAINST 함수 등록
        functionContributions.functionRegistry.registerPattern(
            "match",
            "match (?1) against (?2 in boolean mode) > 0",
            functionContributions.typeConfiguration.basicTypeRegistry
                .resolve(StandardBasicTypes.BOOLEAN)
        )

        // SCORE 함수 등록
        functionContributions.functionRegistry.registerPattern(
            "score",
            "match (?1) against (?2 in boolean mode)",
            functionContributions.typeConfiguration.basicTypeRegistry
                .resolve(StandardBasicTypes.DOUBLE)
        )
    }
}