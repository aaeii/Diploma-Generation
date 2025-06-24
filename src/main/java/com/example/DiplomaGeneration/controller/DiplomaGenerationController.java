package com.example.DiplomaGeneration.controller;

import com.example.DiplomaGeneration.entity.FileEntity;
import com.example.DiplomaGeneration.entity.FileType;
import com.example.DiplomaGeneration.entity.User;
import com.example.DiplomaGeneration.repository.FileRepository;
import com.example.DiplomaGeneration.repository.UserRepository;
import com.example.DiplomaGeneration.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.mock.web.MockMultipartFile;

@Controller
@RequestMapping("/")
public class DiplomaGenerationController {
    private final DataXlsx dataXlsx;
    private final AddToDocx addToDocx;
    private final GetDataHTML getDataHTML;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final UserService userService;

    @Autowired
    public DiplomaGenerationController(DataXlsx dataXlsx, AddToDocx addToDocx, GetDataHTML getDataHTML, FileService fileService, UserRepository userRepository, FileRepository fileRepository, UserService userService) {
        this.dataXlsx = dataXlsx;
        this.addToDocx = addToDocx;
        this.getDataHTML = getDataHTML;
        this.fileService = fileService;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.userService = userService;
    }


    @GetMapping("/")
    public String showIndexPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        List<FileEntity> userFiles = user.getFiles().stream()
                .sorted(Comparator.comparing(FileEntity::getId))
                .filter(file -> file.getType() == FileType.DIPLOMA)
                .collect(Collectors.toList());

        List<FileEntity> userTemplates = user.getFiles().stream()
                .sorted(Comparator.comparing(FileEntity::getId))
                .filter(file -> file.getType() == FileType.TEMPLATE)
                .collect(Collectors.toList());

        List<FileEntity> userDataFiles = user.getFiles().stream()
                .sorted(Comparator.comparing(FileEntity::getId))
                .filter(file -> file.getType() == FileType.PARTICIPANT_DATA)
                .collect(Collectors.toList());
        model.addAttribute("userFiles", userFiles);
        model.addAttribute("userDataFiles", userDataFiles);
        model.addAttribute("userTemplates", userTemplates);
        return "index";
    }

    @PostMapping("/uploadExcel")
    public ResponseEntity<List<String>> uploadExcel(@RequestParam(required = false) MultipartFile dataAboutParticipantsFile,
                                                    @RequestParam(required = false) Long existingDataFileId) {
        try {
            List<String> results;
            if (existingDataFileId != null) {
                dataAboutParticipantsFile = convertToMultipartFile(existingDataFileId);
            }
            results = dataXlsx.processExcel(dataAboutParticipantsFile);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList("Ошибка обработки файла: " + e.getMessage()));
        }
    }

    @PostMapping("/submitData")
    public ResponseEntity<Resource> submitData(
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "htmlFile", required = false) MultipartFile htmlFile,
            @RequestParam(value = "diplomaTypeAllTeams", required = false) Boolean diplomaTypeAllTeams,
            @RequestParam(value = "diplomaTypeSelectedTeam", required = false) Boolean diplomaTypeSelectedTeam,
            @RequestParam(value = "num1_1", required = false) Integer num1_1,
            @RequestParam(value = "num1_2", required = false) Integer num1_2,
            @RequestParam(value = "num2_1", required = false) Integer num2_1,
            @RequestParam(value = "num2_2", required = false) Integer num2_2,
            @RequestParam(value = "num3_1", required = false) Integer num3_1,
            @RequestParam(value = "num3_2", required = false) Integer num3_2,
            @RequestParam(value = "dataAboutParticipantsFile", required = false) MultipartFile dataAboutParticipantsFile,
            @RequestParam(value = "existingDataFileId", required = false) Long existingDataFileId,
            @RequestParam(value = "saveDataFile", required = false, defaultValue = "false") boolean saveDataFile,
            @RequestParam(value = "diplomaTemplateFile", required = false) MultipartFile diplomaTemplateFile,
            @RequestParam(value = "existingFileId", required = false) Long existingFileId,
            @RequestParam(value = "saveDiplomaFile", required = false, defaultValue = "false") boolean saveDiplomaFile,
            @RequestParam("fileFormat") String fileFormat,
            @RequestParam(value = "nameTeam", required = false) List<String> nameTeamList,
            @RequestParam(value = "degreeNumber", required = false) List<Integer> degreeNumberList,
            @RequestParam List<Integer> data) throws IOException {
        Integer[] nums = new Integer[]{num1_1, num1_2, num2_1, num2_2, num3_1, num3_2};
        checkEmpty(nums);
        num1_1 = nums[0];
        num1_2 = nums[1];
        num2_1 = nums[2];
        num2_2 = nums[3];
        num3_1 = nums[4];
        num3_2 = nums[5];
        getDataHTML.place.clear();
        getDataHTML.tasks.clear();
        getDataHTML.teams.clear();
        getDataHTML.pageSize = 0;
        String err = getDataHTML.getData(url, htmlFile);
        if (err != null) {
            return ResponseEntity.ok().body(new ByteArrayResource(err.getBytes()));
        }
        dataAboutParticipantsFile = addDataFile(dataAboutParticipantsFile, existingDataFileId, saveDataFile);
        diplomaTemplateFile = addTemplate(diplomaTemplateFile, existingFileId, saveDiplomaFile);
        try {
            List<DataXlsx> records = new ArrayList<>();
            dataXlsx.readingXlsx(dataAboutParticipantsFile, records, data);
            generateDiplomas(diplomaTypeAllTeams, diplomaTypeSelectedTeam, diplomaTemplateFile, nameTeamList, degreeNumberList, records, num1_1, num1_2, num2_1, num2_2, num3_1, num3_2);
            if (Objects.equals(fileFormat, "pdf")) {
                convertToPdfAllFiles(addToDocx.path);
            }
            Long userId = userService.getCurrentUserId();
            boolean filesAttached = attachFilesToUser(userId, addToDocx.path);
            if (!filesAttached) {
                return ResponseEntity.ok().body(new ByteArrayResource("Дипломы не были сгенерированы. Проверьте введеные данные".getBytes()));
            }
            deleteAllFilesInDirectory(addToDocx.path);
            return ResponseEntity.ok().body(new ByteArrayResource("Файлы успешно сохранены.".getBytes()));
        } catch (Exception e) {
            throw new IOException("Error processing data: " + e.getMessage(), e);
        }
    }

    private MultipartFile addDataFile(MultipartFile dataAboutParticipantsFile, Long existingDataFileId, boolean saveDataFile) throws IOException {
        if (dataAboutParticipantsFile != null && !dataAboutParticipantsFile.isEmpty() && saveDataFile) {
            Long userId = userService.getCurrentUserId();
            attachTemplateFileToUser(userId, dataAboutParticipantsFile);
        } else if (existingDataFileId != null) {
            dataAboutParticipantsFile = convertToMultipartFile(existingDataFileId);
        }
        return dataAboutParticipantsFile;
    }

    private MultipartFile convertToMultipartFile(Long existingDataFileId) throws IOException {
        byte[] fileContent = fileService.getFileContentById(existingDataFileId);
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        return new MockMultipartFile(
                "dataAboutParticipantsFile",
                "dataAboutParticipantsFile.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                inputStream
        );
    }

    public void attachTemplateFileToUser(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пуст");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        FileEntity newFile = new FileEntity();
        newFile.setFilename(file.getOriginalFilename());
        byte[] fileContent = file.getBytes();
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("Содержимое файла пусто");
        }

        newFile.setContent(fileContent);
        newFile.setSize(file.getSize());
        newFile.setUser(user);
        if (Objects.equals(newFile.getFileFormat(), "docx")) {
            newFile.setType(FileType.TEMPLATE);
        } else {
            newFile.setType(FileType.PARTICIPANT_DATA);
        }
        fileRepository.save(newFile);
        System.out.println("Файл успешно сохранен: " + newFile.getFilename());
    }

    private void checkEmpty(Integer[] nums) {
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == null) {
                nums[i] = 0;
            }
        }
    }

    private boolean attachFilesToUser(Long userId, String directoryPath) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    FileEntity fileEntity = new FileEntity();
                    fileEntity.setFilename(file.getName());
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    fileEntity.setContent(fileContent);
                    fileEntity.setSize(file.length());
                    fileEntity.setUser(user);
                    fileEntity.setType(FileType.DIPLOMA);
                    fileRepository.save(fileEntity);
                }
            }
            return true;
        } else {
            System.out.println("Директория не найдена: " + directoryPath);
            return false;
        }
    }

    private void deleteAllFilesInDirectory(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Ошибка при удалении файла: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    private void convertToPdfAllFiles(String path) throws IOException {
        Path dir = Paths.get(path);
        if (Files.exists(dir)) {
            Files.list(dir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".docx"))
                    .forEach(p -> {
                        try {
                            addToDocx.convertToPdf(path, p.getFileName().toString());
                        } catch (Exception e) {
                            System.err.println("Ошибка при конвертации в PDF: " + e.getMessage());
                        }
                    });
        }
    }

    private void generateDiplomas(Boolean diplomaTypeAllTeams, Boolean diplomaTypeSelectedTeam, MultipartFile diplomaTemplateFile,
                                  List<String> nameTeamList, List<Integer> degreeNumberList, List<DataXlsx> records,
                                  Integer num1_1, Integer num1_2, Integer num2_1,
                                  Integer num2_2, Integer num3_1, Integer num3_2) throws IOException {
        if (diplomaTypeAllTeams) {
            addToDocx.createDiplomas(diplomaTemplateFile, records, num1_1, num1_2, num2_1, num2_2, num3_1, num3_2);

        }
        if (diplomaTypeSelectedTeam) {
            for (int i = 0; i < nameTeamList.size(); i++) {
                String nameTeam = nameTeamList.get(i);
                int degreeNumber = degreeNumberList.get(i);
                addToDocx.createOne(diplomaTemplateFile, records, nameTeam, degreeNumber);
            }
        }
    }

    private MultipartFile addTemplate(MultipartFile diplomaTemplateFile, Long existingFileId, boolean saveDiplomaFile) throws IOException {
        if (diplomaTemplateFile != null && !diplomaTemplateFile.isEmpty() && saveDiplomaFile) {
            Long userId = userService.getCurrentUserId();
            attachTemplateFileToUser(userId, diplomaTemplateFile);
        } else if (existingFileId != null) {
            byte[] fileContent = fileService.getFileContentById(existingFileId);
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            diplomaTemplateFile = new MockMultipartFile(
                    "diplomaTemplateFile",
                    "filename.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    inputStream
            );
        }
        return diplomaTemplateFile;
    }
}