package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.ParameterDirection;
import com.definefunction.transfer.model.pojo.ParameterType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "parameter")
public class Parameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "parameter_name")
    private String name;

    @Column(name = "parameter_display_name")
    private String displayName;

    @Column(name = "parameter_value")
    private ParameterType parameterType;

    @Column(name = "parameter_direction")
    private ParameterDirection parameterDirection;

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

    public ParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ParameterDirection getParameterDirection() {
        return parameterDirection;
    }

    public void setParameterDirection(ParameterDirection parameterDirection) {
        this.parameterDirection = parameterDirection;
    }
}
