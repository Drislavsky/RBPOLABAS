package com.example.autoservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF — не нужен для stateless JWT API
                .csrf(csrf -> csrf.disable())

                // Отключаем Basic Auth — мы используем только JWT в Bearer
                .httpBasic(httpBasic -> httpBasic.disable())

                // Stateless — не создаём сессии на сервере
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Добавляем наш JWT-фильтр ПЕРЕД стандартным фильтром аутентификации
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Правила доступа
                .authorizeHttpRequests(authz -> authz
                        // Открытые эндпоинты для аутентификации
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/register").hasRole("ADMIN")

                        // Пользователи — только админ
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Заказы
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("ADMIN", "CUSTOMER", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/orders/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Детали (parts) — админ и механик
                        .requestMatchers("/api/parts/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Клиенты, машины, механики — просмотр админ и механик
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/mechanics/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Создание сущностей
                        .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyRole("ADMIN", "CUSTOMER", "MECHANIC")
                        .requestMatchers(HttpMethod.POST, "/api/mechanics/**").hasAnyRole("ADMIN", "MECHANIC")

                        // Редактирование и удаление
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyRole("ADMIN", "CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/mechanics/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mechanics/**").hasRole("ADMIN")

                        // Всё остальное — требует аутентификации по JWT
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}