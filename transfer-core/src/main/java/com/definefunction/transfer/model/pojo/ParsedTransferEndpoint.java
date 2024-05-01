package com.definefunction.transfer.model.pojo;

import com.definefunction.transfer.model.Endpoint;

public class ParsedTransferEndpoint {

    public ParsedTransferEndpoint() {
    }

    public ParsedTransferEndpoint(String endpointURL, Endpoint endpoint) {
        EndpointURL = endpointURL;
        this.endpoint = endpoint;
    }

    private String EndpointURL;

    private Endpoint endpoint;

    private String AzurePostProcessingOperation;

    public String getEndpointURL() {
        return EndpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        EndpointURL = endpointURL;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getAzurePostProcessingOperation() {
        return AzurePostProcessingOperation;
    }

    public void setAzurePostProcessingOperation(String azurePostProcessingOperation) {
        AzurePostProcessingOperation = azurePostProcessingOperation;
    }
}
