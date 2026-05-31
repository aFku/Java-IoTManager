package org.rcbg.device_management_service.integration_tests;

import org.junit.jupiter.api.Test;
import org.rcbg.device_management_service.enums.DeviceType;
import org.rcbg.device_management_service.enums.HomeAccessRole;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseDeviceWithSecretDto;
import org.rcbg.device_management_service.models.dto.devices.ResponseSecretDto;
import org.rcbg.device_management_service.models.dto.errors.StandardProblemDetail;
import org.rcbg.device_management_service.models.entities.Device;
import org.rcbg.device_management_service.models.entities.Home;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DevicesControllerIT extends AbstractIntegrationTest {

    @Test
    void getDevices_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        createDevice(home, "Thermometer", DeviceType.SENSOR, "secret-1");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseDeviceDto.class)
                .hasSize(1);
    }

    @Test
    void getDevices_noJwt() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getDevices_noPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("NO_PERMISSIONS")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getDevices_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getDevices_caching() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        Device device = createDevice(home, "Cached sensor", DeviceType.SENSOR, "secret-1");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseDeviceDto.class)
                .hasSize(1);

        deviceRepository.deleteById(device.getDeviceId());
        deviceRepository.flush();

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseDeviceDto.class)
                .hasSize(1);
    }

    @Test
    void createDevice_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 1", "SENSOR"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ResponseDeviceWithSecretDto.class)
                .value(dto -> {
                    assertThat(dto.getName()).isEqualTo("Sensor 1");
                    assertThat(dto.getSecret()).isNotBlank();
                    assertThat(dto.getHomeId()).isEqualTo(home.getHomeId());
                });
    }

    @Test
    void createDevice_invalidBody() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "aa"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(StandardProblemDetail.class)
                .value(p -> assertThat(p.getType()).isEqualTo("validation-error"));
    }

    @Test
    void createDevice_noPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("NO_PERMISSIONS")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 1", "SENSOR"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createDevice_lowPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 1", "SENSOR"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createDevice_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices", home.getHomeId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 1", "SENSOR"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getDevice_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDeviceDto.class)
                .value(dto -> assertThat(dto.getDeviceId()).isEqualTo(device.getDeviceId()));
    }

    @Test
    void getDevice_resourceDoesNotExist_whenDeviceMissing() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), UUID.randomUUID())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getDevice_resourceDoesNotExist_whenNoHomeAccess() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("HOME_READ_WRITE")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void patchDevice_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 2", "SWITCH"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDeviceDto.class)
                .value(dto -> assertThat(dto.getName()).isEqualTo("Sensor 2"));
    }

    @Test
    void patchDevice_invalidBody() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "a"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(StandardProblemDetail.class)
                .value(p -> assertThat(p.getType()).isEqualTo("validation-error"));
    }

    @Test
    void patchDevice_lowPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 2", "SWITCH"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void patchDevice_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.patch()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), UUID.randomUUID())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validDeviceBody("Sensor 2", "SWITCH"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteDevice_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.delete()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteDevice_lowPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        grantAccess(home, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.delete()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteDevice_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.delete()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", home.getHomeId(), UUID.randomUUID())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void refreshSecret_success() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}/secret", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseSecretDto.class)
                .value(dto -> assertThat(dto.getSecret()).isNotBlank());
    }

    @Test
    void refreshSecret_noPermissions() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");
        Device device = createDevice(home, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}/secret", home.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("NO_PERMISSIONS")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void refreshSecret_resourceDoesNotExist() {
        Home home = createHomeOwnedBy("READ_WRITE_ALL", "Home");

        webTestClient.post()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}/secret", home.getHomeId(), UUID.randomUUID())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void moveDevice_success() {
        Home source = createHomeOwnedBy("READ_WRITE_ALL", "Source");
        Home target = createHomeOwnedBy("READ_WRITE_ALL", "Target");
        Device device = createDevice(source, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/homes/{homeId}/devices/{deviceId}/move")
                        .queryParam("target", target.getHomeId())
                        .build(source.getHomeId(), device.getDeviceId()))
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseDeviceDto.class)
                .value(dto -> assertThat(dto.getHomeId()).isEqualTo(target.getHomeId()));
    }

    @Test
    void moveDevice_invalidMissingQueryParam() {
        Home source = createHomeOwnedBy("READ_WRITE_ALL", "Source");
        Device device = createDevice(source, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.put()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}/move", source.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void moveDevice_lowPermissions() {
        Home source = createHomeOwnedBy("READ_WRITE_ALL", "Source");
        Home target = createHomeOwnedBy("READ_WRITE_ALL", "Target");
        grantAccess(source, "READ_ALL_ONLY", HomeAccessRole.VIEWER);
        Device device = createDevice(source, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/homes/{homeId}/devices/{deviceId}/move")
                        .queryParam("target", target.getHomeId())
                        .build(source.getHomeId(), device.getDeviceId()))
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void moveDevice_resourceDoesNotExist_whenDeviceMissing() {
        Home source = createHomeOwnedBy("READ_WRITE_ALL", "Source");
        Home target = createHomeOwnedBy("READ_WRITE_ALL", "Target");

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/homes/{homeId}/devices/{deviceId}/move")
                        .queryParam("target", target.getHomeId())
                        .build(source.getHomeId(), UUID.randomUUID()))
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void moveDevice_cachingOrphanPreventionBaseline() {
        Home source = createHomeOwnedBy("READ_WRITE_ALL", "Source");
        Home target = createHomeOwnedBy("READ_WRITE_ALL", "Target");
        Device device = createDevice(source, "Sensor 1", DeviceType.SENSOR, "secret-1");

        webTestClient.get()
                .uri("/api/v1/homes/{homeId}/devices/{deviceId}", source.getHomeId(), device.getDeviceId())
                .headers(h -> h.setBearerAuth(token("READ_ALL_ONLY")))
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/homes/{homeId}/devices/{deviceId}/move")
                        .queryParam("target", target.getHomeId())
                        .build(source.getHomeId(), device.getDeviceId()))
                .headers(h -> h.setBearerAuth(token("READ_WRITE_ALL")))
                .exchange()
                .expectStatus().isOk();
    }
}
