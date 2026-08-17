package com.dropzone.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**", "/eureka/**", "/api/chaos", "/api/chaos/**", "/chaos", "/chaos/**", "/test-report", "/api/test-report", "/testing/**").permitAll()
                .pathMatchers("/api/orders", "/api/orders/**", "/orders", "/orders/**").hasAnyRole("USER", "ORGANIZER", "SUPPORT", "ADMIN")
                .pathMatchers("/api/payments", "/api/payments/**", "/payments", "/payments/**").hasAnyRole("USER", "ORGANIZER", "SUPPORT", "ADMIN")
                .pathMatchers("/api/audit", "/api/audit/**", "/audit", "/audit/**").hasAnyRole("USER", "ORGANIZER", "SUPPORT", "ADMIN")
                .pathMatchers("/api/notifications", "/api/notifications/**", "/notifications", "/notifications/**").hasAnyRole("USER", "ORGANIZER", "SUPPORT", "ADMIN")
                .pathMatchers("/api/inventory", "/api/inventory/**", "/inventory", "/inventory/**").hasAnyRole("USER", "ORGANIZER", "ADMIN")
                .pathMatchers("/api/events", "/api/events/**", "/events", "/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                .pathMatchers("/api/admin", "/api/admin/**", "/admin", "/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/users", "/api/users/**", "/users", "/users/**").hasAnyRole("USER", "ORGANIZER", "SUPPORT", "ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            );
        return http.build();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        return new ReactiveJwtAuthenticationConverterAdapter(jwt -> {
            Collection<GrantedAuthority> authorities = extractGrantedAuthorities(jwt);
            return new JwtAuthenticationToken(jwt, authorities);
        });
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractGrantedAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }
}
