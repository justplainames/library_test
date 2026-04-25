package com.assignment.book.config;

import com.assignment.book.domain.ApiKey;
import com.assignment.book.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class ApiFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiFilter(ApiKeyService apiKeyService){
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKeyValue = request.getHeader("X-API-KEY");

        if (apiKeyValue != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            ApiKey apiKey = apiKeyService.findByKey(apiKeyValue);

            if (apiKey != null) {
                var authorities = apiKey.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList();

                var authentication = new UsernamePasswordAuthenticationToken(
                        apiKey.getApiKey(),
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

}
