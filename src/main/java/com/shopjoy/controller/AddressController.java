package com.shopjoy.controller;

import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.request.UpdateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Address controller.
 */
@Tag(name = "Address Management", description = "APIs for managing user addresses including shipping and billing addresses")
@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    /**
     * Instantiates a new Address controller.
     *
     * @param addressService the address service
     */
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Create address response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Create address",
            description = "Creates a new address for a user (shipping or billing)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Address created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid address data",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Address created successfully"));
    }

    /**
     * Gets address by id.
     *
     * @param id the id
     * @return the address by id
     */
    @Operation(
            summary = "Get address by ID",
            description = "Retrieves a specific address by its unique identifier"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @Parameter(description = "Address unique identifier", required = true, example = "1")
            @PathVariable Integer id) {
        AddressResponse response = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Address retrieved successfully"));
    }

    /**
     * Gets addresses by user.
     *
     * @param userId the user id
     * @return the addresses by user
     */
    @Operation(
            summary = "Get addresses by user",
            description = "Retrieves all addresses for a specific user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User addresses retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddressesByUser(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        List<AddressResponse> response = addressService.getAddressesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "User addresses retrieved successfully"));
    }

    /**
     * Gets default address.
     *
     * @param userId the user id
     * @return the default address
     */
    @Operation(
            summary = "Get default address",
            description = "Retrieves the default address for a specific user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Default address retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No default address found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId) {
        AddressResponse response = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Default address retrieved successfully"));
    }

    /**
     * Sets default address.
     *
     * @param id the id
     * @return the default address
     */
    @Operation(
            summary = "Set default address",
            description = "Sets an address as the default address for a user"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Default address set successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @Parameter(description = "Address unique identifier", required = true, example = "1")
            @PathVariable Integer id) {
        AddressResponse response = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Default address set successfully"));
    }

    /**
     * Update address response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Update address",
            description = "Updates an existing address details"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid address data",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @Parameter(description = "Address unique identifier", required = true, example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Address updated successfully"));
    }

    /**
     * Delete address response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @Operation(
            summary = "Delete address",
            description = "Permanently deletes an address"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address deleted successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Address unique identifier", required = true, example = "1")
            @PathVariable Integer id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}
