package com.assignment.book.service;

import com.assignment.book.domain.ApiKey;
import com.assignment.book.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository){
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKey findByKey(String apiKey){
        return apiKeyRepository.findByApiKey(apiKey).orElse(null);
    }

}
