package com.definefunction.transfer.service;

import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.TransferRecord;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import com.definefunction.transfer.parsers.AzureBlobStorageParser;
import com.definefunction.transfer.parsers.AzureFileTransferParser;
import com.definefunction.transfer.parsers.FileParser;
import com.definefunction.transfer.parsers.SFTPParser;
import org.apache.camel.CamelContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class URLBuilderService {

    @Autowired
    SFTPParser sftpParser;

    @Autowired
    FileParser fileParser;

    @Autowired
    AzureBlobStorageParser azureBlobStorageParser;

    @Autowired
    AzureFileTransferParser azureFileTransferParser;

    public List<ParsedTransferEndpoint> createParsedEndpoint(TransferRecord transferRecord, CamelContext camelContext) throws Exception {
        List<ParsedTransferEndpoint> parsedTransferEndpoints = new ArrayList<>();
        for (Endpoint endpoint : transferRecord.getEndpoints()) {
            switch (endpoint.getProtocol()) {
                case "sftp":
                    parsedTransferEndpoints.add(sftpParser.parseEndpoint(endpoint, camelContext));
                    break;
                case "file":
                    parsedTransferEndpoints.add(fileParser.parseEndpoint(endpoint, camelContext));
                    break;
                case "azure-storage-blob":
                    parsedTransferEndpoints.add(azureBlobStorageParser.parseEndpoint(endpoint, camelContext));
                    break;
                case "azure-files":
                    parsedTransferEndpoints.add(azureFileTransferParser.parseEndpoint(endpoint,camelContext));
                    break;
                default:
                    throw new Exception("Protocol not supported");
            };
        }
        return parsedTransferEndpoints;
    }

    public boolean isHostnameReachable(String hostname, int port){
        return sftpParser.isServerReachable(hostname, port);
    }

    public List<ParsedTransferEndpoint> createParsedAzureOperationalEndpoints(Endpoint endpoint, CamelContext camelContext) throws Exception {
        Map<String, String> parameters = azureBlobStorageParser.returnParamMap(endpoint.getParameter());
        List<ParsedTransferEndpoint> parsedTransferEndpoints = new ArrayList<>();
        List<Endpoint> operationalEndpoints = azureBlobStorageParser.mapParametersToOperationEndpoints(parameters, endpoint, camelContext);
        operationalEndpoints.forEach(operationalEndpoint -> {
            try {
                parsedTransferEndpoints.add(new ParsedTransferEndpoint(azureBlobStorageParser.generateOperationalURL(operationalEndpoint), operationalEndpoint));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return parsedTransferEndpoints;
    }

    public Map<String, String> returnMapping(String parameters) throws UnsupportedEncodingException {
        return sftpParser.returnParamMap(parameters);
    }
}
