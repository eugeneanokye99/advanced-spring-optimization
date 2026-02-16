package com.shopjoy.dto.response;

import com.shopjoy.entity.AddressType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO for Address.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    
    private Integer id;
    private Integer userId;
    private AddressType addressType;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;
    private LocalDateTime createdAt;

}
