package com.example.ledgercore.auth.config;

import com.example.ledgercore.auth.security.filter.JwtAuthenticationFilter;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()

                                .requestMatchers(
                                        "/test/webhook"
                                ).permitAll()
                                
                                .requestMatchers(
                                        "/api/v1/auth/sign-up",
                                        "/api/v1/auth/verify-email",
                                        "/api/v1/auth/verify-email/resend",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh",
                                        "/api/v1/auth/logout"
                                ).permitAll()

                                .requestMatchers("/api/v1/admin/**")
                                .hasRole("ADMIN")
                                
                                .anyRequest()
                                .authenticated()
                )
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint()
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler()
                                )
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("*"));
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS",
                        "QUERY"
                ));
        configuration.setAllowedHeaders(
                List.of("*"));
        configuration.setExposedHeaders(
                List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {

            response.setContentType("application/json");
            response.setStatus(
                    ErrorCode.UNAUTHORIZED.getStatus().value()
            );

            ApiResponse<Object> body =
                    ApiResponse.error(
                            ErrorCode.UNAUTHORIZED.getCode(),
                            ErrorCode.UNAUTHORIZED.getMessage()
                    );

            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(body)
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {

            response.setContentType("application/json");
            response.setStatus(
                    ErrorCode.ACCESS_DENIED.getStatus().value()
            );

            ApiResponse<Object> body =
                    ApiResponse.error(
                            ErrorCode.ACCESS_DENIED.getCode(),
                            ErrorCode.ACCESS_DENIED.getMessage()
                    );

            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(body)
            );
        };
    }
}