package cn.lyjuan.base.http.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

/**
 * swagger配置文件
 * 需要在Spring上配置{@link EnableSwagger2}注解
 */
public class SwaggerConfig
{
    @Bean
    public Docket addUserDocket()
    {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        ApiInfo apiInfo = new ApiInfo(
                "翊星建材",
                "API Document管理",
                "V1.0.1",
                "https://www.hzyistar.com/",
                "li17206@163.com",
                "PRIVATE",
                "");
        docket.apiInfo(apiInfo);
        Set<String> protocols = new HashSet<>();
        protocols.add("https");
        docket.protocols(protocols).host("api.hzyistar.com");
//        docket.host("localhost:9090");
        return docket;
    }
}
