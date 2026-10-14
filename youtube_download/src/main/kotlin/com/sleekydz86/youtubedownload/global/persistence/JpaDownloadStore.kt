package com.sleekydz86.youtubedownload.global.persistence

import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.VectorCodec
import com.sleekydz86.youtubedownload.global.embedding.downloadText
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.DownloadTicket
import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.downloadStatus
import com.sleekydz86.youtubedownload.video.read.SavedCatalog
import com.sleekydz86.youtubedownload.video.react.DownloadReactions
import com.sleekydz86.youtubedownload.video.react.Reaction
import com.sleekydz86.youtubedownload.video.react.reacted
import com.sleekydz86.youtubedownload.video.save.ClaimedDownload
import com.sleekydz86.youtubedownload.video.save.DownloadClaims
import com.sleekydz86.youtubedownload.video.save.DownloadRequests
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class JpaDownloadStore(
    private val repository: DownloadRepository,
    private val embedder: HashingEmbedder,
) : DownloadRequests, DownloadClaims, DownloadReactions, SavedCatalog {
    @Transactional
    override fun request(video: SharedVideo): DownloadTicket {
        val existing = repository.findFirstByVideoIdOrderByIdDesc(video.id.value)
        if (existing != null && existing.status == DownloadStatus.SAVED.name) return ticket(keepEmbedding(existing))
        if (existing != null && existing.status == DownloadStatus.REQUESTED.name) return ticket(keepEmbedding(existing))
        if (existing != null && existing.status == DownloadStatus.SAVING.name) {
            existing.status = DownloadStatus.REQUESTED.name
            existing.updatedAt = Instant.now()
            return ticket(repository.save(withEmbedding(existing)))
        }
        return ticket(repository.save(requested(video)))
    }

    @Transactional
    override fun claim(id: Long): ClaimedDownload? {
        if (repository.markSaving(id) == 0) return null
        val entity = repository.findById(id).orElse(null) ?: return null
        return ClaimedDownload(entity.id, SharedVideo(VideoId(entity.videoId)))
    }

    @Transactional
    override fun markSaved(id: Long, file: RemoteFile, title: String) {
        val entity = savingRow(id) ?: return
        entity.status = DownloadStatus.SAVED.name
        entity.title = title
        entity.bucket = file.bucket
        entity.objectKey = file.objectKey
        entity.fileName = file.fileName
        entity.sizeBytes = file.sizeBytes
        entity.errorMessage = null
        entity.updatedAt = Instant.now()
        entity.embeddingJson = encoded(entity)
        repository.save(entity)
    }

    @Transactional
    override fun markFailed(id: Long, message: String) {
        val entity = savingRow(id) ?: return
        entity.status = DownloadStatus.FAILED.name
        entity.errorMessage = message
        entity.updatedAt = Instant.now()
        entity.embeddingJson = encoded(entity)
        repository.save(entity)
    }

    @Transactional(readOnly = true)
    override fun find(id: Long): DownloadSnapshot? =
        repository.findById(id).map(::snapshot).orElse(null)

    @Transactional(readOnly = true)
    override fun saved(): List<DownloadSnapshot> =
        repository.findByStatus(DownloadStatus.SAVED.name).map(::snapshot)

    @Transactional
    override fun react(id: Long, reaction: Reaction): DownloadSnapshot {
        val entity = repository.findById(id).orElse(null)
            ?: throw DownloadException("저장된 영상을 찾지 못했습니다. 번호: $id", status = HttpStatus.NOT_FOUND)
        val next = reacted(snapshot(entity), reaction)
        entity.likeCount = next.likeCount
        entity.dislikeCount = next.dislikeCount
        entity.viewCount = next.viewCount
        entity.updatedAt = Instant.now()
        return snapshot(repository.save(entity))
    }

    private fun savingRow(id: Long): DownloadEntity? {
        val entity = repository.findById(id).orElse(null) ?: return null
        if (entity.status != DownloadStatus.SAVING.name) return null
        return entity
    }

    private fun requested(video: SharedVideo): DownloadEntity {
        val now = Instant.now()
        return withEmbedding(
            DownloadEntity(
                videoId = video.id.value,
                watchUrl = video.watchUrl,
                status = DownloadStatus.REQUESTED.name,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun ticket(entity: DownloadEntity): DownloadTicket =
        DownloadTicket(entity.id, VideoId(entity.videoId), statusOf(entity))

    private fun snapshot(entity: DownloadEntity): DownloadSnapshot =
        DownloadSnapshot(
            id = entity.id,
            videoId = VideoId(entity.videoId),
            title = entity.title,
            status = statusOf(entity),
            bucket = entity.bucket,
            objectKey = entity.objectKey,
            fileName = entity.fileName,
            sizeBytes = entity.sizeBytes,
            errorMessage = entity.errorMessage,
            updatedAt = entity.updatedAt,
            embeddingJson = entity.embeddingJson,
            likeCount = entity.likeCount,
            dislikeCount = entity.dislikeCount,
            viewCount = entity.viewCount,
        )

    private fun keepEmbedding(entity: DownloadEntity): DownloadEntity {
        if (entity.embeddingJson.isNotBlank()) return entity
        return repository.save(withEmbedding(entity))
    }

    private fun withEmbedding(entity: DownloadEntity): DownloadEntity {
        if (entity.embeddingJson.isBlank()) entity.embeddingJson = encoded(entity)
        return entity
    }

    private fun encoded(entity: DownloadEntity): String =
        VectorCodec.encode(
            embedder.embed(downloadText(entity.videoId, entity.title, entity.fileName, entity.errorMessage)),
        )

    private fun statusOf(entity: DownloadEntity): DownloadStatus =
        downloadStatus(entity.status) ?: DownloadStatus.FAILED
}
