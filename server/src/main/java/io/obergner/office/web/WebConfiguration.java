package io.obergner.office.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfiguration {

    @Bean
    public AllowAllOriginsCORSFilter allowAllOriginsCORSFilter() {
        return new AllowAllOriginsCORSFilter();
    }
}
