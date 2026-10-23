package com.sleekydz86.multitenant.domain

class UnknownTenant(val tenant: String) :
    RuntimeException("등록되지 않은 테넌트입니다. 입력값: $tenant")
