package com.zjt.httpreverseproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author zhujuntao
 * @create 2018-7-15 22:39
 * @desc 说明 启动入口
 */
@SpringBootApplication
@EnableSwagger2
@ComponentScan({"com.zjt.httpreverseproxy"})
public class HttpReverseProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpReverseProxyApplication.class, args);
    }

    /**
     * swagger start
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zjt.httpreverseproxy"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Http反向代理 APIs")
                .description("Http反向代理 restful APIs")
                .termsOfServiceUrl("https://XXXX")
                .contact(new Contact("项目组", "https://XXXX", "354623698@qq.com"))
                .version("1.0")
                .build();
    }
    /** swagger end */

}
