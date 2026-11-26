package org.example.library_management.controller;

import org.example.library_management.dto.LibrarianLoginRequest;
import org.example.library_management.dto.LoginResponse;
import org.example.library_management.dto.MemberLoginRequest;
import org.example.library_management.dto.MemberRegistrationRequest;
import org.example.library_management.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/member/register")
    public Map<String, Object> registerMember(@RequestBody MemberRegistrationRequest request) {
        return authService.registerMember(request);
    }

    @PostMapping("/member/login")
    public LoginResponse loginMember(@RequestBody MemberLoginRequest request) {
        return authService.loginMember(request);
    }

    @PostMapping("/librarian/login")
    public LoginResponse loginLibrarian(@RequestBody LibrarianLoginRequest request) {
        return authService.loginLibrarian(request);
    }
}
