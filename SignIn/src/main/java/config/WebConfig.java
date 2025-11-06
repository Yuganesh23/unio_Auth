package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.Profiles;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private Environment environment; 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow all origins during development (specific origins in production)
   //     String frontendUrl = (environment.acceptsProfiles("prod")) ? "https://your-production-frontend.com" : "http://localhost:8081";

    	registry.addMapping("/**").allowedOrigins("http://localhost:8081")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
