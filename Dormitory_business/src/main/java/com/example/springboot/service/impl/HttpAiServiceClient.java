package com.example.springboot.service.impl;

import com.example.springboot.service.AiServiceClient;
import com.example.springboot.service.dto.AiServiceAnalyzeRequest;
import com.example.springboot.service.dto.AiServiceAnalyzeResponse;
import com.example.springboot.service.dto.AiRepairFeedbackRequest;
import com.example.springboot.service.dto.AiRepairMetricsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
        HttpHeaders headers = headers();
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

    @Override
    public void feedback(AiRepairFeedbackRequest request) {
        try {
            restTemplate.postForEntity(
                    endpoint("/v1/repair/feedback"), new HttpEntity<>(request, headers()), Map.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("AI反馈服务暂时不可用", e);
        }
    }

    @Override
    public AiRepairMetricsResponse metrics() {
        try {
            ResponseEntity<AiRepairMetricsResponse> response = restTemplate.exchange(
                    endpoint("/v1/repair/metrics"), HttpMethod.GET,
                    new HttpEntity<>(headers()), AiRepairMetricsResponse.class);
            if (response.getBody() == null) {
                throw new IllegalStateException("AI指标服务返回内容为空");
            }
            return response.getBody();
        } catch (RestClientException e) {
            throw new IllegalStateException("AI指标服务暂时不可用", e);
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (internalToken != null && !internalToken.trim().isEmpty()) {
            headers.set("X-Internal-Token", internalToken.trim());
        }
        return headers;
    }

    private String endpoint(String path) {
        String value = baseUrl == null ? "" : baseUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value + path;
    }
}
