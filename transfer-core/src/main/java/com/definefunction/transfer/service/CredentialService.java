package com.definefunction.transfer.service;

import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.DTO.CredentialDTO;
import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.Scope;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.serialization.PropertySerializer;
import com.definefunction.transfer.model.views.View;
import com.definefunction.transfer.repository.CredentialRepository;
import com.definefunction.transfer.utilities.Utilities;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CredentialService {

    @Autowired
    CredentialRepository credentialRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ScopeService scopeService;

    @Autowired
    CamelService camelService;

    @Autowired
    RecordService recordService;

    @Autowired
    private PropertySerializer propertySerializer;

    @Deprecated
    public Credential save(Credential credential, String token) throws Exception {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        Scope scope = scopeService.FindAllById(credential.getScope().getId());
        if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {

            return credentialRepository.save(credential);
        } else {
            throw new Exception("User is not authorized to save a credential for this scope.");
        }
    }

    public Credential save(CredentialDTO credential, String token) throws Exception {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        Scope scope = scopeService.FindAllById(credential.getScope().getId());
        if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
            boolean exists = findById(String.valueOf(credential.getId())) != null;
            Credential newOrExistingCredential = exists ? findById(String.valueOf(credential.getId())) : new Credential();
            newOrExistingCredential.setUsername(updateField(newOrExistingCredential.getUsername(), credential.getUsername(), false));
            newOrExistingCredential.setPassword(updateField(newOrExistingCredential.getPassword(), credential.getPassword(), true));
            newOrExistingCredential.setPublic_key(updateField(newOrExistingCredential.getPublic_key(), credential.getPublic_key(), false));
            newOrExistingCredential.setPrivate_key(updateField(newOrExistingCredential.getPrivate_key(), credential.getPrivate_key(), true));
            newOrExistingCredential.setKey_phrase(updateField(newOrExistingCredential.getKey_phrase(), credential.getKey_phrase(), true));
            newOrExistingCredential.setClient_id(updateField(newOrExistingCredential.getClient_id(), credential.getClient_id(), false));
            newOrExistingCredential.setClient_secret(updateField(newOrExistingCredential.getClient_secret(), credential.getClient_secret(), true));
            newOrExistingCredential.setTenant_id(updateField(newOrExistingCredential.getTenant_id(), credential.getTenant_id(), false));
            newOrExistingCredential.settoken(updateField(newOrExistingCredential.gettoken(), credential.getToken(), true));
            newOrExistingCredential.setScope(scope);
            newOrExistingCredential.setCredentialType(credential.getCredentialType());
            newOrExistingCredential.setLast_updated_at(ZonedDateTime.now());
            if (exists) {
                List<TransferRecord> transferRecords = recordService.findAllTransferRecordsOnCredential(String.valueOf(credential.getId()));
                transferRecords.forEach(transferRecord -> {
                    transferRecord.setVersion(transferRecord.getVersion()+1);
                    recordService.save(transferRecord);
                });
            }
            return credentialRepository.save(newOrExistingCredential);
        } else {
            throw new Exception("User is not authorized to save a credential for this scope.");
        }
    }

    public String updateField (String input, String newValue, boolean hasEncryption) throws Exception {
        // Check if the new value is null
        if (newValue != null) {
            if (input != null) {
                // Both input and newValue are not null
                if (!hasEncryption) {
                    // Encryption is false, values do not need to be masked or encrypted.
                    return input.equals(newValue) ? input : newValue;
                } else {
                    // Encryption is applied. If the field has the serialization value, input can stay as is. Else it needs to be encrypted.
                    return propertySerializer.hasSerialization(newValue) ? input : Utilities.encrypt(newValue);
                }
            } else {
                // Input is null, the existing value is null and was not set.
                // New value is not null, so needs to be set on input.
                // If the field is flagged as encrypted, the new value needs to be encrypted as well.
                return hasEncryption ? Utilities.encrypt(newValue) : newValue;
            }
        } else {
            return input;
        }
    }

    public Credential findById(String id) {
       Optional<Credential> credentialOptional = credentialRepository.findById(id);
        return credentialOptional.orElse(null);
    }

    public List<Credential> findByScope(Scope scope) {
        return credentialRepository.findCredentialsByScopeIs(scope);
    }

    public List<Credential> getAll() {
        return credentialRepository.findAll();
    }

    public void delete(String credentialId, String token) throws Exception {
        Principal principal = authenticationService.retrievePrincipalFromToken(token);
        Scope scope = credentialRepository.getReferenceById(credentialId).getScope();
        if (scopeService.whatIsTheRoleOfThePrincipalInTheScope(principal, scope) != null) {
            credentialRepository.deleteById(credentialId);
        } else {
            throw new Exception("Not allowed to delete this credential for this scope.");
        }
    }

    public boolean exists(String id) {
        return credentialRepository.existsById(id);
    }

    public boolean isCredentialActiveInTransfer(String id) {
        return !credentialRepository.findCredentialInActiveRoutes(id).isEmpty();

    }
}
