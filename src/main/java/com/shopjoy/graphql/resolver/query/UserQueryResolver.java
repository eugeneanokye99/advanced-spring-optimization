package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.graphql.type.PageInfo;
import com.shopjoy.graphql.type.UserConnection;
import com.shopjoy.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserQueryResolver {

    private final UserService userService;

    public UserQueryResolver(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserConnection users(
            @Argument Integer page,
            @Argument Integer size
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        List<UserResponse> allUsers = userService.getAllUsers();
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageSize, allUsers.size());
        List<UserResponse> paginatedUsers = (start < allUsers.size()) 
            ? allUsers.subList(start, end) 
            : List.of();
        
        Page<UserResponse> userPage = new PageImpl<>(paginatedUsers, pageable, allUsers.size());
        
        PageInfo pageInfo = new PageInfo(
                pageNum,
                pageSize,
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );

        return new UserConnection(userPage.getContent(), pageInfo);
    }
}
