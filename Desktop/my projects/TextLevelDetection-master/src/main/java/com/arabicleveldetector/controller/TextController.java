package com.arabicleveldetector.controller;

import com.arabicleveldetector.model.TextInput;
import com.arabicleveldetector.model.TextLevel;
import com.arabicleveldetector.service.TextClassifierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TextController {
    private final TextClassifierService textClassifierService;

    public TextController(TextClassifierService textClassifierService) {
        this.textClassifierService = textClassifierService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("textInput", new TextInput()); // Initialize the form object
        return "index";
    }

    @PostMapping("/classify")
    public String classifyText(@ModelAttribute TextInput textInput, Model model) {
        try {
            if (textInput.getText() == null || textInput.getText().trim().isEmpty()) {
                model.addAttribute("error", "Please enter Arabic text");
                model.addAttribute("textInput", new TextInput()); // Reinitialize if error
                return "index";
            }

            TextLevel result = textClassifierService.classifyText(textInput.getText());
            model.addAttribute("result", result);
            model.addAttribute("originalText", textInput.getText());
            model.addAttribute("textInput", new TextInput()); // Clear form for new input
        } catch (Exception e) {
            model.addAttribute("error", "Error processing request: " + e.getMessage());
            model.addAttribute("textInput", new TextInput());
            return "index";
        }
        return "result";
    }
}