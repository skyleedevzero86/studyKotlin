package com.kposti.global.initsdata

import com.kposti.domain.member.service.MemberService
import com.kposti.domain.post.service.PostService
import com.kposti.global.app.AppConfig
import com.kposti.global.app.CustomConfigProperties
import com.kposti.standard.utils.UtClass
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.transaction.annotation.Transactional

@Configuration
class BaseInitData(
    private val customConfigProperties: CustomConfigProperties,
    private val memberService: MemberService,
    private val postService: PostService,
    @Lazy private val self: BaseInitData
) {

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner = ApplicationRunner {
        self.work1()
        self.work2()
    }

    @Transactional
    fun work1() {
        if (memberService.count() > 0) return

        if (AppConfig.isTest()) {
            UtClass.file.rm(AppConfig.genFileDirPath)
        }

        listOf(
            "system" to "시스템",
            "admin" to "관리자",
            "user1" to "유저1",
            "user2" to "유저2",
            "user3" to "유저3",
            "user4" to "유저4",
            "user5" to "유저5",
            "user6" to "유저6"
        ).forEach { (username, nickname) ->
            memberService.join(username, "1234", nickname, "").apply {
                if (AppConfig.isNotProd()) this.apiKey = username
            }
        }


        customConfigProperties.notProdMembers.forEach { notProdMember ->
            memberService.join(
                notProdMember.username,
                "",
                notProdMember.nickname,
                notProdMember.profileImgUrl
            ).apply {
                if (AppConfig.isNotProd()) this.apiKey = notProdMember.apiKey()
            }
        }
    }

    @Transactional
    fun work2() {
        if (postService.count() > 0) return

        val members = listOf("user1", "user2", "user3", "user4", "user5", "user6")
            .associateWith { memberService.findByUsername(it).get() }

        postService.write(
            members["user1"]!!,
            "2025-05-10 ~ 2025-05-12",
            "",
            true,
            true
        ).apply {
            addComment(members["user2"]!!, "DAY 1 : 2025-05-10 14:00 : 장보기")
            addComment(members["user3"]!!, "DAY 2 : 2025-05-11 14:00 : 성산일출봉")
        }
    }
}