package com.sleekydz86.densevector.unit.config

import com.sleekydz86.densevector.global.config.VectorIndexStartup
import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.persistence.PassageDimGuard
import com.sleekydz86.densevector.global.seed.PassageSeedService
import com.sleekydz86.densevector.global.seed.SamplePassageLoader
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingIndexAdmin
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.ApplicationArguments
import kotlin.test.Test
import kotlin.test.assertTrue

class VectorIndexStartupTest {
    @Test
    fun startupSeedFalseSkipsSampleLoad() {
        val collaborators = startupCollaborators(false)

        collaborators.startup.run(mock<ApplicationArguments>())

        assertTrue(collaborators.admin.ensured)
        verifyNoInteractions(collaborators.loader)
        verifyNoInteractions(collaborators.seed)
    }

    @Test
    fun startupSeedTrueFillsToOneThousand() {
        val collaborators = startupCollaborators(true)
        whenever(collaborators.loader.load()).thenReturn(6)
        whenever(collaborators.seed.fillTo(1000)).thenReturn(994)

        collaborators.startup.run(mock<ApplicationArguments>())

        verify(collaborators.loader).load()
        verify(collaborators.seed).fillTo(1000)
    }

    private fun startupCollaborators(startupSeed: Boolean): StartupCollaborators {
        val properties = VectorProperties().apply { this.startupSeed = startupSeed }
        val loader: SamplePassageLoader = mock()
        val seed: PassageSeedService = mock()
        val admin = RecordingIndexAdmin()
        val startup = VectorIndexStartup(
            admin,
            PassageDimGuard(InMemoryPassages().repository, properties),
            loader,
            seed,
            properties
        )
        return StartupCollaborators(admin, loader, seed, startup)
    }
}

private class StartupCollaborators(
    val admin: RecordingIndexAdmin,
    val loader: SamplePassageLoader,
    val seed: PassageSeedService,
    val startup: VectorIndexStartup
)
