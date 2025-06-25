package com.example.DiplomaGeneration.controller;

import com.example.DiplomaGeneration.entity.FileEntity;
import com.example.DiplomaGeneration.entity.FileType;
import com.example.DiplomaGeneration.entity.User;
import com.example.DiplomaGeneration.repository.FileRepository;
import com.example.DiplomaGeneration.service.FileService;
import com.example.DiplomaGeneration.service.UserService;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class ProfileController {
    private final UserService userService;
    private final FileRepository fileRepository;
    private final FileService fileService;

    public ProfileController(UserService userService, FileRepository fileRepository, FileService fileService) {
        this.userService = userService;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
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
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String email,
            Model model) {
        User user = (User) userService.loadUserByUsername(userDetails.getUsername());
        user.setFullName(fullName);
        user.setEmail(email);
        userService.updateUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        user.getPassword(),
                        user.getAuthorities()
                )
        );
        model.addAttribute("message", "Профиль успешно обновлен");
        return "redirect:/profile";
    }

    @GetMapping("/profile/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam Long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        ByteArrayResource resource = new ByteArrayResource(fileEntity.getContent());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileEntity.getSize())
                .body(resource);
    }

    @GetMapping("/profile/downloadAllFiles")
    public ResponseEntity<Resource> downloadAllFiles(@RequestParam List<Long> fileIds) throws IOException {
        System.out.println("Received fileIds: " + fileIds);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (Long fileId : fileIds) {
                // Получаем FileEntity по его ID
                FileEntity fileEntity = fileRepository.findById(fileId)
                        .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));
                // Добавляем файл в ZIP-архив
                ZipEntry zipEntry = new ZipEntry(fileEntity.getFilename());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(fileEntity.getContent());
                zipOutputStream.closeEntry();
            }
        }
        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diplomas.zip\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(byteArrayOutputStream.size())
                .body(resource);
    }

    public static byte[] convertDocxToPdf(byte[] docxBytes) {
        Document doc = new Document();
        doc.loadFromStream(new ByteArrayInputStream(docxBytes), FileFormat.Docx);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            doc.saveToStream(byteArrayOutputStream, FileFormat.PDF);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @GetMapping("/profile/view")
    public ResponseEntity<Resource> viewFile(@RequestParam Long fileId) {
        FileEntity f = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        byte[] fileContent;
        String contentType;
        if (f.getFilename().toLowerCase().endsWith(".docx") || f.getFilename().toLowerCase().endsWith(".xlsx")) {
            fileContent = convertDocxToPdf(f.getContent());
            contentType = MediaType.APPLICATION_PDF_VALUE;
        } else {
            fileContent = f.getContent();
            contentType = determineContentType(f.getFilename());
        }
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.getFilename() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileContent.length)
                .body(resource);
    }

    private String determineContentType(String filename) {
        if (filename.toLowerCase().endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    @PostMapping("/profile/delete")
    public String deleteFile(@RequestParam Long fileId) {
        fileService.deleteFileById(fileId);
        return "redirect:/profile";
    }
}