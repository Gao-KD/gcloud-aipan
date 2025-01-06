package net.gaokd.gcloudaipan.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName: MinioConfig
 * @Author: gkd
 * @date: 2025/1/5 23:47
 * @Version: V1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    @Value("endpoint")
    private String endpoint;

    @Value("accessKey")
    private String accessKey;

    @Value("accessSecret")
    private String accessSecret;

    @Value("bucketName")
    private String bucketName;

    //预签名url的过期时间 ms
    private Long PRE_SINGLE_URL_EXPIRE_TIME = 10 * 60 * 1000L;


    @Bean
    public MinioClient getMinioClient(){
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, accessSecret).build();
    }
}
