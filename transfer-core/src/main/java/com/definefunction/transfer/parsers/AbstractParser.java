package com.definefunction.transfer.parsers;

import com.definefunction.transfer.model.Credential;
import com.definefunction.transfer.model.Endpoint;
import com.definefunction.transfer.model.pojo.Direction;
import com.definefunction.transfer.model.pojo.ParsedTransferEndpoint;
import org.apache.camel.CamelContext;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Map.entry;

@Service
public abstract class AbstractParser {

    public final static Map<String, String> DEFAULT_PARAMETERS = Map.ofEntries(
            entry("delay", "20000"),
            entry("bridgeErrorHandler", "true")
    );

    public final static String SEPERATOR = "?";

    public final static String COMBINER = "&";

    public final static String FORWARD_SLASH = "/";

    public final static String BACKWARD_SLASH = "\\";

    public abstract String constructCredential(Credential credential) throws Exception;

    public abstract String constructPath(Endpoint endpoint) throws Exception;

    public abstract ParsedTransferEndpoint parseEndpoint(Endpoint endpoint, CamelContext camelContext) throws Exception;

    public abstract String constructUrl(Endpoint endpoint, CamelContext camelContext) throws Exception;

    public abstract String constructParameters(Endpoint endpoint) throws Exception;

    protected String addPort(int port) {
        if(port != 0) {
            return ":" + port;
        } else {
            return "";
        }
    }

    protected String encode64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * Append parameter mappings to each other.
     * @param source
     * @param toAdd
     * @return
     */
    public Map<String, String> appendParameters(Map<String, String> source, Map<String, String> toAdd) {
        source.putAll(toAdd);
        return source;
    }

    /**
     * Return a parameter String with the Default non-component parameters.
     * @param url
     * @param direction
     * @return
     * @throws UnsupportedEncodingException
     */
    public String generateEndpointCredentialAndDefaultParameters(String url, Credential credential, Direction direction) throws Exception {
        Map<String, String> parameters = returnParamMap(url);
        String endpointAndDefaultParameters = toURL(addDefaultParameters(parameters));
        String credentialParameters = generateEndpointCredentialParameters(credential);
        return endpointAndDefaultParameters == null || endpointAndDefaultParameters.isEmpty() ?
                credentialParameters : !Objects.equals(credentialParameters, "") ? endpointAndDefaultParameters + "&" + credentialParameters : endpointAndDefaultParameters;
    }

    /**
     * Abstract method to generate the endpoint credentials if needed.
     * @param credential
     * @return
     */
    public abstract String generateEndpointCredentialParameters(Credential credential) throws Exception;


    /**
     * Method to convert a map of parameters to a String
     * @param urlString
     * @return
     * @throws UnsupportedEncodingException
     */
    public Map<String, String> returnParamMap(String urlString) throws UnsupportedEncodingException {
        Map<String, String> parameters = new HashMap<>();
        if(urlString != null) {
            String[] pairs = urlString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    parameters.put(key, value);
                }
            }
            return parameters;
        } else {
            return parameters;
        }
    }

    /**
     * Adds the DEFAULT_PARAMETERS map to the parameter input.
     * @param parameters parameter map that needs to default parameters to be added.
     * @return parameter map
     */
    public Map<String, String> addDefaultParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : DEFAULT_PARAMETERS.entrySet()) {
            if(!parameters.containsKey(entry.getKey())) {
                parameters.put(entry.getKey(), entry.getValue());
            }
        }
        return parameters;
    }

    /**
     * Method to add parameter specific components to a parameter map.
     * @param defaultParameters component specific parameters to be added to the map.
     * @param parameters parameter map that will receive the component specific parameters
     * @param direction if needed a direction to set rules.
     * @return a mapping of the new parameters with default component parameters.
     */
    public abstract Map<String, String> addComponentParameters(Map<String, String> defaultParameters, Map<String, String> parameters, Direction direction);

    /**
     * Converts a map of parameters to an url.
     * @param parameters map
     * @return a string of parameters to be added to the uri.
     */
    public String toURL(Map<String, String> parameters) {
        StringBuilder urlString = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlString.append(urlString.isEmpty() ? "" : "&");
            String pair = entry.getKey()+"="+entry.getValue();
            urlString.append(pair);
        }
        return urlString.toString();
    }

    /**
     * Checks if the server connecting to is reachable by Telnet.
     * @param host for the hostname
     * @param port for the port
     * @return boolean to determine whether connection is working.
     */
    public boolean isServerReachable(String host, int port) {
        try  {
            TelnetClient telnetClient = new TelnetClient();
            telnetClient.setConnectTimeout(5000);
            telnetClient.connect(host, port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
