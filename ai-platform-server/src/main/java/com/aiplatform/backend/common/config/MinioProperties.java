package com.aiplatform.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO（S3 兼容）连接与桶配置。
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** 服务地址，含协议与端口，如 {@code http://192.168.0.35:9000} */
    private String endpoint ;

    private String accessKey ;

    private String secretKey;

    private String bucketName;
}
