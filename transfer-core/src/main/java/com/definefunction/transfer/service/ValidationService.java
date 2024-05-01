package com.definefunction.transfer.service;

import com.definefunction.transfer.exception.ObjectDoesNotExistsException;
import com.definefunction.transfer.exception.TransferRecordNotValidException;
import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.DTO.CredentialDTO;
import com.definefunction.transfer.model.DTO.EndpointDTO;
import com.definefunction.transfer.model.DTO.TransferRecordDTO;
import com.definefunction.transfer.repository.HostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private final static String ROUTE_VALIDATION_REGEX = "^[a-zA-Z_-]+$";

    @Autowired
    RecordService recordService;

    @Autowired
    HostRepository hostRepository;

    @Autowired
    CredentialService credentialService;

    public boolean isRouteValid(TransferRecordDTO transferRecordDTO) {
        String routeId = transferRecordDTO.getId();
        Pattern pattern = Pattern.compile(ROUTE_VALIDATION_REGEX);
        return pattern.matcher(routeId).matches() && !recordService.exists(routeId) && isEndpointValid(transferRecordDTO.getEndpoints());
    }

    public boolean isEndpointValid(List<EndpointDTO> endpointDTOList) {
        for (EndpointDTO endpointDTO : endpointDTOList) {
            if (!isHostValid(endpointDTO.getHost().getId())){
                throw new TransferRecordNotValidException("The host in endpoint with path: " + endpointDTO.getPath() + " is not valid");
            }

            if(!isCredentialValid(endpointDTO.getCredentialDTO())) {
                throw new TransferRecordNotValidException("The credential in endpoint with path: " + endpointDTO.getPath() + " is not valid");
            }

        }
        return true;
    }

    public boolean isHostValid(long hostId) {
        return hostRepository.existsById(hostId);
    }

    public boolean isCredentialValid(CredentialDTO credentialDTO){
        String id = String.valueOf(credentialDTO != null ? credentialDTO.getId() : null);
        return id != null && credentialService.exists(id);
    }


}
