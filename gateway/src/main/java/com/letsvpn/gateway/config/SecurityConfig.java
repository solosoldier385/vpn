package com.letsvpn.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.*;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomServerAuthenticationEntryPoint customServerAuthenticationEntryPoint;
    private final CustomServerAuthenticationFailureHandler customServerAuthenticationFailureHandler;


    // 放行 Swagger UI 自身相关的静态资源路径
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs", // 网关自身的 api-docs 根路径 (如果有)
            "/v3/api-docs/swagger-config", // 网关自身的 swagger-config (如果有)
            "/webjars/**",
    };

    // 放行公共 API 路径
    private static final String[] PUBLIC_API_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            // 其他需要公开访问的 API 端点
            "/api/user/node/free",
            "/api/user/node/list",
            // 添加支付接口到白名单
            //"/api/pay/req",
            //"/api/pay/**",
            //支付回调
            "/api/pay/channel/list",
            "/api/pay/notify**",
            "/api/pay/notify*/**",
            //app版本
            "/api/user/app/version"
    };

    // 放行通过网关访问的、聚合的各微服务的 API Docs 路径
    // 使用 * 匹配单个服务名段 (user, auth, order 等)
    private static final String[] AGGREGATED_API_DOCS_PATHS = {
            "/api/*/v3/api-docs/**"
    };

    // 新增：放行 Gateway 自身的 Actuator 健康检查端点
    private static final String[] ACTUATOR_HEALTH_PATHS = {
            "/actuator/health/readiness",
            "/actuator/health/liveness"
            // 如果需要，也可以放行 /actuator/health
            // "/actuator/health"
    };

//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        return http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 关闭 CSRF
//                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers(SWAGGER_PATHS).permitAll()          // 放行 Swagger UI 静态资源
//                        .pathMatchers(PUBLIC_API_PATHS).permitAll()       // 放行登录、注册等公共 API
//                        .pathMatchers(AGGREGATED_API_DOCS_PATHS).permitAll() // 明确放行聚合的 api-docs 路径
//                        .anyExchange().authenticated()                    // 其他所有请求都需要认证 (会被 GlobalAuthFilter 拦截处理)
//                )
//                .build();
//    }


    private final JwtAuthenticationManager jwtAuthenticationManager; // 注入自定义的认证管理器 (这个需要保持)




    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        // 1. 创建认证过滤器实例 (使用我们的 JwtAuthenticationManager)
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtAuthenticationManager);

        // 2. *** 使用我们自己写的 BearerTokenServerAuthenticationConverter ***
        authenticationWebFilter.setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter());

        authenticationWebFilter.setAuthenticationFailureHandler(customServerAuthenticationFailureHandler); // <--- 在这里设置


        // 3. 配置过滤器链
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 配置异常处理
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // 指定当发生认证异常时，使用我们自定义的 EntryPoint
                        .authenticationEntryPoint(customServerAuthenticationEntryPoint) // <--- 在这里配置使用你的自定义入口点
                )
                // 4. 添加认证过滤器到链中
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        // 5. 配置放行规则 (白名单)
                        .pathMatchers(SWAGGER_PATHS).permitAll()
                        .pathMatchers(PUBLIC_API_PATHS).permitAll()
                        .pathMatchers(AGGREGATED_API_DOCS_PATHS).permitAll()
                        .pathMatchers(ACTUATOR_HEALTH_PATHS).permitAll()  // <--- 新增：放行 Actuator 健康检查路径

                        // 6. 其他所有请求都需要认证
                        .anyExchange().authenticated()
                )
                // 7. 禁用其他认证机制
                .httpBasic().disable()
                .formLogin().disable();

        return http.build();
    }



}