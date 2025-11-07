package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final com.example.backend.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(com.example.backend.security.JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Public: auth endpoints and API docs
        .requestMatchers(
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/error"
        ).permitAll()
                // VNPay và PayOS webhooks và return/cancel URLs (KHÔNG cần authentication)
                .requestMatchers(
                    "/api/payments/webhook/**",
                    "/api/payments/vnpay-return/**", 
                    "/api/payments/vnpay-notify/**",
                    "/api/payments/return/**", 
                    "/api/payments/cancel/**",
                    "/api/payments/success/**"
                ).permitAll()
                // Public GETs for cards, listings and home endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/cards/**", "/api/listings/**", "/api/home/**").permitAll()
                // all other requests require authentication
                .anyRequest().authenticated()
            );

        // Add JWT authentication filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // JwtAuthenticationFilter and UserDetailsService are provided by component scanning and the service layer; no placeholder beans here
}
