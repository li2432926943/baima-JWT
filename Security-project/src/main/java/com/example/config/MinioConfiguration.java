package com.example.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfiguration {

    @Value("${spring.minio.endpoint}")
    String endpoint;
    @Value("${spring.minio.username}")
    String username;
    @Value("${spring.minio.password}")
    String password;

    @Bean
    public MinioClient minioClient(){
        log.info("Init minio client...");
        log.info("MinIO配置 - endpoint: {}, username: {}", endpoint, username);
        
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();
        
        // 测试连接并创建桶
        try {
            log.info("测试MinIO连接...");
            boolean bucketExists = client.bucketExists(io.minio.BucketExistsArgs.builder().bucket("study").build());
            if (!bucketExists) {
                log.info("创建study桶...");
                client.makeBucket(io.minio.MakeBucketArgs.builder().bucket("study").build());
                log.info("study桶创建成功");
            } else {
                log.info("study桶已存在");
            }
        } catch (Exception e) {
            log.error("MinIO初始化失败: ", e);
            log.error("请检查MinIO服务是否运行在: {}", endpoint);
        }
        
        return client;
    }
}
