package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.Status;
import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class TransferRecord {

    @Id
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String id;

    @Column(columnDefinition = "LONGTEXT")
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String description;

    @ManyToOne
    @JoinColumn(name = "scope_id", nullable = false)
    @JsonManagedReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Scope scope;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Status status;

    @JsonIgnore
    private int version;

    @OneToMany(mappedBy = "transferRecord", fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private List<Endpoint> endpoints;

    @JsonView(value = {View.UserView.GET.class})
    private Date last_updated_at;

    public TransferRecord() {

    }

    public String getId() {
        return id;
    }

    public TransferRecord(String id, String description, Status status, List<Endpoint> endpoints, Scope scope) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.endpoints = endpoints;
        this.scope = scope;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public Date getLast_updated_at() {
        return last_updated_at;
    }

    public void setLast_updated_at(Date last_updated_at) {
        this.last_updated_at = last_updated_at;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
