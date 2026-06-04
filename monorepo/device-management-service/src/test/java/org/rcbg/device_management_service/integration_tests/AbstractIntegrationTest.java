package org.rcbg.device_management_service.integration_tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.rcbg.device_management_service.enums.DeviceType;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.rcbg.device_management_service.models.entities.HomeAccess;
import org.rcbg.device_management_service.repositories.DeviceRepository;
import org.rcbg.device_management_service.repositories.HomeAccessRepository;
import org.rcbg.device_management_service.repositories.HomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64.Decoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("device_management")
            .withUsername("test")
            .withPassword("test");

    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0.7")
            .withRealmImportFile("test-realm.json");

    static {
        postgres.start();
        redis.start();
        keycloak.start();
        System.out.println("ALL CONTAINERS STARTED SUCCESSFULLY");
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("POSTGRES_ADDR", postgres::getHost);
        registry.add("POSTGRES_PORT", () -> postgres.getMappedPort(5432));
        registry.add("POSTGRES_DB", postgres::getDatabaseName);
        registry.add("POSTGRES_USER", postgres::getUsername);
        registry.add("POSTGRES_PASSWORD", postgres::getPassword);

        registry.add("REDIS_ADDR", redis::getHost);
        registry.add("REDIS_PORT", () -> String.valueOf(redis.getMappedPort(6379)));

        registry.add("KEYCLOAK_ADDR", () -> keycloak.getHost() + ":" + keycloak.getFirstMappedPort());
        registry.add("KEYCLOAK_REALM", () -> "IoTManager");
        registry.add("KEYCLOAK_AUTH_CLIENT_ID", () -> "device-management-service");
        registry.add("KEYCLOAK_SVC_CLIENT_ID", () -> "device-management-service");
        registry.add("KEYCLOAK_SVC_CLIENT_SECRET", () -> "test-device-secret-456");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () ->
                "http://" + keycloak.getHost() + ":" + keycloak.getFirstMappedPort() + "/realms/IoTManager"
        );
        registry.add("LOG_LEVEL", () -> "WARN");
        registry.add("DEBUG_ENABLED", () -> "false");
    }

    @Autowired protected WebTestClient webTestClient;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected HomeRepository homeRepository;
    @Autowired protected DeviceRepository deviceRepository;
    @Autowired protected HomeAccessRepository homeAccessRepository;
    @Autowired protected CacheManager cacheManager;
    @Autowired protected RedisConnectionFactory redisConnectionFactory;

    protected final Map<String, UUID> userIds = new HashMap<>();
    protected final Map<String, String> tokens = new HashMap<>();

    @BeforeEach
    void beforeEach() {
        deviceRepository.deleteAll();
        homeAccessRepository.deleteAll();
        homeRepository.deleteAll();
        redisConnectionFactory
                .getConnection()
                .serverCommands()
                .flushAll();

        Optional.ofNullable(cacheManager.getCache("homes")).ifPresent(c -> c.clear());
        Optional.ofNullable(cacheManager.getCache("devices")).ifPresent(c -> c.clear());
        Optional.ofNullable(cacheManager.getCache("homes_members")).ifPresent(c -> c.clear());

        userIds.clear();
        tokens.clear();
    }

    protected String token(String username) {
        return tokens.computeIfAbsent(username, this::fetchAccessToken);
    }

    protected UUID userId(String username) {
        return userIds.computeIfAbsent(username, u -> decodeSub(token(u)));
    }

    protected Home createHomeOwnedBy(String username, String homeName) {
        UUID ownerId = userId(username);

        Home home = new Home();
        home.setName(homeName);
        home.setOrphaned(false);
        home = homeRepository.saveAndFlush(home);

        HomeAccess access = new HomeAccess();
        access.setHome(home);
        access.setUserId(ownerId);
        access.setRole(HomeAccessRole.MANAGER);
        homeAccessRepository.saveAndFlush(access);

        return home;
    }

    protected HomeAccess grantAccess(Home home, String username, HomeAccessRole role) {
        HomeAccess access = new HomeAccess();
        access.setHome(home);
        access.setUserId(userId(username));
        access.setRole(role);
        return homeAccessRepository.saveAndFlush(access);
    }

    protected Device createDevice(Home home, String name, DeviceType type, String secret) {
        Device device = new Device();
        device.setHome(home);
        device.setName(name);
        device.setDeviceType(type);
        device.setSecret(secret);
        return deviceRepository.saveAndFlush(device);
    }

    protected Map<String, Object> validHomeBody(String name) {
        return Map.of("name", name);
    }

    protected Map<String, Object> validDeviceBody(String name, String type) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("deviceType", type);
        return body;
    }

    protected void assertProblem(Object body, int status, String type) {
        JsonNode node = objectMapper.valueToTree(body);
        org.assertj.core.api.Assertions.assertThat(node.get("status").asInt()).isEqualTo(status);
        org.assertj.core.api.Assertions.assertThat(node.get("type").asText()).isEqualTo(type);
    }

    private String fetchAccessToken(String username) {
        String password = username;

        String response = webTestClient.post()
                .uri("http://" + keycloak.getHost() + ":" + keycloak.getFirstMappedPort()
                        + "/realms/IoTManager/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "grant_type=password" +
                                "&client_id=login-service" +
                                "&client_secret=test-login-secret-456" +
                                "&username=" + username +
                                "&password=" + password)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        try {
            return objectMapper.readTree(response).get("access_token").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot extract access token", e);
        }
    }

    private UUID decodeSub(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payload);
            return UUID.fromString(node.get("sub").asText());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot decode JWT subject", e);
        }
    }
}
