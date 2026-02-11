package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.service.AddressService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class AddressQueryResolver {

    private final AddressService addressService;

    public AddressQueryResolver(AddressService addressService) {
        this.addressService = addressService;
    }

    @QueryMapping
    public AddressResponse address(@Argument Long id) {
        return addressService.getAddressById(id.intValue());
    }

    @QueryMapping
    public List<AddressResponse> addresses(@Argument Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return addressService.getAddressesByUser(userId.intValue());
    }
}
