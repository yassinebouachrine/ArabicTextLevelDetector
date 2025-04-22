package com.arabicleveldetector;

import com.arabicleveldetector.service.DeepLearningService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ArabicLevelDetectorApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ArabicLevelDetectorApplication.class, args);

        try {
            DeepLearningService dlService = context.getBean(DeepLearningService.class);
            System.out.println("Initializing model...");
            dlService.initializeModel();
            System.out.println("System ready for predictions");
        } catch (Exception e) {
            System.err.println("Startup failed: " + e.getMessage());
            System.exit(1);
        }
    }
}