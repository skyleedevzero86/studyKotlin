package com.sleekydz86.youtubemsa.download.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "download")
class DownloadEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "video_id", nullable = false, length = 11)
    var videoId: String = "",
    @Column(name = "watch_url", nullable = false, length = 80)
    var watchUrl: String = "",
    @Column(length = 300)
    var title: String? = null,
    @Column(nullable = false, length = 20)
    var status: String = "",
    @Column(length = 80)
    var bucket: String? = null,
    @Column(name = "object_key", length = 300)
    var objectKey: String? = null,
    @Column(name = "file_name", length = 300)
    var fileName: String? = null,
    @Column(name = "size_bytes")
    var sizeBytes: Long? = null,
    @Column(name = "error_message", length = 500)
    var errorMessage: String? = null,
    @Column(name = "embedding_json", nullable = false, length = 2000)
    var embeddingJson: String = "",
    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
    @Column(name = "dislike_count", nullable = false)
    var dislikeCount: Int = 0,
    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH,
)
