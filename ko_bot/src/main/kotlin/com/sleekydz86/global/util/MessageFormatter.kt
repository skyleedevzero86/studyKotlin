package com.sleekydz86.global.util

import com.sleekydz86.discod.entity.Comment
import com.sleekydz86.discod.entity.Feed
import com.sleekydz86.discod.entity.ReportedType
import com.sleekydz86.discod.entity.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MessageFormatter { // 정적 메서드를 제공하므로 object 선언

    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"

    private const val FEED_REPORT_MESSAGE =
        "```[%s] 피드 %s 신고가 접수되었습니다.\n\n" +
                "[신고된 피드 작성자]\n" +
                "유저 아이디 : %d\n" +
                "유저 닉네임 : %s\n\n" +
                "[신고된 피드 내용]\n%s\n\n" +
                "[신고 횟수]\n총 신고 횟수 %d회.\n" +
                "%s\n```"

    private const val COMMENT_REPORT_MESSAGE =
        "```[%s] 피드 댓글 %s 신고가 접수되었습니다.\n\n" +
                "[신고된 댓글 작성자]\n" +
                "유저 아이디 : %d\n" +
                "유저 닉네임 : %s\n\n" +
                "[신고된 댓글 내용]\n%s\n\n" +
                "[신고 횟수]\n총 신고 횟수 %d회.\n" +
                "%s\n```"

    @JvmStatic // Java 코드에서 이 메서드를 정적으로 호출할 수 있도록 함
    fun formatFeedReportMessage(feed: Feed, reportedType: ReportedType, reportedCount: Int, isHidden: Boolean): String {
        val hiddenMessage = if (isHidden) "해당 피드는 숨김 처리되었습니다." else "해당 피드는 숨김 처리되지 않았습니다."

        return String.format(
            FEED_REPORT_MESSAGE,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            reportedType.description,
            feed.user.userId,
            feed.user.nickname,
            feed.feedContent,
            reportedCount,
            hiddenMessage
        )
    }

    @JvmStatic // Java 코드에서 이 메서드를 정적으로 호출할 수 있도록 함
    fun formatCommentReportMessage(comment: Comment, reportedType: ReportedType, user: User,
                                   reportedCount: Int, isHidden: Boolean): String {
        var hiddenMessage = "해당 댓글은 현재 숨김 처리되지 않은 상태입니다."
        if (isHidden) {
            when (reportedType) {
                ReportedType.SPOILER -> hiddenMessage = "해당 댓글은 스포일러 댓글로 지정되었습니다." // Modified
                ReportedType.IMPERTINENCE -> hiddenMessage = "해당 댓글은 부적절한 내용으로 인해 숨김 처리되었습니다." // Modified
                else -> { /* 기타 신고 유형에 대한 처리 (필요시 추가) */ }
            }
        }

        return String.format(
            COMMENT_REPORT_MESSAGE,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            reportedType.description,
            user.userId,
            user.nickname,
            comment.commentContent,
            reportedCount,
            hiddenMessage
        )
    }
}
