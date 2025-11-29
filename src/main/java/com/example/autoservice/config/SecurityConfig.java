package com.example.autoservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/auth/register")
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/register").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()

                        // ===== КАСТОМЕР =====
                        // Customer: может добавлять заказы, но не просматривать/удалять чужие
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("ADMIN", "CUSTOMER", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/orders/customer/**").hasAnyRole("ADMIN", "CUSTOMER") // свои заказы
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "MECHANIC") // все заказы - только админ и механик

                        // ===== МЕХАНИК =====
                        // Mechanic: полный доступ к parts (добавлять, просматривать, удалять)
                        .requestMatchers(HttpMethod.GET, "/api/parts/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.POST, "/api/parts/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.PUT, "/api/parts/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.DELETE, "/api/parts/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Mechanic: просмотр заказов, кастомеров и машин
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Mechanic: удаление заказов
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAnyRole("ADMIN", "MECHANIC")

                        // ===== ОБЩИЕ ПРАВИЛА =====
                        // GET - админ и механик (просмотр)
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/mechanics/**").hasAnyRole("ADMIN", "MECHANIC")

                        // POST - все роли (добавление)
                        .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyRole("ADMIN", "CUSTOMER", "MECHANIC")
                        .requestMatchers(HttpMethod.POST, "/api/mechanics/**").hasAnyRole("ADMIN", "MECHANIC")

                        // PUT/DELETE - только админ (редактирование и удаление основных сущностей)
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mechanics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mechanics/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}