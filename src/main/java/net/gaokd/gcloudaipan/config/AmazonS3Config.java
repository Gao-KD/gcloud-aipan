package net.gaokd.gcloudaipan.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: AmazonS3Config
 * @Author: gkd
 * @date: 2025/1/6 13:54
 * @Version: V1.0
 */
@Configuration
public class AmazonS3Config {

    @Resource
    private MinioConfig minioConfig;

    //创建并配置s3客户端

    @Bean(name = "amazonS3Client")
    public AmazonS3 getClient() {
        //创建客户端配置
        ClientConfiguration config = new ClientConfiguration();

        //配置协议
        config.setProtocol(Protocol.HTTP);

        //配置超时时间
        config.setConnectionTimeout(5000);
        config.setUseExpectContinue(true);

        //使用minio配置的密钥创建访问凭据
        AWSCredentials awsCredentials = new BasicAWSCredentials(minioConfig.getAccessKey(), minioConfig.getAccessSecret());

        //设置Endpoint
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(minioConfig.getEndpoint(), Regions.US_EAST_1.name());

        //使用上面的配置创建客户端
        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(config)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(endpointConfiguration)
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
