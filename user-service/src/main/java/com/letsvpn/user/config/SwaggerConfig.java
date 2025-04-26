package com.letsvpn.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    final String securitySchemeName = "bearerAuth"; // 定义一个名称，要和 @SecurityRequirement 中一致


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LetsVPN API USER文档")
                        .version("1.0")
                        .description("接口文档说明"))
                // --- 添加 Components 和 SecuritySchemes ---
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, // 使用上面定义的名称
                                new SecurityScheme()
                                        .name(securitySchemeName) // 名称
                                        .type(SecurityScheme.Type.HTTP) // 类型: HTTP
                                        .scheme("bearer") // 具体的 Scheme: bearer
                                        .bearerFormat("JWT") // Token 格式: JWT
                                        .description("在此处输入你的 JWT Token (不需要加 'Bearer ' 前缀)") // 给用户的提示
                        )
                )
                // --- 可选: 如果所有接口都需要认证，可以在这里全局添加要求 ---
                // .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                ;

    }
}
