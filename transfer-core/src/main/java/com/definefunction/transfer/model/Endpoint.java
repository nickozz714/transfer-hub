package com.definefunction.transfer.model;

import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(value = {View.UserView.GET.class, View.UserView.PUT.class})
    private long id;
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String protocol;
    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    @JsonManagedReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Host host;
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String path;
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Direction direction;
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private String parameter;
    @ManyToOne
    @JoinColumn(name = "transferRecord_id", nullable = true)
    @JsonBackReference
    @JsonIgnore
    private TransferRecord transferRecord;

//    @OneToMany(mappedBy = "endpoint", fetch = FetchType.EAGER)
//    @JsonManagedReference
//    private List<Parameter> parameterList;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credential_id", nullable = true)
    @JsonManagedReference
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Credential credential;

    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private Date last_updated_at;

    public Endpoint(String protocol, Host host, String path, Direction direction, Credential credential) {
        this.protocol = protocol;
        this.host = host;
        this.path = path;
        this.direction = direction;
        this.credential = credential;
        this.last_updated_at = Date.from(ZonedDateTime.now().toInstant());
    }

    public Endpoint() {

    }
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
    public Credential getCredential() {
        return credential;
    }
    public TransferRecord getTransferRecord() {
        return transferRecord;
    }
    public void setTransferRecord(TransferRecord transferRecord) {
        this.transferRecord = transferRecord;
    }
    public void setCredential(Credential credential) {
        this.credential = credential;
    }
    public Date getLast_updated_at() {
        return last_updated_at;
    }
    public void setLast_updated_at(Date last_updated_at) {
        this.last_updated_at = last_updated_at;
    }
    public String getParameter() {
        return parameter;
    }
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
    public Host getHost() {
        return host;
    }
    public void setHost(Host host) {
        this.host = host;
    }
}
