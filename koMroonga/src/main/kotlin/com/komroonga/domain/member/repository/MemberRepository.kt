package com.komroonga.member.repository

import com.komroonga.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByUsername(username: String): Member?

    // 대량 처리를 위한 메서드 추가
    fun findAllByUsernameIn(usernames: List<String>): List<Member>

    @Query("SELECT m FROM Member m WHERE m.username LIKE %:keyword% OR m.name LIKE %:keyword% OR m.email LIKE %:keyword%")
    fun searchByKeyword(@Param("keyword") keyword: String): List<Member>

    // 성능 최적화를 위한 벌크 삽입 메서드
    @Query(
        value = "INSERT INTO member (username, password, name, email, role) VALUES (:username, :password, :name, :email, :role)",
        nativeQuery = true
    )
    fun bulkInsert(
        @Param("username") username: String,
        @Param("password") password: String,
        @Param("name") name: String,
        @Param("email") email: String,
        @Param("role") role: String
    )
}