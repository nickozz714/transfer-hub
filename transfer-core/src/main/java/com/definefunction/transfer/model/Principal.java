package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.AuthenticationRole;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;

    private String password;

    private String email;

    private AuthenticationRole authenticationRole;

    @OneToMany(mappedBy = "principal")
    List<ScopePrincipal> principals;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public List<ScopePrincipal> getPrincipals() {
        return principals;
    }

    public void setPrincipals(List<ScopePrincipal> principals) {
        this.principals = principals;
    }
}
