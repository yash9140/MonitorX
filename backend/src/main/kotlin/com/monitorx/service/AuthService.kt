package com.monitorx.service

import com.monitorx.dto.LoginRequest
import com.monitorx.dto.SignupRequest
import com.monitorx.dto.AuthResponse
import com.monitorx.dto.UserDto
import com.monitorx.model.User
import com.monitorx.repository.metadata.UserRepository
import com.monitorx.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    fun signup(request: SignupRequest): UserDto {
        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }
        
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username already taken")
        }

        // Create new user
        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = request.role
        )

        val savedUser = userRepository.save(user)

        return UserDto(
            id = savedUser.id!!,
            username = savedUser.username,
            email = savedUser.email,
            role = savedUser.role
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val token = jwtUtil.generateToken(user.username, user.role)

        return AuthResponse(
            token = token,
            user = UserDto(
                id = user.id!!,
                username = user.username,
                email = user.email,
                role = user.role
            )
        )
    }
}
