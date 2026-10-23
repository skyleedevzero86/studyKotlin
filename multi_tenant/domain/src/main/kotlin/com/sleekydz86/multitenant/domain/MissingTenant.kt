package com.sleekydz86.multitenant.domain

class MissingTenant : RuntimeException("요청에 테넌트가 없습니다. 헤더 X-Tenant-Id 가 필요합니다.")
