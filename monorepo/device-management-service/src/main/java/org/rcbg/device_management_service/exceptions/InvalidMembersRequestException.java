package org.rcbg.device_management_service.exceptions;

import java.util.UUID;

public class InvalidMembersRequestException extends RuntimeException {
    private final UUID userId;

    public InvalidMembersRequestException(String message, UUID userId) {
        super(message);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
