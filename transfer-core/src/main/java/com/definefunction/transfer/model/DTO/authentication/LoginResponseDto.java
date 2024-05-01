package com.definefunction.transfer.model.DTO.authentication;

import com.definefunction.transfer.model.pojo.AuthenticationRole;

class Details {

    int id;
    String username;
    String email;
    AuthenticationRole authenticationRole;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AuthenticationRole getAuthenticationRole() {
        return authenticationRole;
    }

    public void setAuthenticationRole(AuthenticationRole authenticationRole) {
        this.authenticationRole = authenticationRole;
    }
}

public class LoginResponseDto {
    private boolean success;

    private String message;

    private String token;

    private Details details;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(int id, String username, String email, AuthenticationRole authenticationRole) {
        this.details = new Details();
        this.details.setId(id);
        this.details.setUsername(username);
        this.details.setEmail(email);
        this.details.setAuthenticationRole(authenticationRole);
    }
}
