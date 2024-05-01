package com.definefunction.transfer.parsers;

import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class FileParser extends AbstractParser {
    private final static String FILE = "file://";

    // TODO maybe include FILE_DEFAULT_PARAMETERS IF ANY.

    @Override
    public String constructUrl(Endpoint endpoint, CamelContext camelContext) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FILE);
        stringBuilder.append(constructPath(endpoint));
        String parameters = constructParameters(endpoint);
        stringBuilder.append(parameters.isEmpty() ? parameters : SEPERATOR + parameters);
        return stringBuilder.toString();
    }

    @Override
    public String constructParameters(Endpoint endpoint) throws Exception {
        String params = endpoint.getParameter();
        String parametersWithDefaultAndCredentials = generateEndpointCredentialAndDefaultParameters(params, endpoint.getCredential(), endpoint.getDirection());
        return parametersWithDefaultAndCredentials;
    }

    @Override
    public String constructCredential(Credential credential) {
        // There are no credentials to be added to the URL. This can be empty.
        return null;
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
                throw new Exception("Endpoint path is left empty and thus does not contain a directory. This is not allowed.");
            }
        } else {
            throw new Exception("Endpoint does not contain any path to the directory.");
        }
        return stringBuilder.toString();
    }

    public ParsedTransferEndpoint parseEndpoint(Endpoint endpoint, CamelContext camelContext) throws Exception {
        ParsedTransferEndpoint parsedTransferEndpoint = new ParsedTransferEndpoint();
        parsedTransferEndpoint.setEndpoint(endpoint);
        parsedTransferEndpoint.setEndpointURL(constructUrl(endpoint, camelContext));
        return parsedTransferEndpoint;
    }

    @Override
    public String generateEndpointCredentialParameters(Credential credential) {
        return null;
    }

    @Override
    public Map<String, String> addComponentParameters(Map<String, String> defaultParameters, Map<String, String> parameters, Direction direction) {
        return null;
    }
}
