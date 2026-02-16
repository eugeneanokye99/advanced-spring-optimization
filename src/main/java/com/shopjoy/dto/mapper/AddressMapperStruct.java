package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.request.UpdateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.entity.Address;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MapStruct mapper for Address entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
@Component
public interface AddressMapperStruct {

    /**
     * Maps CreateAddressRequest to Address entity.
     * 
     * @param request the create address request
     * @return the mapped address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", source = "isDefault", defaultValue = "false")
    @Mapping(target = "createdAt", ignore = true)
    Address toAddress(CreateAddressRequest request);

    /**
     * Maps Address entity to AddressResponse.
     * 
     * @param address the address entity
     * @return the mapped address response
     */
    @Mapping(target = "addressId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "default", source = "address.default")
    AddressResponse toAddressResponse(Address address);

    /**
     * Maps list of Address entities to list of AddressResponse DTOs.
     * 
     * @param addresses list of address entities
     * @return list of address response DTOs
     */
    List<AddressResponse> toAddressResponseList(List<Address> addresses);

    /**
     * Updates existing Address entity from UpdateAddressRequest.
     * Only maps non-null values from the request.
     * 
     * @param request the update request
     * @param address the existing address to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "default", source = "isDefault")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRequest(UpdateAddressRequest request, @MappingTarget Address address);

    /**
     * Custom method to handle default values during creation.
     */
    @AfterMapping
    default void setDefaults(@MappingTarget Address address, CreateAddressRequest request) {
        if (request.getIsDefault() == null) {
            address.setDefault(false);
        }
    }
}