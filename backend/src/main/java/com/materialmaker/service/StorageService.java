package com.materialmaker.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储：所有素材与结果都落在本地目录（可挂载为容器数据卷）。
 */
@Service
public class StorageService {

    private final Path root;
    private final Path uploadsDir;
    private final Path resultsDir;

    public StorageService(@Value("${app.storage.root:./data}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
        this.uploadsDir = this.root.resolve("uploads");
        this.resultsDir = this.root.resolve("results");
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadsDir);
        Files.createDirectories(resultsDir);
    }

    public Path getUploadsDir() {
        return uploadsDir;
    }

    public Path getResultsDir() {
        return resultsDir;
    }

    /** 根据相对存储根的路径（如 uploads/xxx.jpg）解析为绝对路径 */
    public Path resolveUpload(String relativePath) {
        return root.resolve(relativePath).normalize();
    }
}