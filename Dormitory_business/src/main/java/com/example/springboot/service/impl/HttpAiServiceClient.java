package com.example.springboot.service.impl;

import com.example.springboot.service.AiServiceClient;
import com.example.springboot.service.dto.AiServiceAnalyzeRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpAiServiceClient implements AiServiceClient {
    private final RestTemplate restTemplate;

    @Value("${ai.service.base-url:http://127.0.0.1:8000}")
    private String baseUrl;

    @Value("${ai.service.token:}")
    private String internalToken;

    public HttpAiServiceClient(
            @Value("${ai.service.connect-timeout-ms:2000}") int connectTimeout,
            @Value("${ai.service.read-timeout-ms:10000}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public AiServiceAnalyzeResponse analyze(AiServiceAnalyzeRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (internalToken != null && !internalToken.trim().isEmpty()) {
            headers.set("X-Internal-Token", internalToken.trim());
        }
        try {
            AiServiceAnalyzeResponse response = restTemplate.postForObject(
                    endpoint("/v1/repair/analyze"),
                    new HttpEntity<>(request, headers),
                    AiServiceAnalyzeResponse.class);
            if (response == null || response.getRequestId() == null) {
                throw new IllegalStateException("AI服务返回内容为空");
            }
            return response;
        } catch (RestClientException e) {
            throw new IllegalStateException("AI服务暂时不可用，请使用人工报修", e);
        }
    }

    private String endpoint(String path) {
        String value = baseUrl == null ? "" : baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value + path;
    }
}
