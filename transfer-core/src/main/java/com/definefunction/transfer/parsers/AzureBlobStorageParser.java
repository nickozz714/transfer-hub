package com.definefunction.transfer.parsers;

import com.azure.core.credential.AzureSasCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import com.definefunction.transfer.utilities.Utilities;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.util.Map.entry;

@Service
public class AzureBlobStorageParser extends AbstractParser {
    private final static String BLOB = "azure-storage-blob:";

    Map<String, String> PARAMETER_PARSE_MAPPER = Map.ofEntries(
            entry("antInclude", "prefix"),
            entry("include", "regex")
    );

    Map<String, String> PARAMETER_OPERATION_MAPPER = Map.ofEntries(
      entry("delete","operation=deleteBlob"),
      entry("move", "operation=uploadBlockBlob"));
    // Not using path here and username is accountName.


    @Override
    public ParsedTransferEndpoint parseEndpoint(Endpoint endpoint, CamelContext camelContext) throws Exception {
        ParsedTransferEndpoint parsedTransferEndpoint = new ParsedTransferEndpoint();
        parsedTransferEndpoint.setEndpoint(endpoint);
        parsedTransferEndpoint.setEndpointURL(constructUrl(endpoint, camelContext));
        return parsedTransferEndpoint;
    }
    @Override
    public String constructUrl(Endpoint endpoint, CamelContext camelContext) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BLOB);
        stringBuilder.append(endpoint.getHost().getHostname()); // Add the storage account name
        stringBuilder.append(constructPath(endpoint)); // Add the containers that are in the path.
        createAzureCredential(endpoint, camelContext);
        containerExistsCreate(getBlobServiceClient(endpoint, camelContext), endpoint.getPath());
        String params = constructParameters(endpoint);
        String paramsWithSeperator = !params.isEmpty() ? SEPERATOR + params : params;
        stringBuilder.append(paramsWithSeperator); // Parameters are created, included the default parameters.
        return stringBuilder.toString();
    }
    @Override
    public String constructParameters(Endpoint endpoint) throws UnsupportedEncodingException {
        String params = endpoint.getParameter();

        // If the direction of the endpoint is to a destination, an operation needs to be defined and added as component param.
        // A new param map is created with the operations.
        Map<String, String> operationalParameters = defineProducerBlobEndpointOperation(endpoint.getDirection(),returnParamMap(params));

        // Parse endpoint specific parameters to be used in the endpoint url like filter.
        Map<String, String> endpointSpecificParameters = parseEndpointSpecificParameters(returnParamMap(params));

        // Combine the operational And Endpoint parameters.
        Map<String, String> operationalAndEndpointSpecificParameters = addComponentParameters(operationalParameters, endpointSpecificParameters, endpoint.getDirection());

        // Add the default parameters and parse to string
        String parameters = toURL(addDefaultParameters(operationalAndEndpointSpecificParameters));

        return parameters;
    }

    /**
     * Is needed to define what operation needs to be done, based on the parameters.
     * @param direction
     * @param parameter
     * @return
     */
    private Map<String, String> defineProducerBlobEndpointOperation(Direction direction, Map<String, String> parameter) {
        Map<String, String> defaultParametersBlob = new HashMap<>();
        if (direction == Direction.TO) { // Make sure that the endpoint is a producing endpoint.
            // Endpoint is a TO endpoint
            if (!parameter.containsKey("operation")) { // Does it have any operation parameter already, as only one is allowed.
                defaultParametersBlob.put("blobName", "blob");
                defaultParametersBlob.put("operation", "uploadBlockBlob");
            }
            else if (!parameter.containsKey("blobName")) {  // There already is an operation, how about the blobName?
                defaultParametersBlob.put("blobName", "blob");
                defaultParametersBlob.put("operation", parameter.get("operation"));
            }
        }
        return defaultParametersBlob;
    }
    private Map<String, String> parseEndpointSpecificParameters(Map<String, String> parameters) {
        Map<String, String> parsedParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if(PARAMETER_PARSE_MAPPER.containsKey(entry.getKey())) {
                parsedParameters.put(PARAMETER_PARSE_MAPPER.get(entry.getKey()), entry.getValue());
            }
            if (DEFAULT_PARAMETERS.containsKey(entry.getKey())) {
                parsedParameters.put(entry.getKey(), entry.getValue());
            }
        }
        return parsedParameters;
    }
    public String generateOperationalURL(Endpoint endpoint) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BLOB);
        stringBuilder.append(endpoint.getHost().getHostname()); // Add the storage account name
        stringBuilder.append(constructPath(endpoint)); // Add the containers that are in the path.
        String params = constructOperationalParameters(endpoint);
        String paramsWithSeperator = !params.isEmpty() ? SEPERATOR + params : params;
        stringBuilder.append(paramsWithSeperator);
        return stringBuilder.toString();
    }

    private String constructOperationalParameters(Endpoint endpoint) throws UnsupportedEncodingException {
        String params = endpoint.getParameter();
        String parameters = toURL(addDefaultParameters(returnParamMap(params)));
        String credentialParameters = generateEndpointCredentialParameters(endpoint.getCredential());
        return parameters == null || parameters.isEmpty() ?
                credentialParameters : !Objects.equals(credentialParameters, "") ? parameters + COMBINER + credentialParameters : parameters;
    }

    public List<Endpoint> mapParametersToOperationEndpoints(Map<String, String> parameters, Endpoint endpoint, CamelContext camelContext) throws Exception {
        List<Endpoint> parsedTransferEndpointList = new ArrayList<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if(PARAMETER_OPERATION_MAPPER.containsKey(entry.getKey())) {
                BlobServiceClient blobServiceClient = getBlobServiceClient(endpoint, camelContext);
                if (containerExistsCreate(blobServiceClient, endpoint.getPath())) {
                    // Pending on the move parameter, we need to construct a new path (containers) to where the files should be moved.
                    switch (entry.getKey()) {
                        case "delete" ->
                            parsedTransferEndpointList.add(constructDeleteEndpoint(endpoint));
                        case "move" ->
                            parsedTransferEndpointList.addAll(constructMoveEndpoints(endpoint, entry.getValue(), blobServiceClient));
                    }
                } else {
                    throw new Exception("Container does not exist or cannot be initialized");
                }
            }
        }
        return parsedTransferEndpointList;
    }

    private List<Endpoint> constructMoveEndpoints(Endpoint endpoint, String moveTo, BlobServiceClient blobServiceClient) throws Exception {
        String container = moveTo.contains("/") ? moveTo.split("/")[0] : moveTo;
        if (containerExistsCreate(blobServiceClient, container)) {
            List<Endpoint> endpoints = new ArrayList<>();
            Endpoint newEndpoint = new Endpoint();
            newEndpoint.setProtocol(endpoint.getProtocol());
            newEndpoint.setHost(endpoint.getHost());
            newEndpoint.setPath(container); // This is the container to move to and should not include anything else.
            Map<String, String> moveOperationParameters = Map.ofEntries(
                    entry("operation", "uploadBlockBlob"),
                    entry("blobName", moveTo) // basically obsolete, but mandatory when creating the route.
            );
            newEndpoint.setParameter(toURL(moveOperationParameters));
            newEndpoint.setCredential(endpoint.getCredential());
            newEndpoint.setDirection(Direction.TO);
            endpoints.add(newEndpoint);
            endpoints.add(constructDeleteEndpoint(endpoint));
            return endpoints;
        } else {
            throw new Exception("Container: " + container + " could not be found or created.");
        }
    }

    private Endpoint constructDeleteEndpoint(Endpoint endpoint){
        Endpoint newEndpoint = new Endpoint();
        newEndpoint.setProtocol(endpoint.getProtocol());
        newEndpoint.setHost(endpoint.getHost());
        newEndpoint.setPath(endpoint.getPath());
        Map<String, String> deleteOperationParameters = Map.ofEntries(
                entry("operation", "deleteBlob"),
                entry("blobName", "blob")
        );
        newEndpoint.setParameter(toURL(deleteOperationParameters));
        newEndpoint.setCredential(endpoint.getCredential());
        newEndpoint.setDirection(Direction.TO);
        return newEndpoint;
    }

    private boolean AzureCredentialExists(Credential credential, CamelContext camelContext) {
        // If the return value is null, the bean does not exist in the registry.
        return camelContext.getRegistry().lookupByName(getCredentialRegistryValue(credential)) != null;
    }

    private void createAzureCredential(Endpoint endpoint, CamelContext camelContext) throws Exception {
        Credential credential = endpoint.getCredential();
        if (credential != null) {
            boolean exists = AzureCredentialExists(credential, camelContext);
            BlobServiceClient blobServiceClient = generateBlobServiceClient(endpoint);
            if (exists) {
                camelContext.getRegistry().unbind(getCredentialRegistryValue(credential));
            }
            camelContext.getRegistry().bind(getCredentialRegistryValue(credential), blobServiceClient);
        } else {
            throw new Exception("Credential cannot be empty when initializing an Azure-Blob Endpoint");
        }
    }

    public String getCredentialRegistryValue(Credential credential) {
        return String.valueOf(credential.getId());
    }

    private BlobServiceClient generateBlobServiceClient(Endpoint endpoint) throws Exception {
        String uri = String.format("https://%s.blob.core.windows.net", endpoint.getHost().getHostname());
        return switch (endpoint.getCredential().getCredentialType()) {
            case OAUTH -> new BlobServiceClientBuilder()
                    .endpoint(uri)
                    .credential(createClientSecretCredential(endpoint.getCredential()))
                    .buildClient();
            case ACCESS_KEY -> new BlobServiceClientBuilder()
                        .endpoint(uri)
                        .credential(createSharedKeyCredential(endpoint))
                        .buildClient();
            case SAS_TOKEN -> new BlobServiceClientBuilder()
                        .endpoint(uri)
                        .credential(createAzureSasCredential(endpoint.getCredential()))
                        .buildClient();
            default -> throw new Exception("Credential is not supported for use in an Azure connector.");
        };
    }
    private AzureSasCredential createAzureSasCredential(Credential credential) throws Exception {
        return new AzureSasCredential(Utilities.decrypt(credential.gettoken()));
    }
    private StorageSharedKeyCredential createSharedKeyCredential(Endpoint endpoint) throws Exception {
        return new StorageSharedKeyCredential(endpoint.getHost().getHostname(), Utilities.decrypt(endpoint.getCredential().gettoken()));
    }
    private ClientSecretCredential createClientSecretCredential(Credential credential) throws Exception {
        ClientSecretCredentialBuilder clientSecretCredentials = new ClientSecretCredentialBuilder();
        return clientSecretCredentials
                .clientId(credential.getClient_id())           // replace with your Azure AD application client ID
                .clientSecret(Utilities.decrypt(credential.getClient_secret()))   // replace with your Azure AD application client secret
                .tenantId(credential.getTenant_id())           // replace with your Azure AD tenant ID
                .authorityHost("https://login.microsoftonline.com")
                .build();
    }

    @Override
    public String constructPath(Endpoint endpoint) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String path = endpoint.getPath();
        if (path != null) {
            if (!path.isEmpty()) {
                stringBuilder.append("/");
                stringBuilder.append(path);
            } else {
                throw new Exception("Endpoint path is left empty and thus does not contain a container. This is not allowed.");
            }
        } else {
            throw new Exception("Endpoint does not contain any containers.");
        }
        return stringBuilder.toString();
    }

    @Override
    public String constructCredential(Credential credential) {
        return null;
    }

    @Override
    public String generateEndpointCredentialParameters(Credential credential) {
        return "serviceClient=#"+getCredentialRegistryValue(credential);
    }

    @Override
    public Map<String, String> addComponentParameters(Map<String, String> defaultParameters, Map<String, String> parameters, Direction direction) {
        for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
            if (!parameters.containsKey(entry.getKey())) {
                parameters.put(entry.getKey(), entry.getValue());
            }
        }
        return parameters;
    }

    public BlobServiceClient getBlobServiceClient(Endpoint endpoint, CamelContext camelContext) {
        return (BlobServiceClient) camelContext.getRegistry().lookupByName(String.valueOf(endpoint.getCredential().getId()));
    }

    public boolean containerExistsCreate(BlobServiceClient blobServiceClient, String container) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(container);
        try {
            if (!containerClient.exists()) {
                containerClient.create();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
