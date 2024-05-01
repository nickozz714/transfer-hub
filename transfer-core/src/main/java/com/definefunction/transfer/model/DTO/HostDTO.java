package com.definefunction.transfer.model.DTO;

import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;

public class HostDTO {

    private long id;

    private String hostname;

    private int port;

    private String description;

    private ScopeDTO scope;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScopeDTO getScope() {
        return scope;
    }

    public void setScope(ScopeDTO scope) {
        this.scope = scope;
    }
}
