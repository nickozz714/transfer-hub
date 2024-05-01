package com.definefunction.transfer.utilities;

import com.definefunction.transfer.model.TransferRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Utilities {

    @Value("${encryption.secret.key}")
    private String encryptionKey;

    private static String ENCRYPTION_KEY;

    @Value("${encryption.secret.key}")
    public void setEncryptionKeyStatic(String encryptionKey) {
        ENCRYPTION_KEY = encryptionKey;
    }

    private static final String ALGORITHM = "AES";

    public final static List<String> DEFAULT_SANITIZE_PARAMETERS = List.of(
            new String[]{
                    "password",
                    "clientSecret",
                    "sharedKey",
                    "sdd",
                    "si",
                    "se",
                    "sig",
                    "sip",
                    "sp",
                    "spr",
                    "sr",
                    "ss",
                    "st",
                    "sr"
            });

    /**
     * Sanitize and clean urls to hide passwords or other sensitive data.
     *
     * @param url
     * @return
     */
    public static String sanitizeParametersInURL(String url) {
        if (!url.isEmpty()) {
            try {
                boolean paramsOnly = !url.contains("?");
                String parameters = paramsOnly ? url : url.split("\\?")[1];
                String[] pairs = parameters.split("&");
                Map<String, String> parameterMap = new HashMap<>();
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = DEFAULT_SANITIZE_PARAMETERS.contains(key) ? "*******" : URLDecoder.decode(keyValue[1], "UTF-8");
                        parameterMap.put(key, value);
                    }
                }
                return paramsOnly ? toString(parameterMap) : url.split("\\?")[0] + "?" + toString(parameterMap);
            } catch (Exception e) {
                return url;
            }
        }
        return url;
    }

    public static String toString(Map<String, String> parameters) {
        StringBuilder urlString = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            urlString.append(urlString.isEmpty() ? "" : "&");
            String pair = entry.getKey() + "=" + entry.getValue();
            urlString.append(pair);
        }
        return urlString.toString();
    }

    public static String encrypt(String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static String replaceLineEndings(String input) {
        // Count the number of line endings in the input
        long lineEndingCount = input.chars().filter(ch -> ch == '\n').count();

        // If there are more than 3 line endings, replace the line endings except the first and last
        if (lineEndingCount > 3) {
            String[] lines = input.trim().split("\n");

            // Replace line endings for lines except the first and last
            StringBuilder intermediateResultBuilder = new StringBuilder();
            for (int i = 1; i < lines.length - 1; i++) {
                intermediateResultBuilder.append(lines[i].replaceAll("\n", ""));
                //lines[i] = lines[i].replaceAll("\n", "");
            }

            // Reconstruct the string with modified lines
            StringBuilder resultBuilder = new StringBuilder();
            String beginMarker = lines[0];
            String endMarker = lines[lines.length -1];
            resultBuilder.append(beginMarker).append(" ").append(intermediateResultBuilder).append(" ").append(endMarker);

            return resultBuilder.toString();
        }

        return input;
    }
    public static String getRouteContextName(TransferRecord transferRecord) {
        return transferRecord.getId() + "-" + transferRecord.getVersion();
    }

    public static Integer getRouteVersion(String routeId) {
        return Integer.valueOf(routeId.substring(routeId.lastIndexOf("-")+1));
    }

    public static String getRouteName (String routeId) {
        return routeId.substring(0, routeId.lastIndexOf("-"));
    }
}
