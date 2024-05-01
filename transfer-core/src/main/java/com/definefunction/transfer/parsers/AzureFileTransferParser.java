package com.definefunction.transfer.parsers;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import com.definefunction.transfer.utilities.Utilities;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Map.entry;

@Service
public class AzureFileTransferParser extends AbstractParser {

    private final static String AZURE_FILES = "azure-files://";

    private final Map<String, String> DEFAULT_AZURE_FILES_PARAMETERS = Map.ofEntries(
            entry("throwExceptionOnConnectFailed", "true"),
            entry("streamDownload", "true"),
            entry("stepwise", "false")
    );

    @Override
    public String constructCredential(Credential credential) throws Exception {
        // No need to check the credential here. Already done.
        // we can add the token directly in the url.
        return Utilities.decrypt(credential.gettoken());
    }

    @Override
    public String constructPath(Endpoint endpoint) throws Exception {
        // Path needs to contain a value
        String path = endpoint.getPath();
        if (!path.isEmpty() && endpoint.getCredential() != null) {
            ShareClient shareClient = returnShareClient(endpoint);
            if (shareClient.exists()) {
                // we need to check whether the share exists.
                return path;
            } else {
                throw new Exception("Share does not exist");
            }
        } else {
            throw new Exception("Path is empty or no credential has been provided.");
        }
    }

    @Override
    public ParsedTransferEndpoint parseEndpoint(Endpoint endpoint, CamelContext camelContext) throws Exception {
        ParsedTransferEndpoint parsedTransferEndpoint = new ParsedTransferEndpoint();
        parsedTransferEndpoint.setEndpoint(endpoint);
        parsedTransferEndpoint.setEndpointURL(constructUrl(endpoint,camelContext));
        return parsedTransferEndpoint;
    }

    @Override
    public String constructUrl(Endpoint endpoint, CamelContext camelContext) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(AZURE_FILES); //setting the protocol
        stringBuilder.append(endpoint.getHost().getHostname()); // Hostname is the account to use.
        stringBuilder.append(FORWARD_SLASH); //Seperate the hostname and the path with a forward slash.
        stringBuilder.append(constructPath(endpoint)); // adding the path.
        String params = constructParameters(endpoint);
        String paramsWithSeperator = !params.isEmpty() ? SEPERATOR + params : params;
        stringBuilder.append(paramsWithSeperator);
        return null;
    }

    @Override
    public String constructParameters(Endpoint endpoint) throws Exception {
        String params = endpoint.getParameter(); // Get the endpoint parameters.
        String parametersWithDefaultAndCredentials = generateEndpointCredentialAndDefaultParameters(params, endpoint.getCredential(), endpoint.getDirection());
        Map<String, String> paramMap = returnParamMap(parametersWithDefaultAndCredentials); // Create a parameter map of the url
        Map<String, String> paramMapWithComponentParameters = addComponentParameters(DEFAULT_AZURE_FILES_PARAMETERS, paramMap, endpoint.getDirection());
        String parameters = toURL(paramMapWithComponentParameters);
        return parameters;
    }

    @Override
    public String generateEndpointCredentialParameters(Credential credential) throws Exception {
        return constructCredential(credential);
    }

    @Override
    public Map<String, String> addComponentParameters(Map<String, String> defaultParameters, Map<String, String> parameters, Direction direction) {
        if (direction == Direction.FROM) {
            for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
                if (!parameters.containsKey(entry.getKey())) {
                    parameters.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return parameters;
    }

    public ShareClient returnShareClient(Endpoint endpoint) throws Exception {
        if (endpoint.getCredential().getCredentialType() == CredentialType.SAS_TOKEN) {
            return new ShareClientBuilder()
                    .endpoint("https://" + endpoint.getHost().getHostname() + ".file.core.windows.net")
                    .sasToken(Utilities.decrypt(endpoint.getCredential().gettoken()))
                    .shareName(endpoint.getPath().split(FORWARD_SLASH, 2)[0])
                    .buildClient();
        } else {
            throw new Exception("Credential is not of type SAS_TOKEN and cannot be used.");
        }
    }
}
