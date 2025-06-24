package com.example.DiplomaGeneration.repository;

import com.example.DiplomaGeneration.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FileRepository extends JpaRepository<FileEntity, Long> {


}

