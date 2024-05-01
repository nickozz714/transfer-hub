package com.definefunction.transfer.controller;

import com.definefunction.transfer.model.DTO.authentication.AuthDto;
import com.definefunction.transfer.model.DTO.authentication.AuthRoleDto;
import com.definefunction.transfer.model.DTO.authentication.LoginResponseDto;
import com.definefunction.transfer.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthDto authDto) {
        return authenticationService.register(authDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody AuthDto authDto) {
        return authenticationService.login(authDto);
    }

    @PutMapping("/admin/authenticationRole")
    public ResponseEntity<LoginResponseDto> updateAuthenticationRole(@RequestBody AuthRoleDto authRoleDto, @RequestHeader(name = "Authorization") String token) {
        return authenticationService.updateAuthenticationRole(authRoleDto, token);
    }
}
