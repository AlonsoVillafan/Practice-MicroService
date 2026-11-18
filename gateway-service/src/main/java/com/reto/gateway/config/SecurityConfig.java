package com.reto.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//Configuración reactiva que utiliza Spring Security para proteger endpoints mediante JWT.
//Configuración de seguridad del API Gateway.
//El Gateway será el encargado de validar el JWT
//antes de permitir el acceso a los microservicios.
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                 //Nuestra API trabaja con Bearer Tokens
                 //y no con sesiones/cookies de navegador,
                 //por lo que desactivamos CSRF.
                .csrf(
                        ServerHttpSecurity.CsrfSpec::disable
                )
                //Toda llamada hacia /api/**
                //necesita autenticación.
                .authorizeExchange(
                        exchanges -> exchanges

                                .pathMatchers("/api/**")
                                .authenticated()

                                //Cualquier otra ruta queda permitida por ahora.
                                .anyExchange()
                                .permitAll()
                )
                //Indicamos que los Bearer Tokens serán JWT.
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        Customizer.withDefaults()
                                )
                )
                .build();
    }
}