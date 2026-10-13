package com.sleekydz86.densevector.passage.get

import com.sleekydz86.densevector.global.passage.PassageDetail
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.persistence.PassageLookup
import org.springframework.stereotype.Service

@Service
class PassageGetService(private val lookup: PassageLookup) {
    fun get(id: Long): PassageDetail = PassageViews.detail(lookup.get(id))
}
