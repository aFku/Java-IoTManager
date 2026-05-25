package org.rcbg.device_management_service.services;

import org.rcbg.device_management_service.models.dto.keycloak.TokenResponseDto;
import org.rcbg.device_management_service.models.dto.keycloak.UserEntryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.svc.client-id}")
    private String clientId;
    @Value("${keycloak.svc.client-secret}")
    private String clientSecret;
    @Value("${keycloak.realm}")
    private String realm;

    private WebClient keycloakWebClient;

    public KeycloakService(@Value("${keycloak.base-url}") String baseUrl,
                           WebClient.Builder builder) {
        this.keycloakWebClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    private Mono<String> getSvcAccessToken() {
        return keycloakWebClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "grant_type=client_credentials" +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret
                )
                .retrieve()
                .bodyToMono(TokenResponseDto.class)
                .map(response -> response.access_token);
    }

    public Mono<List<UserEntryDto>>getUsersList() {
        return getSvcAccessToken().flatMap(token ->
                    keycloakWebClient.get()
                            .uri("/admin/realms/{realm}/users", realm)
                            .headers(auth_header -> auth_header.setBearerAuth(token))
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<UserEntryDto>>() {})
                );
    }
}
