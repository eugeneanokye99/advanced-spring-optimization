package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.UpdateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for User entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring", // Generate Spring component
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapperStruct {

    /**
     * Maps CreateUserRequest to User entity.
     * 
     * @param request the create user request
     * @return the mapped user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "userType", constant = "CUSTOMER")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(CreateUserRequest request);

    /**
     * Maps CreateUserRequest to User entity with specific user type.
     * 
     * @param request the create user request
     * @param userType the user type to assign
     * @return the mapped user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "request.password")
    @Mapping(target = "userType", source = "userType")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(CreateUserRequest request, UserType userType);

    /**
     * Maps User entity to UserResponse.
     * Password is never included in response for security.
     * 
     * @param user the user entity
     * @return the mapped user response
     */

    UserResponse toUserResponse(User user);

    /**
     * Maps list of User entities to list of UserResponse DTOs.
     * 
     * @param users list of user entities
     * @return list of user response DTOs
     */
    List<UserResponse> toUserResponseList(List<User> users);

    /**
     * Updates existing User entity from UpdateUserRequest.
     * Only maps non-null values from the request.
     * 
     * @param request the update request
     * @param user the existing user to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true) // Usually not updated
    @Mapping(target = "passwordHash", ignore = true) // Password updated separately
    @Mapping(target = "userType", ignore = true) // UserType not updated via user update
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Custom method to set default values during user creation.
     */
    @AfterMapping
    default void setUserDefaults(@MappingTarget User user, CreateUserRequest request) {
        if (user.getUserType() == null) {
            user.setUserType(UserType.CUSTOMER);
        }
    }
}