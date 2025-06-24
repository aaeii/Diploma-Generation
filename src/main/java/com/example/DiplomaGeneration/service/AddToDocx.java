package com.example.DiplomaGeneration.service;

import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
public class AddToDocx {
    public String path = "src/diplomas/";
    public String formatDocx = ".docx";
    public String formatPdf = ".pdf";
    private static final String ORIENTATION_LANDSCAPE = "landscape";
    private static final String ORIENTATION_PORTRAIT = "portrait";
    private final GetDataHTML getDataHTML;

    public AddToDocx(GetDataHTML getDataHTML) {
        this.getDataHTML = getDataHTML;
    }

    private static void ensureDirectoryExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private void fileConversion(XWPFDocument doc1, String newFile, DataXlsx r, int index, List<Integer> tasks, int degreeNumber) throws IOException, InvalidFormatException, XmlException {
        XWPFDocument doc2 = new XWPFDocument();
        ensureDirectoryExists(path);
        copyFile(doc1, doc2, newFile);
        for (XWPFParagraph p : doc2.getParagraphs()) {
            replace(p, "<name_of_team>", r.getTeam());
            replace(p, "<name_of_university>", r.getUniversity());
            replace(p, "<coach>", r.getTrainer());
            replace(p, "<contant1>", r.getNameA());
            replace(p, "<contant2>", r.getNameB());
            replace(p, "<contant3>", r.getNameC());
            replace(p, "<0>", Integer.toString(tasks.get(index)));
            if (degreeNumber == 1) {
                replace(p, "<I>", "I");
            } else if (degreeNumber == 2) {
                replace(p, "<I>", "II");
            } else if (degreeNumber == 3) {
                replace(p, "<I>", "III");
            }
        }
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            doc2.write(out);
        }
    }

    private void replace(XWPFParagraph paragraph, String oldString, String newString) {
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            fullText.append(run.getText(0));
        }
        String updatedText = fullText.toString().replace(oldString, newString);
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            paragraph.getRuns().get(i).setText("", 0);
        }
        XWPFRun newRun = paragraph.createRun();
        newRun.setText(updatedText);
        if (!paragraph.getRuns().isEmpty()) {
            XWPFRun oldRun = paragraph.getRuns().get(0);
            copyStyles(oldRun, newRun);
        }
    }

    private static void copyStyles(XWPFRun sourceRun, XWPFRun targetRun) {
        Double fontSize = sourceRun.getFontSizeAsDouble();
        if (fontSize != null) {
            targetRun.setBold(sourceRun.isBold());
            targetRun.setItalic(sourceRun.isItalic());
            targetRun.setUnderline(sourceRun.getUnderline());
            targetRun.setFontFamily(sourceRun.getFontFamily());
            targetRun.setColor(sourceRun.getColor());
            targetRun.setTextPosition(sourceRun.getTextPosition());
            targetRun.setStyle(sourceRun.getStyle());
            targetRun.setFontSize(fontSize.intValue());
        } else {
            targetRun.setBold(false);
            targetRun.setItalic(false);
            targetRun.setUnderline(UnderlinePatterns.NONE);
            targetRun.setFontFamily(null);
            targetRun.setColor("FFFFFF");
            targetRun.setTextPosition(0);
            targetRun.setStyle(null);
            targetRun.setFontSize(0);
        }
    }

    private String orientationPage(XWPFDocument doc) {
        CTDocument1 ctDocument = doc.getDocument();
        CTBody body = ctDocument.getBody();
        CTSectPr sectPr = body.getSectPr();
        if (sectPr == null) {
            return ORIENTATION_PORTRAIT;
        }
        CTPageSz pageSize = sectPr.getPgSz();
        BigInteger width = (BigInteger) pageSize.getW();
        BigInteger height = (BigInteger) pageSize.getH();
        if (width.compareTo(height) > 0) {
            return ORIENTATION_LANDSCAPE;
        } else {
            return ORIENTATION_PORTRAIT;
        }
    }

    private void copyFile(XWPFDocument doc1, XWPFDocument doc2, String newFile) throws IOException {
        for (IBodyElement element : doc1.getBodyElements()) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph sourcePara = (XWPFParagraph) element;
                XWPFParagraph targetPara = doc2.createParagraph();
                copyParagraph(sourcePara, targetPara);
            } else if (element instanceof XWPFTable) {
                XWPFTable sourceTable = (XWPFTable) element;
                XWPFTable targetTable = doc2.createTable();
                copyTable(sourceTable, targetTable);
            }
        }
        String orientation = orientationPage(doc1);
        setPageSize(doc2, orientation);
        try (FileOutputStream out = new FileOutputStream(newFile)) {
            doc2.write(out);
        }
    }

    private static void copyParagraph(XWPFParagraph source, XWPFParagraph target) {
        target.getCTP().setPPr(source.getCTP().getPPr());
        for (XWPFRun sourceRun : source.getRuns()) {
            XWPFRun targetRun = target.createRun();
            copyRun(sourceRun, targetRun);
        }
    }

    private static void copyRun(XWPFRun source, XWPFRun target) {
        target.getCTR().setRPr(source.getCTR().getRPr());
        String text = source.getText(0);
        if (text != null && !text.isEmpty()) {
            target.setText(text, 0);
        }
        for (XWPFPicture picture : source.getEmbeddedPictures()) {
            try {
                XWPFPictureData pictureData = picture.getPictureData();
                byte[] data = pictureData.getData();
                long width = picture.getCTPicture().getSpPr().getXfrm().getExt().getCx();
                long height = picture.getCTPicture().getSpPr().getXfrm().getExt().getCy();
                if (text != null && !text.isEmpty()) {
                    target = target.getParagraph().createRun();
                }
                target.addPicture(new ByteArrayInputStream(data),
                        pictureData.getPictureType(),
                        pictureData.getFileName(),
                        (int) width,
                        (int) height);
            } catch (Exception e) {
                System.err.println("Ошибка при копировании изображения: " + e.getMessage());
            }
        }
    }

    private static void copyTable(XWPFTable source, XWPFTable target) {
        target.getCTTbl().setTblPr(source.getCTTbl().getTblPr());
        target.getCTTbl().setTblGrid(source.getCTTbl().getTblGrid());
        for (XWPFTableRow row : source.getRows()) {
            XWPFTableRow newRow = target.createRow();
            for (XWPFTableCell cell : row.getTableCells()) {
                XWPFTableCell newCell = newRow.getCell(row.getTableCells().indexOf(cell));
                copyTableCell(cell, newCell);

            }
        }
    }

    private static void copyTableCell(XWPFTableCell source, XWPFTableCell target) {
        target.getCTTc().setTcPr(source.getCTTc().getTcPr());
        for (XWPFParagraph sourcePara : source.getParagraphs()) {
            XWPFParagraph targetPara = target.addParagraph();
            copyParagraph(sourcePara, targetPara);
        }
    }

    private void setPageSize(XWPFDocument doc2, String orientation) {
        CTDocument1 newCtDocument = doc2.getDocument();
        CTBody allSection = newCtDocument.getBody();
        CTSectPr sectionSettings = allSection.addNewSectPr();
        CTPageSz newSize = sectionSettings.addNewPgSz();

        if (orientation.equals(ORIENTATION_LANDSCAPE)) {
            newSize.setW(BigInteger.valueOf(15840));
            newSize.setH(BigInteger.valueOf(12240));
        } else {
            newSize.setW(BigInteger.valueOf(12240));
            newSize.setH(BigInteger.valueOf(15840));
        }
    }

    public void createDiplomas(MultipartFile diplomaTemplate, List<DataXlsx> records,
                               int num1_1, int num1_2, int num2_1, int num2_2,
                               int num3_1, int num3_2) throws IOException {
        try (InputStream templateInputStream = diplomaTemplate.getInputStream();
             XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (DataXlsx record : records) {
                for (int i = 0; i < getDataHTML.pageSize; i++) {
                    if (Objects.equals(getDataHTML.teams.get(i), record.getTeam())) {
                        if (getDataHTML.place.get(i) >= num1_1 && getDataHTML.place.get(i) <= num1_2) {
                            fileConversion(document, path + getDataHTML.teams.get(i) + ", degree 1" + formatDocx, record, i, getDataHTML.tasks, 1);
                        } else if (getDataHTML.place.get(i) >= num2_1 && getDataHTML.place.get(i) <= num2_2) {
                            fileConversion(document, path + getDataHTML.teams.get(i) + ", degree 2" + formatDocx, record, i, getDataHTML.tasks, 2);
                        } else if (getDataHTML.place.get(i) >= num3_1 && getDataHTML.place.get(i) <= num3_2) {
                            fileConversion(document, path + getDataHTML.teams.get(i) + ", degree 3" + formatDocx, record, i, getDataHTML.tasks, 3);

                        }
                    }
                }
            }
        } catch (IOException | InvalidFormatException | XmlException e) {
            throw new IOException("Error processing diploma template file: " + e.getMessage(), e);
        }
    }
    public void convertToPdf(String path, String file) {
        Document doc = new Document();
        doc.loadFromFile(path + file);
        doc.saveToFile(path + getFileNameWithoutExtension(file) + formatPdf, FileFormat.PDF);
        Path fileToDelete = Paths.get(path + file);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createOne(MultipartFile diplomaTemplate, List<DataXlsx> records, String team, int degreeNumber) throws IOException {
        try (InputStream templateInputStream = diplomaTemplate.getInputStream();
             XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (DataXlsx record : records) {
                for (int i = 0; i < getDataHTML.pageSize; i++) {
                    if (Objects.equals(team, record.getTeam()) && Objects.equals(team, getDataHTML.teams.get(i)) && Objects.equals(getDataHTML.teams.get(i), record.getTeam())) {
                        fileConversion(document, path + team + ", degree " + degreeNumber + formatDocx, record, i, getDataHTML.tasks, degreeNumber);
                    }
                }

            }
        } catch (IOException e) {
            throw new IOException("Error processing diploma template file: " + e.getMessage(), e);
        } catch (InvalidFormatException | XmlException e) {
            throw new RuntimeException(e);
        }

    }

    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}

