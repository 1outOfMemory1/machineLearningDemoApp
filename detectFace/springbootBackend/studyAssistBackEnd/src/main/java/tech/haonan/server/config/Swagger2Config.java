package tech.haonan.server.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

//   接口文档地址在  IP地址:端口/swagger-ui/index.html#/
@EnableSwagger2
@Configuration
public class    Swagger2Config {


    @Value("${jwt.tokenHeader}")
    private String tokenHeader;


    @Bean
    public Docket getDocket(Environment environment) { // 注意这里的environment
        // 下边几行可以进行判断环境 完成的目标是只有生产环境启用swagger2

//        //设置 如果是dev环境(生产环境)就启用swagger
//        Profiles profiles = Profiles.of("dev"); // 这个of 传的参数是个可变参数 可以传递多个string类型的值
//        boolean flag = environment.acceptsProfiles(profiles); // 返回flag 如果 处于dev环境 flag就是true

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //.enable(flag) // 是否启用swagger
                .groupName("main")  // 右上角可以选择分组  这里
                .select()
                //.apis(RequestHandlerSelectors.withMethodAnnotation(RequestMapping.class)) // 扫描方法上的注解
                //.apis(RequestHandlerSelectors.any()) // 扫描全部
                //.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)) // 扫描类上的注解
                .apis(RequestHandlerSelectors.basePackage("tech.haonan.server.controller")) // 只扫描一部分包
                .paths(PathSelectors.any())
                //.paths(PathSelectors.ant("/test/**")) // 只扫描形如/test/  的接口
                .build()
                // 以下两个配置是给swagger配置token携带  因为不用每次都加上登录验证这样非常完美
                .securityContexts(myGetSecurityContexts())
                .securitySchemes(myGetSecuritySchemes());

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("学习助手接口文档") // 标题
                .description("学习助手接口文档") // 描述
                .termsOfServiceUrl("https://haonan.tech") //
                .contact(new Contact("haonan", "http://localhost:8081/doc.html", "aaa@qq.com")) // 联系人
                .version("1.0")
                .build();
    }


    /**
     * 以下几个方法都是 为 .securityContexts(myGetSecurityContexts())
     *                 .securitySchemes(myGetSecuritySchemes());
     * 这两个服务的  效果是 网页右上角出现一个Authorize 按钮 然后 就可以把token(情况不同)填写到header 请求头中
     * 防止出现需要授权的接口 swagger无法使用的情况
     */

    private List<SecurityScheme> myGetSecuritySchemes(){
        List<SecurityScheme> result = new ArrayList<>();
        // 主要这里的名字要和 下边方法 defaultAuth 中的  第一个空一致 需要都叫  namehhh 应该是  不是十分确定
        //result.add(new SecurityReference("namehhh",authorizationScopes));
        ApiKey apiKey = new ApiKey("namehhh",tokenHeader,"Header");
        result.add(apiKey);
        return result;
    }
    private List<SecurityContext> myGetSecurityContexts(){
        List<SecurityContext> result = new ArrayList<>();
        result.add(getContextByPath("/.*"));
        return result;
    }

    private SecurityContext getContextByPath(String pathRegex) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(pathRegex) )
                .build();

    }
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> result = new ArrayList<>();
        AuthorizationScope authorizationScope = new AuthorizationScope("global","允许所有人访问");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        result.add(new SecurityReference("namehhh",authorizationScopes));
        return result;
    }
}