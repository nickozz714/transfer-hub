package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.Role;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class ScopePrincipal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "principal_id")
    @JsonBackReference
    private Principal principal;

    @ManyToOne
    @JoinColumn(name = "scope_id")
    @JsonBackReference
    private Scope scope;

    private Role role;

    private Date last_changed_on;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getLast_changed_on() {
        return last_changed_on;
    }

    public void setLast_changed_on(Date last_changed_on) {
        this.last_changed_on = last_changed_on;
    }
}
