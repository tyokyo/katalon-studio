package com.kms.katalon.activation.plugin.models;

import java.util.HashMap;
import java.util.Map;

public class KStoreApiKeyCredentials implements KStoreCredentials {

    private String apiKey;
    
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("apiKey", apiKey);
        return headers;
    }

}