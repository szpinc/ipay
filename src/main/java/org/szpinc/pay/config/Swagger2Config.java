package org.szpinc.pay.config;


import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Value("${swagger.api.title}")
    private String swaggerApiTitle;
    @Value("${swagger.api.description}")
    private String swaggerApiDescription;
    @Value("${swagger.api.ServiceUrl}")
    private String swaggerApiServiceUrl;
    @Value("${swagger.contact.name}")
    private String swaggerContactName;
    @Value("${swagger.contact.email}")
    private String swaggerContactEmail;
    @Value("${swagger.contact.url}")
    private String swaggerContactUrl;
    @Value("${swagger.api.version}")
    private String swaggerApiVersion;


    private final Logger LOG = LoggerFactory.getLogger(Swagger2Config.class);

    @Bean
    public Docket createRestApi() {

        if (LOG.isInfoEnabled()) {
            LOG.info("开始加载Swagger2");
        }

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //扫描所有使用API注解的Controller，用这种方式更灵活
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }


    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(swaggerApiTitle)
                .description(swaggerApiDescription)
                .termsOfServiceUrl(swaggerApiServiceUrl)
                .contact(new Contact(swaggerContactName, swaggerContactUrl, swaggerContactEmail))
                .version(swaggerApiVersion)
                .build();
    }

}
