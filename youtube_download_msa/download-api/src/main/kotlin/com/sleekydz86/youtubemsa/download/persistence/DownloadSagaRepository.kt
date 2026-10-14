package com.sleekydz86.youtubemsa.download.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface DownloadSagaRepository : JpaRepository<DownloadSagaEntity, Long>
