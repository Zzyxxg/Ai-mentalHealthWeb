package com.example.mentalhealth.enums;

public enum AppointmentStatus {

    CREATED(1),
    CANCELED(2),
    CONFIRMED(3),
    COMPLETED(4);

    private final int code;

    AppointmentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AppointmentStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppointmentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown appointment status code: " + code);
    }
}
