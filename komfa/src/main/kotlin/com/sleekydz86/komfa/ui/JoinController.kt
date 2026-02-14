package com.sleekydz86.komfa.ui

import com.sleekydz86.komfa.application.user.UserService
import com.sleekydz86.komfa.domain.user.JoinRejectedException
import com.sleekydz86.komfa.domain.user.UserRequestDTO
import com.sleekydz86.komfa.ui.dto.JoinErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class JoinController(
    private val userService: UserService,
) {

    @PostMapping("/join")
    fun join(@RequestBody dto: UserRequestDTO): ResponseEntity<Any> {
        return try {
            userService.join(dto)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: JoinRejectedException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(JoinErrorResponse(e.code, e.message!!))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}
