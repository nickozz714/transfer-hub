package com.definefunction.transfer.service;

import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.repository.EndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EndpointService {
    @Autowired
    EndpointRepository endpointRepository;

    public void save(Endpoint endpoint) {
        endpointRepository.save(endpoint);
    }

    public void saveAll(List<Endpoint> endpoints) {endpointRepository.saveAll(endpoints);}

    public List<Endpoint> findAll() {return endpointRepository.findAll();}
}
