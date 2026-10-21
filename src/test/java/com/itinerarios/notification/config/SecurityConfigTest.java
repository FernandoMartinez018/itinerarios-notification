package com.itinerarios.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    /** Regresion: la Lambda arranca sin servidor web y SecurityConfig no debe impedir el contexto. */
    @Test
    void contextoNoWeb_comoLaLambda_arrancaSinFiltroDeSeguridad() {
        new ApplicationContextRunner()
                .withUserConfiguration(SecurityConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).doesNotHaveBean(SecurityFilterChain.class);
                });
    }
}
