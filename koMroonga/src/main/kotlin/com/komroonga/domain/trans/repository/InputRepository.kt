package com.komroonga.domain.trans.repository

import com.komroonga.domain.trans.entity.InputEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InputRepository : JpaRepository<InputEntity, Long>