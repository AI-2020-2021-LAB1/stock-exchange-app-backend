package com.project.stockexchangeappbackend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.project.stockexchangeappbackend.rest"))
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(swaggerApiInfo());
    }

    private ApiInfo swaggerApiInfo () {
        return new ApiInfoBuilder()
                .title("Stock Exchange")
                .build();
    }


    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(
            InMemorySwaggerResourcesProvider defaultResourcesProvider) {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>(defaultResourcesProvider.get());
            resources.add(loadOAuthResource());
            return resources;
        };
    }

    private SwaggerResource loadOAuthResource() {
        SwaggerResource wsResource = new SwaggerResource();
        wsResource.setName("OAuth");
        wsResource.setSwaggerVersion("2.0");
        wsResource.setLocation("/swagger.yml");
        return wsResource;
    }

}
