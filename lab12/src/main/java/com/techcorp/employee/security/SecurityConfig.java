package com.techcorp.employee.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Łańcuch dla API - Basic Auth
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .httpBasic(httpBasic -> {});

        return http.build();
    }

    /**
     * Łańcuch dla aplikacji webowej - Form Login
     */
    @Bean
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // Wyłącz CSRF dla H2 Console
                )
                .authorizeHttpRequests(auth -> auth
                        // Publiczne zasoby
                        .requestMatchers("/css/**", "/images/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // H2 Console dostępna dla wszystkich
                        .requestMatchers("/login", "/error").permitAll()

                        // Uprawnienia dla ról
                        .requestMatchers("/employees/delete/**", "/employees/edit/**",
                                "/employees/update/**", "/employees/new", "/departments/add", "/departments/edit/**",
                                "/departments/delete/**", "/departments/details/**/assign-employee",
                                "/departments/documents/**/upload", "/departments/documents/**/delete").hasRole("ADMIN")
                        .requestMatchers("/logs/**", "/api/logs/**").hasRole("ADMIN")

                        // Reszta wymaga zalogowania
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/employees", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .userDetailsService(userDetailsService)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Zezwól na ramki dla H2 Console
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}