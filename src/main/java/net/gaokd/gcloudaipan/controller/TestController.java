package net.gaokd.gcloudaipan.controller;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.config.MinioConfig;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.util.CommonUtil;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @ClassName: TestController
 * @Author: gkd
 * @date: 2025/1/5 23:52
 * @Version: V1.0
 */
@RestController
@RequestMapping("/api/test/v1")
@Slf4j
public class TestController {

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private MinioClient minioClient;

    @PostMapping("/upload")
    public String testUpload(@RequestParam("file") MultipartFile file) {
        String fileName = CommonUtil.getFilePath(file.getOriginalFilename());
        //读取文件流上传到minio里面
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(minioConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            throw new BizException(BizCodeEnum.FILE_REMOTE_UPLOAD_FAILED, e);
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;
    }


}
