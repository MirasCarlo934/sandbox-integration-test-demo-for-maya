package com.example.integrationtestdemoformaya.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointProperties {
    private String scheme;
    private String host;
    private String resourcePath;
    private long timeout;

    public String toUrl() {
        return String.format("%s://%s/%s", scheme, host, resourcePath);
    }
}
