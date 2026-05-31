package org.rcbg.device_management_service.integration_tests;

import org.junit.jupiter.api.Test;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.models.dto.errors.StandardProblemDetail;
import org.rcbg.device_management_service.models.dto.home_access.MembersPostResponseDto;
import org.rcbg.device_management_service.models.dto.homes.ResponseHomeDto;
import org.rcbg.device_management_service.models.entities.Home;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HomesControllerIT extends AbstractIntegrationTest {

    @Test
    void getHomes_success() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "My home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.get()
                .uri("/api/v1/homes")
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseHomeDto.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getHomeId()).isEqualTo(home.getHomeId()));
    }

    @Test
    void getHomes_noJwt() {
        webTestClient.get()
                .uri("/api/v1/homes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getHomes_noPermissions() {
        webTestClient.get()
                .uri("/api/v1/homes")
                .headers(h -> h.setBearerAuth(token("NO_PERMISSIONS")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createHome_success() {
        webTestClient.post()
                .uri("/api/v1/homes")
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validHomeBody("Created home"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ResponseHomeDto.class)
                .value(dto -> assertThat(dto.getName()).isEqualTo("Created home"));
    }

    @Test
    void createHome_invalidBody() {
        webTestClient.post()
                .uri("/api/v1/homes")
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "aa"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(StandardProblemDetail.class)
                .value(p -> assertThat(p.getType()).isEqualTo("validation-error"));
    }

    @Test
    void createHome_lowPermissions() {
        webTestClient.post()
                .uri("/api/v1/homes")
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validHomeBody("Denied"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getHome_success() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Own home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseHomeDto.class)
                .value(dto -> assertThat(dto.getHomeId()).isEqualTo(home.getHomeId()));
    }

    @Test
    void getHome_resourceDoesNotExistWhenNoAccess() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Private home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(StandardProblemDetail.class)
                .value(p -> assertThat(p.getType()).isEqualTo("not-found"));
    }

    @Test
    void getHome_caching() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Cache home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseHomeDto.class)
                .value(dto -> assertThat(dto.getName()).isEqualTo("Cache home"));

        home.setName("Changed in DB");
        homeRepository.saveAndFlush(home);

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseHomeDto.class)
                .value(dto -> assertThat(dto.getName()).isEqualTo("Cache home"));
    }

    @Test
    void patchHome_success() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Old name");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validHomeBody("New name"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseHomeDto.class)
                .value(dto -> assertThat(dto.getName()).isEqualTo("New name"));
    }

    @Test
    void patchHome_lowPermissions() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Old name");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validHomeBody("New name"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void patchHome_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Old name");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validHomeBody("New name"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteHome_success() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Delete me");

        webTestClient.delete()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteHome_lowPermissions() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Delete me");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.delete()
                .uri("/api/v1/homes/{homeId}", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getMembers_success() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Members");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/members", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.members").isArray()
                .jsonPath("$.members.length()").isEqualTo(2);
    }

    @Test
    void updateMembers_success_addAndDelete() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Members");
        UUID viewerId = userId("READ_ALL_ONLY");
        UUID adminId = userId("SYSTEM_ADMIN");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/members", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "add", Map.of(adminId.toString(), "VIEWER"),
                        "delete", List.of(viewerId.toString())
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(MembersPostResponseDto.class)
                .value(dto -> {
                    assertThat(dto.getAdded()).hasSize(1);
                    assertThat(dto.getRemoved()).containsExactly(viewerId);
                });
    }

    @Test
    void updateMembers_invalidBody_collisionBetweenAddAndDelete() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Members");
        UUID id = userId("READ_ALL_ONLY");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/members", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "add", Map.of(id.toString(), "VIEWER"),
                        "delete", List.of(id.toString())
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(StandardProblemDetail.class)
                .value(p -> assertThat(p.getType()).isEqualTo("bad-request"));
    }

    @Test
    void updateMembers_noPermissions() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Members");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/members", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("add", Map.of(), "delete", List.of()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateMembers_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("HOME_READ_WRITE", "Members");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/members", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("add", Map.of(), "delete", List.of()))
                .exchange()
                .expectStatus().isNotFound();
    }
}
