package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.Direction;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class EndpointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String protocol;

    private String hostname;

    private int port;

    private String path;

    private Direction direction;

    @OneToMany
    private List<ParameterArchived> parameterList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public List<ParameterArchived> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<ParameterArchived> parameterList) {
        this.parameterList = parameterList;
    }
}
