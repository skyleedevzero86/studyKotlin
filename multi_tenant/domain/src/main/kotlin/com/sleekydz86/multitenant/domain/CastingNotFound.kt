package com.sleekydz86.multitenant.domain

class CastingNotFound(val actorSlug: String) :
    RuntimeException("배역을 찾을 수 없습니다. actorSlug=$actorSlug")
