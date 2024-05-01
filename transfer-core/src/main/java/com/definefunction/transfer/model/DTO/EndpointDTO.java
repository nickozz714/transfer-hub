package com.definefunction.transfer.model.DTO;

import com.definefunction.transfer.model.Host;
import com.definefunction.transfer.model.pojo.Direction;

public class EndpointDTO {
    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    private String protocol;

    private Host host;


    private String path;

    private Direction direction;

    private String parameter;

    private CredentialDTO credentialDTO;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public CredentialDTO getCredentialDTO() {
        return credentialDTO;
    }

    public void setCredentialDTO(CredentialDTO credentialDTO) {
        this.credentialDTO = credentialDTO;
    }
}
