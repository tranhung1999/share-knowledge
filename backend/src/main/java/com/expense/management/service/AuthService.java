package com.expense.management.service;

import com.expense.management.dto.request.LoginRequest;
import com.expense.management.dto.response.AuthResponse;
import com.expense.management.dto.response.UserResponse;
import com.expense.management.exception.BusinessException;
import com.expense.management.repository.UserRepository;
import com.expense.management.security.JwtTokenProvider;
import com.expense.management.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = tokenProvider.generateToken(principal);

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

            log.info("User logged in: {}", request.getEmail());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getAccessTokenExpirationMs() / 1000)
                    .user(UserResponse.from(user))
                    .build();
        } catch (AuthenticationException ex) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
