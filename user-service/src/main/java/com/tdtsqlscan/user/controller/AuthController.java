package com.tdtsqlscan.user.controller;

import com.tdtsqlscan.user.dto.LoginRequestDto;
import com.tdtsqlscan.user.dto.LoginResponseDto;
import com.tdtsqlscan.user.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication and token generation")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and generate JWT",
            description = "Authenticates user credentials and returns a JWT if successful.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Authentication successful"),
                @ApiResponse(responseCode = "401", description = "Invalid credentials")
            })
    public ResponseEntity<LoginResponseDto> authenticateUser(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDto.getUsername(), loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);

        return ResponseEntity.ok(new LoginResponseDto(jwt));
    }
}
