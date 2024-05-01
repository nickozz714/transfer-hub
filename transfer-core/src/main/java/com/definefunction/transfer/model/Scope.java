package com.definefunction.transfer.model;

import com.definefunction.transfer.model.views.View;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.List;


@Entity
public class Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(value= View.UserView.GET.class)
    private long id;

    @JsonView(value= View.UserView.GET.class)
    private String name;

    @JsonView(value= View.UserView.GET.class)
    private String description;

    @JsonView(value= View.UserView.GET.class)
    private String email;


    // Transferrecords need to be part of a Scope.
    @OneToMany(mappedBy = "scope")
    @JsonBackReference
    @JsonView(value= View.UserView.GET.class)
    @JsonIgnore
    private List<TransferRecord> transferRecord;

    // Principals in a scope. One principal can have multiple scopes, but a single scope can have multiple principals. Hence many to many.
    @OneToMany(mappedBy = "scope")
    @JsonBackReference
    @JsonView(value= View.UserView.GET.class)
    @JsonIgnore
    List<ScopePrincipal> scope;


    @OneToMany(mappedBy = "scope", fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnore
//    @JsonIgnoreProperties("scope") // Voeg deze annotatie toe om hosts te negeren
//    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
//    @JsonIdentityReference(alwaysAsId = false)
    @JsonView(value = {View.UserView.GET.class, View.UserView.Post.class, View.UserView.PUT.class})
    private List<Host> hosts;

    @OneToMany(mappedBy = "scope")
    @JsonBackReference
    @JsonView(value= View.UserView.GET.class)
    @JsonIgnore
    List<Credential> credentials;

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

    public List<TransferRecord> getTransferRecord() {
        return transferRecord;
    }

    public void setTransferRecord(List<TransferRecord> transferRecord) {
        this.transferRecord = transferRecord;
    }

    public List<ScopePrincipal> getScope() {
        return scope;
    }

    public void setScope(List<ScopePrincipal> scope) {
        this.scope = scope;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public List<Credential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credential> credentials) {
        this.credentials = credentials;
    }
}
