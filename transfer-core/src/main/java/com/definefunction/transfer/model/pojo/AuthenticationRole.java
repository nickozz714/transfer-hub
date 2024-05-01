package com.definefunction.transfer.model.pojo;

public enum AuthenticationRole {

    ADMIN("ADMIN"), PRINCIPAL("PRINCIPAL");

    private final String role;

    AuthenticationRole(String string) {
        role = string;
    }

    @Override
    public String toString() {
        return role;
    }
}
