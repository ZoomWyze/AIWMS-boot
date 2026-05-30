package com.jsh.erp.config;

/**
 * Swagger2 API 文档配置类
 *
 * 功能：
 *   1. 启用 Swagger2 自动生成 API 文档
 *   2. 配置 API 信息（标题、描述、版本等）
 *   3. 扫描所有 Controller 生成接口文档
 *
 * 访问地址：http://localhost:9999/AIWMS-boot/swagger-ui.html
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 插件集成配置
 *
 * @author jishenghua
 * @version 1.0
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(this.apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("管伊佳ERP Restful Api")
                .description("管伊佳ERP接口描述")
                .termsOfServiceUrl("http://127.0.0.1")
                .contact(new Contact("jishenghua", "", ""))
                .version("3.0")
                .build();
    }

}
