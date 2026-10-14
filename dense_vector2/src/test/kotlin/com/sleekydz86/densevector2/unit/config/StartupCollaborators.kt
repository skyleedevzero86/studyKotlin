package com.sleekydz86.densevector2.unit.config

import com.sleekydz86.densevector2.global.config.VectorIndexStartup
import com.sleekydz86.densevector2.global.seed.PassageSeedService
import com.sleekydz86.densevector2.global.seed.SamplePassageLoader
import com.sleekydz86.densevector2.support.RecordingIndexAdmin

class StartupCollaborators(
    val admin: RecordingIndexAdmin,
    val loader: SamplePassageLoader,
    val seed: PassageSeedService,
    val startup: VectorIndexStartup
)
