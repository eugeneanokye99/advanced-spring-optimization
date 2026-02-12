package com.shopjoy.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * The enum Address type.
 */
@Getter
public enum AddressType {
    /**
     * Shipping address type.
     */
    SHIPPING("Shipping"),
    /**
     * Billing address type.
     */
    BILLING("Billing"),
    /**
     * Home address label.
     */
    HOME("Home"),
    /**
     * Work address label.
     */
    WORK("Work"),
    /**
     * Other address label.
     */
    OTHER("Other");

    private final String displayName;

    AddressType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getJsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static AddressType fromString(String value) {
        if (value == null)
            return null;
        String v = value.trim();
        try {
            return AddressType.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (AddressType t : values()) {
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
