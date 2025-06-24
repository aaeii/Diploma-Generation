package com.example.DiplomaGeneration.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "longblob")
    private byte[] content;
    @Column(nullable = false)
    private long size;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime dateTime;

    public FileEntity() {
        this.dateTime = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }
    public LocalDateTime getCreatedAt() {
        return dateTime;
    }

    public String getFileFormat() {
        if (filename != null && !filename.isEmpty()) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1);
            }
        }
        return "unknown";
    }

}

