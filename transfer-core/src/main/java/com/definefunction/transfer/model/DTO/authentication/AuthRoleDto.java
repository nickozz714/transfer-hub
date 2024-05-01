package com.definefunction.transfer.model.DTO.authentication;

import com.definefunction.transfer.model.pojo.AuthenticationRole;

public class AuthRoleDto {

    long id_of_user;

    AuthenticationRole authenticationRole;

    public long getId_of_user() {
        return id_of_user;
    }

    public void setId_of_user(long id_of_user) {
        this.id_of_user = id_of_user;
    }

    public AuthenticationRole getAuthenticationRole() {
        return authenticationRole;
    }

    public void setAuthenticationRole(AuthenticationRole authenticationRole) {
        this.authenticationRole = authenticationRole;
    }
}
