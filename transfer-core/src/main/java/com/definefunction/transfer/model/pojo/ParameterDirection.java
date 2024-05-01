package com.definefunction.transfer.model.pojo;

public enum ParameterDirection {

    source("source"), destination("destination");

    private final String parameterDirection;

    ParameterDirection(String string) {
        this.parameterDirection = string;
    }

    @Override
    public String toString() {return parameterDirection;}
}
