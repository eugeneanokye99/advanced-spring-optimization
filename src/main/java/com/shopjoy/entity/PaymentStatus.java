package com.shopjoy.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * The enum Payment status.
 */
@Getter
public enum PaymentStatus {
    /**
     * Unpaid payment status.
     */
    UNPAID("Unpaid"),
    /**
     * Paid payment status.
     */
    PAID("Paid"),
    /**
     * Refunded payment status.
     */
    REFUNDED("Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getJsonValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static PaymentStatus fromString(String value) {
        if (value == null)
            return null;
        String v = value.trim();
        try {
            return PaymentStatus.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (PaymentStatus s : values()) {
                if (s.displayName.equalsIgnoreCase(v) || s.name().equalsIgnoreCase(v))
                    return s;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
