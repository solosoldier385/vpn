package com.letsvpn.pay.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class PasswordEncoderConfig {

//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    private static final String[] SWAGGER_WHITELIST = {
//            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
//    };
//
//    private static final String[] NODE_WHITELIST = {
//            "/node/free"
//    };
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .csrf().disable()
//                .exceptionHandling()
//                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                .and()
//                .authorizeRequests()
//                .antMatchers(SWAGGER_WHITELIST).permitAll()
//                .antMatchers(NODE_WHITELIST).permitAll()
//                .antMatchers("/user/**").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll() // <--- 修改这里，临时允许所有请求

//                .antMatchers("/user/**", "/node/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                .anyRequest().authenticated()
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
