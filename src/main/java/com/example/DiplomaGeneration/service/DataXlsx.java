/**
 * @file DataXlsx.java
 */
package com.example.DiplomaGeneration.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class DataXlsx {
    private String team;
    private String university;
    private String nameA;
    private String surnameA;
    private String nameB;
    private String surnameB;
    private String nameC;
    private String surnameC;
    private String trainer;
    private String surnameTrainer;

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getNameA() {
        return nameA + " " + surnameA;
    }

    public void setNameA(String nameA) {
        this.nameA = nameA;
    }

    public String getNameB() {
        return nameB + " " + surnameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }

    public String getNameC() {
        return nameC + " " + surnameC;
    }

    public void setNameC(String nameC) {
        this.nameC = nameC;
    }

    public void setSurnameA(String surnameA) {
        this.surnameA = surnameA;
    }

    public void setSurnameB(String surnameB) {
        this.surnameB = surnameB;
    }

    public void setSurnameC(String surnameC) {
        this.surnameC = surnameC;
    }

    public void setSurnameTrainer(String surnameTrainer) {
        this.surnameTrainer = surnameTrainer;
    }

    public String getTrainer() {
        return trainer + " " + surnameTrainer;
    }

    public void setTrainer(String trainer) {
        this.trainer = trainer;
    }

    public void readingXlsx(MultipartFile file, List<DataXlsx> records, List<Integer> columnNumbers) {
        System.out.println("Данные из excel");
        try {
            byte[] fileData = file.getBytes();
            try (InputStream fileXLSX = new ByteArrayInputStream(fileData);
                 Workbook workbook = new XSSFWorkbook(fileXLSX)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    DataXlsx r = new DataXlsx();
                    for (int column : columnNumbers) {
                        Cell cell = row.getCell(column - 1);
                        String cellValue = "";
                        if (cell != null) {
                            cellValue = cell.getStringCellValue();
                        }
                        // Запись значений в объект r
                        for (int i = 0; i < columnNumbers.size(); i++) {
                            if (column == columnNumbers.get(i)) {
                                switch (i) {
                                    case 0:
                                        r.setTeam(cellValue);
                                        break;
                                    case 1:
                                        r.setUniversity(cellValue);
                                        break;
                                    case 2:
                                        r.setNameA(cellValue);
                                        break;
                                    case 3:
                                        r.setSurnameA(cellValue);
                                        break;
                                    case 4:
                                        r.setNameB(cellValue);
                                        break;
                                    case 5:
                                        r.setSurnameB(cellValue);
                                        break;
                                    case 6:
                                        r.setNameC(cellValue);
                                        break;
                                    case 7:
                                        r.setSurnameC(cellValue);
                                        break;
                                    case 8:
                                        r.setTrainer(cellValue);
                                        break;
                                    case 9:
                                        r.setSurnameTrainer(cellValue);
                                        break;
                                }
                            }
                        }
                    }
                    records.add(r);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> processExcel(MultipartFile file) {
        List<String> headers = new ArrayList<>();
        try {
            byte[] fileData = file.getBytes();
            try (InputStream fileXLSX = new ByteArrayInputStream(fileData);
                 Workbook workbook = new XSSFWorkbook(fileXLSX)) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return headers;
    }
}
