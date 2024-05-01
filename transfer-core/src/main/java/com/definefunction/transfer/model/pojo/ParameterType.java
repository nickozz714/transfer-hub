package com.definefunction.transfer.model.pojo;

public enum ParameterType {
    VALUE("VALUE"), BOOLEAN("BOOLEAN");

    private final String parameterType;

    ParameterType(String string) {this.parameterType = string;}

    @Override
    public String toString() {return parameterType;}
}
