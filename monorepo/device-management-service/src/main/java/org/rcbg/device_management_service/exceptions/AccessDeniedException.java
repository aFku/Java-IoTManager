package org.rcbg.device_management_service.exceptions;

import java.util.UUID;

public class AccessDeniedException extends RuntimeException {
    private final UUID userId;

    public AccessDeniedException(String message, UUID userId) {
        super(message);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
