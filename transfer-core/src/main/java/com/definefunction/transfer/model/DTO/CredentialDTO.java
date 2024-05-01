package com.definefunction.transfer.model.DTO;

import com.definefunction.transfer.model.pojo.CredentialType;

public class CredentialDTO {
    private long id;

    private String username;

    private String password;

    private String public_key;

    private String private_key;

    private String client_id;

    private String client_secret;

    private String tenant_id;

    private String token;

    private String key_phrase;

    private CredentialType credentialType;

    private ScopeDTO scope;

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

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getKey_phrase() {
        return key_phrase;
    }

    public void setKey_phrase(String key_phrase) {
        this.key_phrase = key_phrase;
    }

    public CredentialType getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(CredentialType credentialType) {
        this.credentialType = credentialType;
    }

    public ScopeDTO getScope() {
        return scope;
    }

    public void setScope(ScopeDTO scope) {
        this.scope = scope;
    }
}
