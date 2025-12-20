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
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 1. ОТКРЫТЫЕ ЭНДПОИНТЫ
                        .requestMatchers("/api/auth/**").permitAll()

                        // 2. ЗАПЧАСТИ (PartController)
                        .requestMatchers(HttpMethod.GET, "/api/parts/restock-calculation").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/parts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/parts/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/parts/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.DELETE, "/api/parts/**").hasAuthority("ROLE_ADMIN")

                        // 3. КЛИЕНТЫ (CustomerController)
                        .requestMatchers(HttpMethod.GET, "/api/customers/*/total-spent").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasAuthority("ROLE_ADMIN")

                        // 4. ЗАКАЗЫ (ServiceOrderController)
                        // Специфичные операции ставим ВЫШЕ общих
                        .requestMatchers(HttpMethod.GET, "/api/orders/*/progress").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/complete").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/*/cancel").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_MECHANIC")
                        // Общие операции с заказами
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAuthority("ROLE_ADMIN")

                        // 5. ТРАНСПОРТ (VehicleController)
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/*/transfer-ownership").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC", "ROLE_CUSTOMER")

                        // 6. МЕХАНИКИ (MechanicController)
                        .requestMatchers(HttpMethod.GET, "/api/mechanics/*/workload").hasAnyAuthority("ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.GET, "/api/mechanics/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MECHANIC")
                        .requestMatchers(HttpMethod.POST, "/api/mechanics/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/mechanics/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mechanics/**").hasAuthority("ROLE_ADMIN")

                        // 7. ПОЛЬЗОВАТЕЛИ
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}