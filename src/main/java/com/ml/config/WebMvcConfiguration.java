package com.ml.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/login");

        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/forgetPwd").setViewName("forgetPwd");
        registry.addViewController("/403").setViewName("error/403");
        registry.addViewController("/500").setViewName("error/500");
    }
}
