package com.monitorx.controller

import com.monitorx.dto.AuthResponse
import com.monitorx.dto.LoginRequest
import com.monitorx.dto.SignupRequest
import com.monitorx.dto.UserDto
import com.monitorx.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<UserDto> {
        val user = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val authResponse = authService.login(request)
        return ResponseEntity.ok(authResponse)
    }
}
