package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.serialization.PropertySerializer;
import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "credentialType", "username", "password", "public_key", "private_key", "key_phrase", "token", "client_id", "client_secret", "tenant_id", "scope"})
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    private long id;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    @JsonProperty("username")
    private String username;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    @JsonProperty("password")
    @JsonSerialize(using = PropertySerializer.class)
    private String password;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    @Column(columnDefinition = "TEXT")
    private String public_key;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    @Column(columnDefinition = "TEXT")
    @JsonSerialize(using = PropertySerializer.class)
    private String private_key;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String client_id;

    @JsonView(value = {View.UserView.Post.class, View.UserView.PUT.class})
    private String client_secret;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String tenant_id;

    @JsonView(value = {View.UserView.Post.class, View.UserView.PUT.class})
    private String token;

    @JsonView(value = {View.UserView.Post.class, View.UserView.PUT.class})
    private String key_phrase;

    @JsonProperty("type")
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private CredentialType credentialType;

    @ManyToOne
    @JoinColumn(name = "scope_id")
    @JsonManagedReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Scope scope;

    @JsonView(value = {View.UserView.GET.class,})
    private ZonedDateTime last_updated_at;

    @OneToMany(mappedBy = "credential", fetch = FetchType.EAGER)
    @JsonIgnore
    @JsonBackReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private List<Endpoint> endpoints;

    public Credential(String username, String password, CredentialType credentialType) {
        this.username = username;
        this.password = password;
        this.credentialType = credentialType;
    }

    public Credential(String username, String password, String public_key, String private_key, String key_phrase, CredentialType credentialType) {
        this.username = username;
        this.password = password;
        this.public_key = public_key;
        this.private_key = private_key;
        this.key_phrase = key_phrase;
        this.credentialType = credentialType;
    }

    public Credential() {

    }

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

    public String getKey_phrase() {
        return key_phrase;
    }

    public void setKey_phrase(String key_phrase) {
        this.key_phrase = key_phrase;
    }

    public CredentialType getCredentialType() {
        return credentialType;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setCredentialType(CredentialType credentialType) {
        this.credentialType = credentialType;
    }

    public ZonedDateTime getLast_updated_at() {
        return last_updated_at;
    }

    public void setLast_updated_at(ZonedDateTime last_updated_at) {
        this.last_updated_at = last_updated_at;
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

    public String gettoken() {
        return token;
    }

    public void settoken(String token) {
        this.token = token;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
