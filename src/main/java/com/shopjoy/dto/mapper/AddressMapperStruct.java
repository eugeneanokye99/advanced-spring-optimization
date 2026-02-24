package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.request.UpdateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.entity.Address;
import org.mapstruct.*;

/**
 * MapStruct mapper for Address entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
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
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "default", source = "default")
    AddressResponse toAddressResponse(Address address);

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