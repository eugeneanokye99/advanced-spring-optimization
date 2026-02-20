package com.shopjoy.config;

import com.shopjoy.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;

/**
 * Configuration for Spring Security with JWT and OAuth2 support.
 * 
 * This configuration demonstrates the difference between:
 * 1. CSRF protection for form-based/session-based endpoints (/demo/*)
 * 2. No CSRF protection for JWT-based stateless API endpoints (/api/**)
 * 
 * WHY CSRF IS DISABLED FOR JWT APIs:
 * - JWT tokens are stored in localStorage/sessionStorage, not cookies
 * - Browsers do NOT automatically attach JWT tokens to requests
 * - Attacker cannot force victim's browser to send authenticated requests
 * - CSRF attacks rely on automatic cookie attachment by browsers
 * - Therefore, JWT APIs are inherently protected from CSRF attacks
 * 
 * WHY CSRF IS ENABLED FOR FORM ENDPOINTS:
 * - Session-based authentication uses cookies (JSESSIONID)
 * - Browsers automatically attach cookies to ALL requests to the domain
 * - Attacker can trick user into submitting malicious form on attacker's site
 * - Browser will automatically send session cookie, authenticating the request
 * - CSRF token prevents this by requiring a secret token that attacker cannot obtain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    /**
     * Configures the AuthenticationManager for processing authentication requests.
     *
     * @param authenticationConfiguration Spring's authentication configuration
     * @return configured AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Security filter chain for form-based demo endpoints with CSRF protection.
     * 
     * This filter chain applies to /demo/** endpoints and demonstrates traditional
     * session-based security with CSRF protection. It is evaluated first (@Order(1)).
     * 
     * CSRF PROTECTION ENABLED because:
     * - These endpoints use session-based authentication (cookies)
     * - Browsers automatically send cookies with every request
     * - Vulnerable to CSRF attacks without token protection
     * 
     * @param http HttpSecurity configuration
     * @return configured SecurityFilterChain with CSRF enabled
     */
    @Bean
    @Order(1)
    public SecurityFilterChain formSecurityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http
            .securityMatcher("/demo/**")
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/demo/csrf-token", "/demo/data").permitAll()
                .requestMatchers("/demo/**").permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }

    /**
     * Security filter chain for JWT-based API endpoints WITHOUT CSRF protection.
     * 
     * This filter chain applies to all other endpoints and uses JWT authentication.
     * It is evaluated second (@Order(2)) after the form filter chain.
     * 
     * CSRF PROTECTION DISABLED because:
     * - JWT tokens are stored in localStorage/sessionStorage, NOT cookies
     * - Browsers do NOT automatically attach Authorization headers
     * - Attacker cannot force victim's browser to send JWT token
     * - JWT APIs are inherently immune to CSRF attacks
     * - CSRF relies on automatic credential submission (cookies), which doesn't apply to JWTs
     *
     * @param http HttpSecurity configuration
     * @return configured SecurityFilterChain with CSRF disabled
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                // Authentication endpoints
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
                // GraphQL endpoints - require authentication
                .requestMatchers("/graphql", "/graphiql").authenticated()
                
                // Public GET endpoints for browsing (read-only)
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").permitAll()
                
                // Admin-only write operations for products, categories, reviews, inventory
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")
                
                // Review operations - authenticated users can create, only admins can update/delete
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/reviews/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/inventory/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/inventory/**").hasRole("ADMIN")
                
                .requestMatchers("/api/v1/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                
                .requestMatchers("/api/v1/security-audit-logs/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/login?error=oauth2_failed")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
