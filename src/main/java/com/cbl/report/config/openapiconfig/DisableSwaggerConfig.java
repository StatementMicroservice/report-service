package com.cbl.report.config.openapiconfig;

import com.cbl.report.config.profileconfig.ConditionalOnProfile;
import com.cbl.report.enums.EnvironmentProfile;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ConditionalOnProfile({EnvironmentProfile.PROD
                       , EnvironmentProfile.QA
                       , EnvironmentProfile.UAT})
@Configuration
public class DisableSwaggerConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/city-statement/report/v3/api-docs").addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/v3/api-docs.yml")
                .addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/v3/api-docs/**")
                .addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/swagger-resources")
                .addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/swagger-resources/**")
                .addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/swagger-ui").addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/swagger-ui.html")
                .addResourceLocations("classpath:/no-swagger/");
        registry.addResourceHandler("/city-statement/report/swagger-ui/index.html")
                .addResourceLocations("classpath:/no-swagger/");
    }
}
