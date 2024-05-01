package com.definefunction.transfer.parsers;

import com.definefunction.transfer.exception.TransferRecordNotValidException;
import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.pojo.CredentialType;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import com.definefunction.transfer.utilities.Utilities;
import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static java.util.Map.entry;

@Service
public class SFTPParser extends AbstractParser {
    private final static String SFTP = "sftp://";

    private final Map<String, String> DEFAULT_SFTP_PARAMETERS = Map.ofEntries(
            entry("throwExceptionOnConnectFailed", "true"),
            entry("streamDownload", "true"),
            entry("stepwise", "false")
    );

    @Override
    public String constructUrl(Endpoint endpoint, CamelContext camelContext) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SFTP);
        stringBuilder.append(constructCredential(endpoint.getCredential()));
        stringBuilder.append(endpoint.getHost().getHostname());
        stringBuilder.append(addPort(endpoint.getHost().getPort()));
        stringBuilder.append(constructPath(endpoint));
        String params = constructParameters(endpoint);
        String paramsWithSeperator = !params.isEmpty() ? SEPERATOR + params : params;
        stringBuilder.append(paramsWithSeperator);
        return stringBuilder.toString();
    }

    @Override
    public ParsedTransferEndpoint parseEndpoint(Endpoint endpoint, CamelContext camelContext) throws Exception {
        if (isServerReachable(endpoint.getHost().getHostname(), endpoint.getHost().getPort())) {
            ParsedTransferEndpoint parsedTransferEndpoint = new ParsedTransferEndpoint();
            parsedTransferEndpoint.setEndpoint(endpoint);
            parsedTransferEndpoint.setEndpointURL(constructUrl(endpoint, camelContext));
            return parsedTransferEndpoint;
        } else {
            throw new Exception("Failed to connect to server.");
        }
    }

    @Override
    public String constructParameters(Endpoint endpoint) throws Exception {
        String params = endpoint.getParameter(); // Get the endpoint parameters.
        String parametersWithDefaultAndCredentials = generateEndpointCredentialAndDefaultParameters(params, endpoint.getCredential(), endpoint.getDirection());
        Map<String, String> paramMap = returnParamMap(parametersWithDefaultAndCredentials); // Create a parameter map of the url
        Map<String, String> paramMapWithComponentParameters = addComponentParameters(DEFAULT_SFTP_PARAMETERS, paramMap, endpoint.getDirection());
        String parameters = toURL(paramMapWithComponentParameters);
        return parameters;
    }

    @Override
    public String constructCredential(Credential credential) {
        if (credential != null) {
            return credential.getUsername() + "@";
        } else {
            return "";
        }
    }

    @Override
    public String constructPath(Endpoint endpoint) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        String path = endpoint.getPath();
        if (path != null) {
            if (!path.isEmpty()) {
                stringBuilder.append("/");
                stringBuilder.append(path);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public String generateEndpointCredentialParameters(Credential credential) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        if (credential != null && credential.getCredentialType() == CredentialType.SFTP) {
            // First check if the private key is not null.
            if (credential.getPrivate_key() != null) {
                // TODO the private key file needs to be bound to a route so it can be created and removed whenever the route starts and ends.
                String id = credential.getUsername() + " - " + credential.getId();
                File privateKeyFile = new File(id);
                FileUtils.writeStringToFile(privateKeyFile, Utilities.decrypt(credential.getPrivate_key()), StandardCharsets.UTF_8);
                parameters.put("privateKeyFile", privateKeyFile.getAbsolutePath());
                if (credential.getKey_phrase() != null) {
                    parameters.put("privateKeyPassphrase", Utilities.decrypt(credential.getKey_phrase()));
                }
                return toURL(parameters);
            }
            if (credential.getPassword() != null){
                parameters.put("password", Utilities.decrypt(credential.getPassword()));
                return toURL(parameters);
            } else {
                throw new TransferRecordNotValidException("The supplied credential does not contain a password or a private key to make a connection to an SFTP host.");
            }
        } else {
            return "";
        }
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
}
