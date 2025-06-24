package com.example.DiplomaGeneration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataXlsxTest {

    @InjectMocks
    private DataXlsx dataXlsx;

    @Mock
    private MultipartFile fileMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private byte[] createXlsxFile(String[][] data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Test
    public void testReadingXlsx() throws Exception {
        String[][] excelData = {
                {"Team", "University", "NameA","SurnameA","NameT","SurnameT"},
                {"Team1", "University1", "Name1","Surname1","NameT1","SurnameT1"}
        };

        byte[] byteArray = createXlsxFile(excelData);
        when(fileMock.getBytes()).thenReturn(byteArray);

        List<DataXlsx> records = new ArrayList<>();
        List<Integer> columnNumbers = List.of(1, 2, 3,4,5,6);
        dataXlsx.readingXlsx(fileMock, records, columnNumbers);

        assertEquals(1, records.size());
        DataXlsx record = records.get(0);
        assertEquals("Team1", record.getTeam());
        assertEquals("University1", record.getUniversity());
        assertEquals("Name1 Surname1", record.getNameA());
        assertEquals("NameT1 SurnameT1", record.getNameB());

    }

    @Test
    public void testProcessExcel() throws Exception {
        String[][] excelData = {
                {"Команда", "ВУЗ", "Участник1"},
                {"Value1", "Value2", "Value3"}
        };

        byte[] byteArray = createXlsxFile(excelData);
        when(fileMock.getBytes()).thenReturn(byteArray);

        List<String> headers = dataXlsx.processExcel(fileMock);
        assertEquals(3, headers.size());
        assertEquals("Команда", headers.get(0));
        assertEquals("ВУЗ", headers.get(1));
        assertEquals("Участник1", headers.get(2));
    }
}

