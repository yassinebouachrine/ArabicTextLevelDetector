package com.arabicleveldetector.service;

import com.arabicleveldetector.model.TextFeatures;
import com.arabicleveldetector.util.TextProcessor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class DataService {
    private final TextProcessor textProcessor;

    @Autowired
    public DataService(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
    }

    public Map<String, String> loadTrainingData() throws IOException {
        Map<String, String> data = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = new ClassPathResource("data/arabic_text_level.xlsx").getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row == null || row.getRowNum() == 0) continue;

                String text = getCellValue(row.getCell(0), formatter);
                String level = getCellValue(row.getCell(1), formatter);

                if (!text.isEmpty() && !level.isEmpty()) {
                    data.put(text, level);
                }
            }
        }

        if (data.isEmpty()) {
            throw new IOException("No valid training data found");
        }

        System.out.printf("Successfully loaded %d training samples%n", data.size());
        return data;
    }

    private String getCellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }
}