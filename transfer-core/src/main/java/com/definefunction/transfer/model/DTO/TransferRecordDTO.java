package com.definefunction.transfer.model.DTO;


import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.pojo.Status;

import java.util.List;

public class TransferRecordDTO {

    private String id;

    private String description;

    private ScopeDTO scope;

    private Status status;

    private List<EndpointDTO> endpoints;

    public String getId() {
        return id;
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

    public List<EndpointDTO> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<EndpointDTO> endpoints) {
        this.endpoints = endpoints;
    }

    public ScopeDTO getScope() {
        return scope;
    }

    public void setScope(ScopeDTO scope) {
        this.scope = scope;
    }
}
