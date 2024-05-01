package com.definefunction.transfer.service;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.UserNotAuthorizedException;
import com.definefunction.transfer.model.*;
import com.definefunction.transfer.model.DTO.CredentialDTO;
import com.definefunction.transfer.model.DTO.EndpointDTO;
import com.definefunction.transfer.model.DTO.TransferRecordDTO;
import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.pojo.Status;
import com.definefunction.transfer.repository.HostRepository;
import com.definefunction.transfer.repository.TransferRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecordService {

    @Autowired
    private TransferRecordRepository transferRecordRepository;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private EndpointService endpointService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ScopeService scopeService;
    
    @Autowired
    private HostRepository hostRepository;


    public List<TransferRecord> getAllRecords() {
        // Internally used
        return transferRecordRepository.findAll();
    }

    public List<TransferRecord> getAllAuthorizedRecords(String token) {
        // Externally used.
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        return transferRecordRepository.findAllByPrincipalInScope(principal.getId());
    }

    public List<TransferRecord> getAllActiveRecords() {
        // Internally used
        return transferRecordRepository.findAllByStatus(Status.ACTIVE);
    }

    public List<TransferRecord> getAllInactiveRecords() {
        //Internally used
        return transferRecordRepository.findAllByStatus(Status.INACTIVE);
    }

    public List<TransferRecord> getAllRecordsByScope(long id, String token) throws Exception {
        // Externally used.
        Scope scope = scopeService.FindAllById(id);
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
            return transferRecordRepository.findAllByScopeIs(scope);
        } else {
            throw new UserNotAuthorizedException("User is not part of this scope.");
        }
    }

    public List<TransferRecord> getAllErrorRecords() {
        // Internally used
        return transferRecordRepository.findAllByStatus(Status.ERROR);
    }

    public List<TransferRecord> getAllFailedRecords() {
        // Internally used
        return transferRecordRepository.findAllByStatus(Status.FAILED);
    }

    public Optional<TransferRecord> getRecordById(String id) {
        // Internally used
        return transferRecordRepository.findById(id);
    }

    public TransferRecord getRecordByIdExternally(String id, String token) throws Exception {
        // externally used
       Optional<TransferRecord> optionalTransferRecord = transferRecordRepository.findById(id);
       if (optionalTransferRecord.isPresent()) {
           Principal principal = authenticationService.retrievePrincipalFromToken(token);
           Scope scope = optionalTransferRecord.get().getScope();
           if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
               return optionalTransferRecord.get();
           } else {
               throw new UserNotAuthorizedException("User is not part of the scope required to see this transfer");
           }
       } else {
           throw new ObjectDoesNotExistsException("Transfer is not found");
       }
    }

    public void save(TransferRecord transferRecord) {
        // only used internally
        transferRecordRepository.save(transferRecord);
    }

    public TransferRecord savedDTO(TransferRecordDTO transferRecordDTO, String token) throws Exception {
        // used externally
        if (scopeService.existsById(transferRecordDTO.getScope().getId())) {
            Principal principal = authenticationService.retrievePrincipalFromToken(token);
            Scope scope = scopeService.FindAllById(transferRecordDTO.getScope().getId());
            if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
                List<Endpoint> endpoints = getEndpoints(transferRecordDTO);
                TransferRecord record = new TransferRecord(transferRecordDTO.getId(), transferRecordDTO.getDescription(), transferRecordDTO.getStatus(), endpoints, scope);
                record.setVersion(1);
                save(record);
                for (Endpoint endpoint : endpoints) {
                    endpoint.setTransferRecord(record);
                }
                endpointService.saveAll(endpoints);
                return record;
            } else {
                throw new UserNotAuthorizedException("The user is not part of the scope.");
            }
        } else {
            throw new ObjectDoesNotExistsException("Scope: "+transferRecordDTO.getScope().getName() + " does not exist");
        }
    }

    private List<Endpoint> getEndpoints(TransferRecordDTO transferRecordDTO) {
        List<EndpointDTO> endpointDTOS = transferRecordDTO.getEndpoints();
        List<Endpoint> endpoints = new ArrayList<>();
        endpointDTOS.forEach(endpointDTO -> {
            CredentialDTO credentialDTO = endpointDTO.getCredentialDTO();
            String id = String.valueOf(credentialDTO != null ? credentialDTO.getId() : null);
            long hostId = endpointDTO.getHost().getId();
            Credential credential = id != null ? credentialService.findById(id) : null;
            Endpoint endpoint = new Endpoint(endpointDTO.getProtocol(),
                    hostRepository.findById(hostId).orElseThrow(),
                    endpointDTO.getPath(),
                    endpointDTO.getDirection(),
                    credential);
            endpoint.setParameter(endpointDTO.getParameter());
            endpoints.add(endpoint);
        });
        return endpoints;
    }

    public TransferRecord update(TransferRecordDTO transferRecordDTO, String token) throws Exception {
        // used externally
        if (scopeService.existsById(transferRecordDTO.getScope().getId())) {
            Principal principal = authenticationService.retrievePrincipalFromToken(token);
            Scope scope = scopeService.FindAllById(transferRecordDTO.getScope().getId());
            if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
                TransferRecord existingTransferRecord = getRecordById(transferRecordDTO.getId()).orElse(null);
                if (existingTransferRecord != null) {
                    existingTransferRecord.setDescription(transferRecordDTO.getDescription());
                    existingTransferRecord.setStatus(transferRecordDTO.getStatus());
                    existingTransferRecord.getEndpoints().forEach(endpoint -> {
                        EndpointDTO endpointDTO = transferRecordDTO.getEndpoints().stream().filter(e -> e.getDirection() == endpoint.getDirection()).findFirst().orElse(null);
                        if (endpointDTO != null) {
                            endpoint.setProtocol(endpointDTO.getProtocol());
                            endpoint.setHost(endpointDTO.getHost());
                            endpoint.setPath(endpointDTO.getPath());
                            endpoint.setParameter(endpointDTO.getParameter());
                            if (endpointDTO.getCredentialDTO() != null) {
                                endpoint.setCredential(credentialService.findById(String.valueOf(endpointDTO.getCredentialDTO().getId())));
                            }
                        }
                    });
                    existingTransferRecord.setVersion(existingTransferRecord.getVersion()+1);
                    save(existingTransferRecord);
                    endpointService.saveAll(existingTransferRecord.getEndpoints());
                    return existingTransferRecord;
                }
            } else {
            throw new UserNotAuthorizedException("The user is not part of the scope.");
        }
            } else {
                throw new ObjectDoesNotExistsException("Scope: "+transferRecordDTO.getScope().getName() + " does not exist");
            }
        return null;
    }

    public boolean exists(String id) {
        return transferRecordRepository.existsById(id);
    }

    @Deprecated
    public boolean update(TransferRecord transferRecord, String token) throws Exception {
        if (exists(transferRecord.getId())) {
            List<Endpoint> endpoints = transferRecord.getEndpoints();
            // We need to update the credentials as well.
            for (Endpoint endpoint : endpoints) {
                credentialService.save(endpoint.getCredential(), token);
            }
            save(transferRecord);
            endpointService.saveAll(endpoints);
            return true;
        } else
            return false;
    }

    public void delete(String id, String token) throws Exception {
        // used externally
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        TransferRecord transferRecord = this.getRecordById(id).orElseThrow();
        Scope scope = transferRecord.getScope();
        if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
            transferRecordRepository.deleteById(id);
        } else {
            throw new UserNotAuthorizedException("User is not allowed to delete transfer for this scope");
        }
    }

    public void updateStatusById(Status status, String Id) {
        // Used internally
        if (transferRecordRepository.existsById(Id)){
            TransferRecord transferRecord = this.getRecordById(Id).orElse(null);
            assert transferRecord != null;
            transferRecord.setStatus(status);
            transferRecordRepository.save(transferRecord);
        }
    }

    public List<TransferRecord> findAllTransferRecordsOnCredentialUpdateAndStatusEnabled(String id){
        return transferRecordRepository.findAllTransferRecordsOnCredentialUpdateAndStatusEnabled(id);
    }

    public List<TransferRecord> findAllTransferRecordsOnCredential(String id){
        return transferRecordRepository.findAllTransferRecordsOnCredential(id);
    }

    public List<TransferRecord> findAllTransferRecordsOnHostUpdateAndStatusEnabled(String id){
        return transferRecordRepository.findAllTransferRecordsOnHostUpdateAndStatusEnabled(id);
    }

    public List<TransferRecord> findAllTransferRecordsOnHost(String id){
        return transferRecordRepository.findAllTransferRecordsOnHost(id);
    }

    public void removePrivateKeyFileIfNeeded(String id) {
        TransferRecord transferRecord = transferRecordRepository.findById(id).orElseThrow();
        List<Credential> credentials = transferRecord.getEndpoints().stream().filter(endpoint ->
            endpoint.getCredential().getCredentialType() == CredentialType.SFTP &&  endpoint.getCredential().getPrivate_key() != null).map(Endpoint::getCredential).toList();

        for (Credential credential : credentials) {
            if (!credentialService.isCredentialActiveInTransfer(String.valueOf(credential.getId()))){
                // Remove the private key-file as it is no longer used.
                File privateKeyFile = new File("credential.getUsername()" + " - " + credential.getId());
                if (privateKeyFile.exists()) {
                    if (!privateKeyFile.delete()) {
                        throw new RuntimeException("Failed to delete the temporary private key file");
                    }
                }
            }
        }
    }
}
