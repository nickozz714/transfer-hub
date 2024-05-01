package com.definefunction.transfer.service;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.UserNotAuthorizedException;
import com.definefunction.transfer.model.DTO.HostDTO;
import com.definefunction.transfer.model.Host;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.repository.HostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostService {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private RecordService recordService;

    public List<Host> findAllHosts(String token) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        return hostRepository.findHostsByPrincipalInScope(principal.getId());
    }

    public Host findHostById(long id) {
        if (hostRepository.existsById(id)) {
            return hostRepository.findById(id).get();
        } else {
            throw new ObjectDoesNotExistsException("Host with id: "+id+" does not exist.");
        }
    }

    public List<Host> findHostsByScopeIs(Scope scope) {
        return hostRepository.findHostsByScopeIs(scope);
    }

    public Host save(String token, HostDTO host) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        Scope scope = scopeService.FindAllById(host.getScope().getId());
        if (principal.getPrincipals().stream().anyMatch(e -> e.getScope() == scope)) {
            Host newHost = createHost(host, true, scope);
            hostRepository.save(newHost);
            return newHost;
        }
        else {
            throw new UserNotAuthorizedException("User is not part of scope " + host.getScope());
        }
    }

    public Host update(String token, HostDTO host) {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        Scope scope = scopeService.FindAllById(host.getScope().getId());
        if (principal.getPrincipals().stream().anyMatch(e -> e.getScope() == scope)) {
            if (hostRepository.existsById(host.getId())) {
                //updating the host.
                Host newHost = createHost(host, false, scope);
                List<TransferRecord> transferRecords = recordService.findAllTransferRecordsOnHost(String.valueOf(host.getId()));
                transferRecords.forEach(transferRecord -> {
                    transferRecord.setVersion(transferRecord.getVersion()+1);
                    recordService.save(transferRecord);
                });
                return newHost;
            } else {
                throw new ObjectDoesNotExistsException("Host "+host.getHostname()+" does not exist.");
            }
        } else {
            throw new UserNotAuthorizedException("User is not part of scope " + host.getScope());
        }
    }

    public boolean remove(String token, long id) {
        try {
            if (hostRepository.existsById(Long.valueOf(id))) {
                Principal principal = authenticationService.retrievePrincipalFromToken(token);
                if (principal.getPrincipals().stream().anyMatch(e -> e.getScope() == hostRepository.getReferenceById(Long.valueOf(id)).getScope())) {
                    hostRepository.deleteById(Long.valueOf(id));
                    return true;
                } else {
                    throw new UserNotAuthorizedException("User is not part of scope " + hostRepository.getReferenceById(Long.valueOf(id)).getScope());
                }
            } else {
                throw new ObjectDoesNotExistsException("Host with id: "+id+" does not exist.");
            }
        } catch (Exception e) {
            return false;
        }
    }

    private Host createHost(HostDTO host, boolean isCreate, Scope scope) {
        Host newHost = new Host();
        if (!isCreate) {
            newHost.setId(host.getId());
        }
        newHost.setHostname(host.getHostname());
        newHost.setPort(host.getPort());
        newHost.setDescription(host.getDescription());
        newHost.setScope(scope);
        return newHost;
    }
}
