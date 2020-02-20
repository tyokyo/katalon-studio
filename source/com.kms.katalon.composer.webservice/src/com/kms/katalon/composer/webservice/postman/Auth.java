package com.kms.katalon.composer.webservice.postman;

import java.util.*;
import com.fasterxml.jackson.annotation.*;

public class Auth {
    private String type;
    private List<Oauth2> oauth2;
    private List<Basic> basic;
    private List<ApiKey> apikey;

    @JsonProperty("type")
    public String getType() { return type; }
    @JsonProperty("type")
    public void setType(String value) { this.type = value; }

    @JsonProperty("oauth2")
    public List<Oauth2> getOauth2() { return oauth2; }
    @JsonProperty("oauth2")
    public void setOauth2(List<Oauth2> value) { this.oauth2 = value; }

    public List<Basic> getBasic() {
        return basic;
    }
    public void setBasic(List<Basic> basic) {
        this.basic = basic;
    }
    public List<ApiKey> getApikey() {
        return apikey;
    }
    public void setApikey(List<ApiKey> apikey) {
        this.apikey = apikey;
    }
}