package com.sleekydz86.densevector.unit.config

import com.sleekydz86.densevector.global.config.VectorIndexStartup
import com.sleekydz86.densevector.global.seed.PassageSeedService
import com.sleekydz86.densevector.global.seed.SamplePassageLoader
import com.sleekydz86.densevector.support.RecordingIndexAdmin

class StartupCollaborators(
    val admin: RecordingIndexAdmin,
    val loader: SamplePassageLoader,
    val seed: PassageSeedService,
    val startup: VectorIndexStartup
)
