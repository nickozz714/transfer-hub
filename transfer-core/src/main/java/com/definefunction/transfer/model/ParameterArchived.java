package com.definefunction.transfer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parameter_archived")
public class ParameterArchived {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "parameter_name")
    private String name;

    @Column(name = "parameter_value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "endpoint_history_id", nullable = false)
    private EndpointHistory endpointHistory;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public EndpointHistory getEndpointHistory() {
        return endpointHistory;
    }

    public void setEndpointHistory(EndpointHistory endpointHistory) {
        this.endpointHistory = endpointHistory;
    }
}
