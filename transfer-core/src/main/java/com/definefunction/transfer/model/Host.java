package com.definefunction.transfer.model;

import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.List;


@Entity
public class Host {

    public Host() {
    }

    public Host(String hostname) {
        this.hostname = hostname;
    }

    public Host(String hostname, String description) {
        this.hostname = hostname;
        this.description = description;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    private long id;

    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    private String hostname;

    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    @Column(nullable = true)
    private int port;

    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    @Column(columnDefinition = "LONGTEXT", nullable = true)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scope_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = false)
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Scope scope;

    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class, View.UserView.DELETE.class})
    private ZonedDateTime last_updated_at;

    @OneToMany(mappedBy = "host", fetch = FetchType.EAGER)
    @JsonBackReference
    @JsonIgnore
    private List<Endpoint> endpoints;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public ZonedDateTime getLast_updated_at() {
        return last_updated_at;
    }

    public void setLast_updated_at(ZonedDateTime last_updated_at) {
        this.last_updated_at = last_updated_at;
    }
}
