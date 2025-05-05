import jakarta.persistence.*
import com.komroonga.member.entity.Member
import org.hibernate.annotations.Index
import java.time.LocalDateTime

/**
 * 게시글 공지 유형
 */
enum class NoticeType {
    NONE,       // 일반 게시글
    ALL,        // 전체 공지
    MEMBER      // 회원 공지
}

@Entity
@Table(
    name = "post",
    indexes = [
        Index(name = "idx_post_author_id", columnList = "author_id"),
        Index(name = "idx_post_is_private", columnList = "is_private"),
        Index(name = "idx_post_notice_type", columnList = "notice_type")
    ]
)
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    val content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    val author: Member,

    @Column(name = "is_private", nullable = false)
    val isPrivate: Boolean = false,

    @Column(name = "notice_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val noticeType: NoticeType = NoticeType.NONE,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)