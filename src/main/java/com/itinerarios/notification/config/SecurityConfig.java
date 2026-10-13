package com.itinerarios.notification.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seguridad servicio-a-servicio (paso 31, seccion 108): el historial de notificaciones solo se
 * consulta con un JWT de Keycloak con scope notification.read (hoy, el Gateway). El consumo de
 * RabbitMQ y el handler Lambda no pasan por HTTP, asi que no se ven afectados.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                // CSRF queda activo: oauth2ResourceServer lo omite en peticiones con Bearer token (el
                // caso normal S2S) y sigue protegiendo cualquier otra (defensa en profundidad, Sonar S4502).
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notifications/**").hasAuthority("SCOPE_notification.read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((req, res, ex) ->
                                writeError(objectMapper, req, res, HttpStatus.UNAUTHORIZED, "Missing or invalid bearer token"))
                        .accessDeniedHandler((req, res, ex) ->
                                writeError(objectMapper, req, res, HttpStatus.FORBIDDEN, "Insufficient scope")));
        return http.build();
    }

    /** Mismo formato de error que los demas servicios (seccion 64). */
    private static void writeError(ObjectMapper objectMapper, HttpServletRequest request, HttpServletResponse response,
                                   HttpStatus status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.name());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("correlationId", request.getHeader("X-Correlation-ID"));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
