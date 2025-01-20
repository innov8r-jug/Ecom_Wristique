package com.pranchal.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class TwoFactorService {
    @Value("${miniorange.customer.key}")
    private String customerKey;

    @Value("${miniorange.api.key}")
    private String apiKey;

    private static final String CHALLENGE_API_URL = "https://login.xecurify.com/moas/api/auth/challenge";
    private static final String VALIDATE_API_URL = "https://login.xecurify.com/moas/api/auth/validate";

    public Map<String, Object> generateOTP(String email) throws Exception {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String authHash = generateAuthHash(timestamp);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("customerKey", customerKey);
        requestBody.put("email", email);
        requestBody.put("authType", "EMAIL");

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(CHALLENGE_API_URL);

        // Set headers
        post.setHeader("Customer-Key", customerKey);
        post.setHeader("Timestamp", timestamp);
        post.setHeader("Authorization", authHash);
        post.setHeader("Content-Type", "application/json");

        // Set body
        StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(requestBody));
        post.setEntity(entity);

        // Execute request
        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        return new ObjectMapper().readValue(responseBody, Map.class);
    }

    public Map<String, Object> validateOTP(String txId, String token) throws Exception {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String authHash = generateAuthHash(timestamp);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("txId", txId);
        requestBody.put("token", token);

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(VALIDATE_API_URL);

        // Set headers
        post.setHeader("Customer-Key", customerKey);
        post.setHeader("Timestamp", timestamp);
        post.setHeader("Authorization", authHash);
        post.setHeader("Content-Type", "application/json");

        // Set body
        StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(requestBody));
        post.setEntity(entity);

        // Execute request
        HttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());

        return new ObjectMapper().readValue(responseBody, Map.class);
    }

    private String generateAuthHash(String timestamp) {
        try {
            String data = customerKey + timestamp + apiKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating auth hash", e);
        }
    }
}
