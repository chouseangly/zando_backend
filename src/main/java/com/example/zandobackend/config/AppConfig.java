package com.example.zandobackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
// import org.springframework.web.servlet.config.annotation.CorsRegistry; <-- REMOVE
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; <-- REMOVE

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*
     * REMOVE THIS ENTIRE BEAN.
     * The new SecurityConfig class now handles all CORS configurations.
     *
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
    */
}