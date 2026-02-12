package com.shopjoy.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * The enum User type.
 */
@Getter
public enum UserType {
    /**
     * Customer user type.
     */
    CUSTOMER("Customer"),
    /**
     * Admin user type.
     */
    ADMIN("Admin");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getJsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static UserType fromString(String value) {
        if (value == null)
            return null;
        String v = value.trim();
        try {
            return UserType.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (UserType t : values()) {
                if (t.displayName.equalsIgnoreCase(v) || t.name().equalsIgnoreCase(v))
                    return t;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
