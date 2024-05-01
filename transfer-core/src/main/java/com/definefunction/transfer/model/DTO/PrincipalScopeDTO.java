package com.definefunction.transfer.model.DTO;

import com.definefunction.transfer.model.pojo.Role;

public class PrincipalScopeDTO {
    public PrincipalScopeDTO() {
    }

    public PrincipalScopeDTO(long principle_id, String principle_name, ScopeDTO scopeDTO, Role role) {
        this.principle_id = principle_id;
        this.principle_name = principle_name;
        this.scopeDTO = scopeDTO;
        this.role = role;
    }

    private long principle_id;

    private String principle_name;

    private ScopeDTO scopeDTO;

    private Role role;

    public long getPrinciple_id() {
        return principle_id;
    }

    public void setPrinciple_id(long principle_id) {
        this.principle_id = principle_id;
    }

    public ScopeDTO getScopeDTO() {
        return scopeDTO;
    }

    public void setScopeDTO(ScopeDTO scopeDTO) {
        this.scopeDTO = scopeDTO;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPrinciple_name() {
        return principle_name;
    }

    public void setPrinciple_name(String principle_name) {
        this.principle_name = principle_name;
    }
}
