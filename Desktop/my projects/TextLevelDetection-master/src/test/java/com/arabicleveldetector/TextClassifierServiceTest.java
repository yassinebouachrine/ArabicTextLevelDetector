//// TextClassifierServiceTest.java
//package com.arabicleveldetector;
//
//import com.arabicleveldetector.model.TextLevel;
//import com.arabicleveldetector.service.TextClassifierService;
//import com.arabicleveldetector.util.TextProcessor;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class TextClassifierServiceTest {
//
//    private TextClassifierService classifierService;
//    private TextProcessor textProcessor;
//
//    @BeforeEach
//    public void setup() {
//        textProcessor = new TextProcessor();
//        classifierService = new TextClassifierService();
//        // Inject dependencies manually for testing
//        // Use reflection to set textProcessor
//        try {
//            java.lang.reflect.Field field = TextClassifierService.class.getDeclaredField("textProcessor");
//            field.setAccessible(true);
//            field.set(classifierService, textProcessor);
//
//            // Call initialize method
//            java.lang.reflect.Method method = TextClassifierService.class.getDeclaredMethod("initialize");
//            method.setAccessible(true);
//            method.invoke(classifierService);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testBasicClassification() {
//        // Test basic A level sentence
//        String basicText = "أنا أحب القراءة";
//        TextLevel level = classifierService.classifyText(basicText);
//        assertEquals("A", level.getLevel());
//
//        // Test more complex sentence
//        String complexText = "على الرغم من التحديات التي نواجهها في العالم المعاصر، فإن التكنولوجيا تقدم حلولاً مبتكرة للعديد من المشكلات";
//        level = classifierService.classifyText(complexText);
//        assertTrue(level.getLevel().equals("C") || level.getLevel().equals("C1"));
//    }
//
//    @Test
//    public void testTextProcessor() {
//        String text = "مَرْحَبًا بِالعَالَم";
//        String processed = textProcessor.preprocess(text);
//        assertEquals("مرحبا بالعالم", processed);
//
//        assertEquals(2, textProcessor.countWords(processed));
//    }
//}