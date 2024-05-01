package com.definefunction.transfer.service;

import com.definefunction.transfer.model.Parameter;
import com.definefunction.transfer.repository.ParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParameterService {

    @Autowired
    ParameterRepository parameterRepository;

    public List<Parameter> save(Parameter parameter) {
        parameterRepository.save(parameter);
        return parameterRepository.findAll();
    }

    public void delete(Parameter parameter) {
        parameterRepository.delete(parameter);
    }

    public List<Parameter> getAllParameters() {
        return parameterRepository.findAll();
    }

    public boolean exists(Parameter parameter) {
        return parameterRepository.existsById(String.valueOf(parameter.getId()));
    }

    public boolean existsById(String id) {
        return parameterRepository.existsById(String.valueOf(id));
    }

    public Parameter findParameterById(String id) {
        return parameterRepository.getById(id);
    }
}
