package com.shopjoy.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails implementation that extends Spring Security's User class
 * to include additional user information like userId for authorization checks.
 */
@Getter
public class CustomUserDetails extends User {

    private final Integer userId;

    /**
     * Constructs a CustomUserDetails with user authentication and authorization information.
     *
     * @param userId      the unique identifier of the user
     * @param username    the username used for authentication
     * @param password    the password hash
     * @param authorities the collection of granted authorities (roles)
     */
    public CustomUserDetails(
            Integer userId,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    /**
     * Constructs a CustomUserDetails with full account status information.
     *
     * @param userId             the unique identifier of the user
     * @param username           the username used for authentication
     * @param password           the password hash
     * @param enabled            true if the user is enabled
     * @param accountNonExpired  true if the account has not expired
     * @param credentialsNonExpired true if the credentials have not expired
     * @param accountNonLocked   true if the account is not locked
     * @param authorities        the collection of granted authorities (roles)
     */
    public CustomUserDetails(
            Integer userId,
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }
}
