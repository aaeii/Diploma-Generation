package com.example.DiplomaGeneration.service;

import com.example.DiplomaGeneration.entity.FileEntity;
import com.example.DiplomaGeneration.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public FileEntity getFileById(Long fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }

    public byte[] getFileContentById(Long fileId) {
        Optional<FileEntity> optionalFile = fileRepository.findById(fileId);
        if (optionalFile.isPresent()) {
            return optionalFile.get().getContent();
        } else {
            throw new RuntimeException("Файл не найден");
        }

    }
    public void deleteFileById(Long fileId) {
        if (!fileRepository.existsById(fileId)) {
            throw new RuntimeException("Файл с ID " + fileId + " не найден");
        }
        fileRepository.deleteById(fileId);
    }

}


