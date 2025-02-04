package net.gaokd.gcloudaipan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: Knife4jConfig
 * @Author: gkd
 * @date: 2025/2/4 15:48
 * @Version: V1.0
 */
@Slf4j
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("智能云盘开发")
                        .version("1.0")
                        .description("AI网盘系统")
                        .termsOfService("https://xxx.net")
                        .license(new License().name("Apache 2.0").url("https://xxx.net"))
                        .contact(new Contact()
                                .name("Gaokd")
                                .email("819978029@qq.com")
                                .url("https://xxx.net"))
        );
    }
}
