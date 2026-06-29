package com.timeweaver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Additional web MVC configuration if needed
    // CORS is handled in SecurityConfig
}
