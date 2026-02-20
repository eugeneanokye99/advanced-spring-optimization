package com.shopjoy.config;

import com.shopjoy.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;

/**
 * Configuration for Spring Security with JWT and OAuth2 support.
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
     * Configures the security filter chain with JWT authentication, OAuth2 login, and authorization rules.
     *
     * @param http HttpSecurity configuration
     * @return configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                // Authentication endpoints
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                
                // OAuth2 endpoints
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                
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
