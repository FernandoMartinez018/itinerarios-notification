package com.itinerarios.notification.controller;

import com.itinerarios.notification.config.SecurityConfig;
import com.itinerarios.notification.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Seguridad S2S del historial (paso 31): sin token 401, scope incorrecto 403, scope correcto 200. */
@WebMvcTest(NotificationHistoryController.class)
@Import(SecurityConfig.class)
class NotificationHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationHistoryRepository repository;

    @Test
    void conScopeNotificationRead_devuelve200() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_notification.read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void sinToken_devuelve401ConJsonDeError() throws Exception {
        mockMvc.perform(get("/api/notifications").header("X-Correlation-ID", "sec-n-01"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.correlationId").value("sec-n-01"));
    }

    @Test
    void scopeIncorrecto_devuelve403() throws Exception {
        mockMvc.perform(get("/api/notifications").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_airport.read"))))
                .andExpect(status().isForbidden());
    }
}
