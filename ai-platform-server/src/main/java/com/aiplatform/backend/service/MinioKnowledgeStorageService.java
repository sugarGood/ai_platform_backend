package com.aiplatform.backend.service;

import com.aiplatform.backend.common.config.MinioProperties;
import com.aiplatform.backend.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 知识库原始文件写入 MinIO。
 *
 * <p>对象键规则：{@code yyyy/MM/dd/文件名(无扩展名)_毫秒时间戳.扩展名}，便于按日浏览与检索。</p>
 */
@Service
public class MinioKnowledgeStorageService {

    private static final DateTimeFormatter DAY_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final AtomicBoolean bucketEnsured = new AtomicBoolean(false);

    public MinioKnowledgeStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    /**
     * 上传文件，返回对象键（不含桶名），写入 {@code kb_documents.file_path}。
     */
    public String uploadKnowledgeObject(MultipartFile file, String safeFileName) {
        ensureBucketOnce();
        String bucket = properties.getBucketName();

        long ts = System.currentTimeMillis();
        String dayFolder = LocalDate.now().format(DAY_PATH);
        String base = fileBaseWithoutExtension(safeFileName);
        if (base.isBlank()) {
            base = "file";
        }
        String ext = fileExtension(safeFileName);
        String extPart = ext.isEmpty() ? "" : "." + ext;
        String objectName = base + "_" + ts + extPart;
        String objectKey = dayFolder + "/" + objectName;

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new BusinessException(500, "MINIO_UPLOAD_FAILED", "上传 MinIO 失败: " + e.getMessage(), e);
        }
        return objectKey;
    }

    private void ensureBucketOnce() {
        if (!bucketEnsured.compareAndSet(false, true)) {
            return;
        }
        String bucket = properties.getBucketName();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            bucketEnsured.set(false);
            throw new BusinessException(500, "MINIO_BUCKET_FAILED", "MinIO 桶初始化失败: " + e.getMessage(), e);
        }
    }

    private static String fileBaseWithoutExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i <= 0) {
            return filename;
        }
        return filename.substring(0, i);
    }

    private static String fileExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i < 0 || i == filename.length() - 1) {
            return "";
        }
        return filename.substring(i + 1).toLowerCase();
    }
}
