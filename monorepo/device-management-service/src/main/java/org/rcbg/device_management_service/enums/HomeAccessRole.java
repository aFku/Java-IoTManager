package org.rcbg.device_management_service.enums;

public enum HomeAccessRole {
    NONE(0),
    VIEWER(1),
    MANAGER(2);

    private final int code;

    HomeAccessRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static HomeAccessRole fromCode(int code) {
        for (HomeAccessRole role : values()) {
            if (role.code == code) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}
