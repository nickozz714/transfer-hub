package com.definefunction.transfer.model.pojo;

public enum Role {

    ADMIN("ADMIN"), PRINCIPAL("PRINCIPAL");

    private final String role;

    Role(String string) {
        role = string;
    }

    @Override
    public String toString() {
        return role;
    }
}
