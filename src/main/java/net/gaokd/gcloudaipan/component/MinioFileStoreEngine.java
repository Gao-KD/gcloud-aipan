package net.gaokd.gcloudaipan.component;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.HttpMethod;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: MinioFileStoreEngine
 * @Author: gkd
 * @date: 2025/1/6 17:20
 * @Version: V1.0
 */
@Slf4j
@Component
public class MinioFileStoreEngine implements StoreEngine {

    @Resource
    private AmazonS3Client amazonS3Client;

    @Override
    public boolean bucketExists(String bucketName) {
        return amazonS3Client.doesBucketExist(bucketName);
    }

    @Override
    public boolean removeBucket(String bucketName) {
        if (bucketExists(bucketName)) {
            amazonS3Client.deleteBucket(bucketName);
            return true;
        }
        return false;

    }

    @Override
    public void createBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            amazonS3Client.createBucket(bucketName);
        } else {
            log.info("bucket已存在");
        }
    }

    @Override
    public List<Bucket> getAllBucket() {
        return amazonS3Client.listBuckets();

    }

    @Override
    public List<S3ObjectSummary> listObjects(String bucketName) {
        if (bucketExists(bucketName)) {
            return amazonS3Client.listObjects(bucketName).getObjectSummaries();
        } else {
            return List.of();
        }
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        if (bucketExists(bucketName)) {
            return amazonS3Client.doesObjectExist(bucketName, objectKey);
        }
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, String localFileName) {
        if (bucketExists(bucketName)) {
            amazonS3Client.putObject(bucketName, objectKey, new File(localFileName));
            return true;
        }
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, MultipartFile file) {
        if (bucketExists(bucketName)) {
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                amazonS3Client.putObject(bucketName, objectKey, file.getInputStream(), metadata);
                return true;
            } catch (Exception e) {
                log.error("上传文件失败", e);
            }
        }
        return false;
    }

    @Override
    public boolean delete(String bucketName, String objectKey) {
        if (bucketExists(bucketName)) {
            amazonS3Client.deleteObject(bucketName, objectKey);
            return true;
        }
        return false;
    }

    @Override
    public String getDownloadUrl(String bucketName, String remoteFileName, long timeout, TimeUnit unit) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + unit.toMillis(timeout));
            return amazonS3Client.generatePresignedUrl(bucketName, remoteFileName, expiration).toString();
        } catch (Exception e) {
            log.error("errorMsg:{}", e.getMessage());
            return null;
        }
    }

    @Override
    @SneakyThrows
    public void download2Response(String bucketName, String objectKey, HttpServletResponse response) {
        S3Object s3Object = amazonS3Client.getObject(bucketName, objectKey);
        response.setHeader("Content-Disposition", "attachment;filename=" + objectKey.substring(objectKey.lastIndexOf("/") + 1));
        response.setContentType("application/force-download");
        response.setCharacterEncoding("UTF-8");
        IOUtils.copy(s3Object.getObjectContent(), response.getOutputStream());
    }

    /**
     * 查询分片数据
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象名称
     * @param uploadId   分片上传ID
     * @return 分片列表对象
     */
    @Override
    public PartListing listMultipart(String bucketName, String objectKey, String uploadId) {
        try {
            ListPartsRequest request = new ListPartsRequest(bucketName, objectKey, uploadId);
            return amazonS3Client.listParts(request);
        } catch (Exception e) {
            log.error("errorMsg:{}", e.getMessage());
            return null;
        }

    }

    /**
     * 1-初始化分片上传任务,获取uploadId,如果初始化时有 uploadId，说明是断点续传，不能重新生成 uploadId
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象名称
     * @param metadata   对象元数据
     * @return 初始化分片上传结果对象，包含uploadId等信息
     */
    @Override
    public InitiateMultipartUploadResult initMultipartUploadTask(String bucketName, String objectKey, ObjectMetadata metadata) {
        InitiateMultipartUploadRequest initiateMultipartUploadRequest = new InitiateMultipartUploadRequest(bucketName, objectKey, metadata);
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3Client.initiateMultipartUpload(initiateMultipartUploadRequest);
        log.info("初始化分片上传:{}", initiateMultipartUploadResult);
        return initiateMultipartUploadResult;
    }

    /**
     * 生成预签名的URL
     * <p>
     * 此方法用于生成一个具有有限有效期的预签名URL，允许在不暴露秘密访问密钥的情况下访问对象
     * 它可以为HTTP GET、PUT等方法生成URL
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键名
     * @param httpMethod HTTP方法，如GET、PUT等
     * @param expiration URL的有效期
     * @param params     可选的请求参数，如响应头设置等
     *                   params:{
     *                   "uploadId":upload,
     *                   "partNumber",String.valueOf(i + 1)
     *                   }
     * @return URL 生成的预签名URL
     */
    @Override
    public URL genePreSignedUrl(String bucketName, String objectKey, HttpMethod httpMethod, Date expiration, Map<String, Object> params) {
        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey)
                    .withExpiration(expiration)
                    .withMethod(httpMethod);
            for (Map.Entry<String, Object> param : params.entrySet()) {
                request.addRequestParameter(param.getKey(), (String) param.getValue());
            }
            URL presignedUrl = amazonS3Client.generatePresignedUrl(request);
            log.info("presignedUrl:{}", presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            log.error("errorMsg:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 合并已上传的分片
     * <p>
     * 此方法用于合并所有已上传的分片，完成多部分上传过程
     * 它会使用上传ID和所有分片的ETag来验证并合并分片
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键名
     * @param uploadId   多部分上传的唯一标识符
     * @param partETags  包含所有已上传分片的ETag和分片编号的列表
     * @return CompleteMultipartUploadResult 包含完成上传后的对象信息的结果对象
     */
    @Override
    public CompleteMultipartUploadResult mergeChunks(String bucketName, String objectKey, String uploadId, List<PartETag> partETags) {
        try {
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest()
                    .withBucketName(bucketName)
                    .withKey(objectKey)
                    .withUploadId(uploadId)
                    .withPartETags(partETags);

            CompleteMultipartUploadResult result = amazonS3Client.completeMultipartUpload(request);
            log.info("result:{}", result);
            return result;
        } catch (Exception e) {
            log.error("errorMsg:{}", e.getMessage());
            return null;
        }
    }
}
