// src/main/java/com/example/demo/CorsConfig.java
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedOrigins("http://localhost:5173") // Vite default
      .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
  }
}
