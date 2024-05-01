package com.definefunction.transfer.service;

import com.definefunction.transfer.model.DTO.authentication.AuthDto;
import com.definefunction.transfer.model.DTO.authentication.AuthRoleDto;
import com.definefunction.transfer.model.DTO.authentication.LoginResponseDto;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.pojo.AuthenticationRole;
import com.definefunction.transfer.security.CustomUserDetailsService;
import com.definefunction.transfer.security.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PrincipalService principalService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtGenerator jwtGenerator;

    public Principal retrievePrincipalFromToken(String token) {
        String username = jwtGenerator.getUsernameFromJWT(token);
        return principalService.findPrincipalByUsername(username).orElseThrow();
    }

    public ResponseEntity<String> register(AuthDto authDto) {
        if (principalService.findPrincipalByUsername(authDto.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already in use!", HttpStatus.BAD_REQUEST);
        } else if (principalService.findPrincipalByEmail(authDto.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email is already in use", HttpStatus.BAD_REQUEST);
        }
        Principal principal = new Principal();
        principal.setUsername(authDto.getUsername());
        principal.setPassword(passwordEncoder.encode(authDto.getPassword()));
        principal.setEmail(principal.getEmail());
        principal.setAuthenticationRole(AuthenticationRole.PRINCIPAL);
        principalService.savePrincipal(principal);
        return new ResponseEntity<>("User registration has been successfull", HttpStatus.CREATED);
    }

   public ResponseEntity<LoginResponseDto> login(AuthDto authDto) {
       Authentication authentication = authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(authDto.getUsername(), authDto.getPassword()));
       SecurityContextHolder.getContext().setAuthentication(authentication);

       String token = jwtGenerator.generateToken(authentication);
       LoginResponseDto loginResponseDto = new LoginResponseDto();
       loginResponseDto.setSuccess(true);
       loginResponseDto.setMessage("Login has been successfull");
       loginResponseDto.setToken(token);
       Principal principal = principalService.findPrincipalByUsername(authDto.getUsername()).orElseThrow();
       loginResponseDto.setDetails((int) principal.getId(), principal.getUsername(), principal.getEmail(), principal.getAuthenticationRole());
       return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
   }

   public ResponseEntity<LoginResponseDto> updateAuthenticationRole(AuthRoleDto authRoleDto, String token) {
       LoginResponseDto loginResponseDto = new LoginResponseDto();
        if (jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            Principal principal = principalService.findPrincipalByUsername(username).orElseThrow();
            if (principal.getAuthenticationRole() == AuthenticationRole.ADMIN) {
                Principal editPrincipal = principalService.findPrincipalByUsername(String.valueOf(authRoleDto.getId_of_user())).orElseThrow();
                editPrincipal.setAuthenticationRole(authRoleDto.getAuthenticationRole());
                principalService.savePrincipal(editPrincipal);

                loginResponseDto.setSuccess(true);
                loginResponseDto.setMessage("Authentication role of user succesfully updated to " + authRoleDto.getAuthenticationRole().toString());
                loginResponseDto.setToken(token);
                loginResponseDto.setDetails((int) editPrincipal.getId(), editPrincipal.getUsername(), editPrincipal.getEmail(), editPrincipal.getAuthenticationRole());
                return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
            } else
                loginResponseDto.setSuccess(false);
                loginResponseDto.setMessage("You don't have the correct role to request this.");
                return new ResponseEntity<>(loginResponseDto, HttpStatus.BAD_REQUEST);
        } else {
            loginResponseDto.setSuccess(false);
            loginResponseDto.setMessage("Token is not valid");
            return new ResponseEntity<>(loginResponseDto, HttpStatus.BAD_REQUEST);
        }
   }
}
