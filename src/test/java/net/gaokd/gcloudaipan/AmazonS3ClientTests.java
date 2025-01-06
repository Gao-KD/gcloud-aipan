package net.gaokd.gcloudaipan;

import cn.hutool.core.date.DateUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.config.MinioConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

/**
 * @ClassName: AmazonS3ClientTests
 * @Author: gkd
 * @date: 2025/1/6 14:46
 * @Version: V1.0
 */
@SpringBootTest
@Slf4j
public class AmazonS3ClientTests {

    @Autowired
    private AmazonS3Client amazonS3Client;

    @Autowired
    private MinioConfig minioConfig;


    /**
     * 判断bucket是否存在
     */
    @Test
    public void bucketExists() {
        boolean bucketExists = amazonS3Client.doesBucketExistV2("ai-pan");
        log.info("bucket是否存在{}", bucketExists);
    }

    /**
     * 创建bucket
     */
    @Test
    public void createBucket() {
        Bucket bucket = amazonS3Client.createBucket("ai-pan1");
        log.info("bucker:{}", bucket);
    }

    /**
     * 删除bucket
     */
    @Test
    public void deleteBucket() {
        amazonS3Client.deleteBucket("ai-pan1");
    }

    /**
     * 根据bucket名称获取bucket详情
     */
    @Test
    public void getBucket() {
        String bucketName = "ai-pan";
        Optional<Bucket> optionalBucket = amazonS3Client.listBuckets().stream().filter(bucket -> bucket.getName().equals(bucketName)).findFirst();
        if (optionalBucket.isPresent()) {
            Bucket bucket = optionalBucket.get();
            log.info("bucket name: {}", bucket.getName());
        } else {
            log.info("bucket name: {}", bucketName + "不存在");
        }
    }

    /**
     * 上传单个文件，直接写入文本
     */
    @Test
    public void testUploadFile() {
        String bucketName = "ai-pan";
        String key = "test.txt";
        String content = "hello world";
        amazonS3Client.putObject(bucketName, key, content);
    }

    /**
     * 上传单个文件，直接写入文本
     */
    @Test
    public void testUploadFile2(){
        amazonS3Client.putObject("ai-pan","test2.txt", new File("/Users/gaokd/log.file"));
    }

    /**
     * 上传文件，输入流的方式  带上文件元数据
     */
    @Test
    @SneakyThrows
    public void testUploadFileWithMetadata() {
        try (FileInputStream fileInputStream = new FileInputStream("/Users/gaokd/1.png")) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("image/png");
            amazonS3Client.putObject("ai-pan", "/aa/1.png", fileInputStream, objectMetadata);
        }
    }

    /**
     * 上传文件，输入流的方式  带上文件元数据
     */
    @Test
    @SneakyThrows
    public void testUploadFileWithMetadata2() {
        try (FileInputStream stream = new FileInputStream("/Users/gaokd/1.png");) {
            byte[] bytes = IOUtils.toByteArray(stream);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("image/png");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            // 上传
            amazonS3Client.putObject("ai-pan", "/aa/1.png", byteArrayInputStream, objectMetadata);
        }

    }

    /**
     * 获取文件
     */
    @Test
    @SneakyThrows
    public void testGetFile(){
        try (FileOutputStream outputStream = new FileOutputStream("/Users/gaokd/Desktop/copy1.txt")){
            S3Object s3Object = amazonS3Client.getObject("ai-pan", "test.txt");
            //下载到指定路径
            s3Object.getObjectContent().transferTo(outputStream);
        }
    }

    /**
     * 删除文件
     */
    @Test
    @SneakyThrows
    public void testDeleteFile(){
        amazonS3Client.deleteObject("ai-pan","test2.txt");
    }

    /**
     * 生成访问地址
     */
    @Test
    public void testGetUrl(){
        //预签名过期时间
        long PRE_SIGN_URL_EXPIRE = minioConfig.getPRE_SINGLE_URL_EXPIRE_TIME();
        //计算预签名url的过期日期
        Date expireDate = DateUtil.offsetMillisecond(new Date(), (int) PRE_SIGN_URL_EXPIRE);
        //创建生成预签名url的请求，并设置过期时间和HTTP方法，withMethod是生成URL的访问方式
        GeneratePresignedUrlRequest request= new GeneratePresignedUrlRequest("ai-pan", "/nginx.conf")
                .withExpiration(expireDate)
                //设置get的话，浏览器能访问
                .withMethod(HttpMethod.GET);

        //生成预签名url
        URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
        //输出预签名url
        log.info("预签名:{}", presignedUrl.toString());
    }


}
