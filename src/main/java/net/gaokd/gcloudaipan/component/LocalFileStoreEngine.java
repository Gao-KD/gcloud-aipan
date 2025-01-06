package net.gaokd.gcloudaipan.component;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: LocalFileStoreEngine
 * @Author: gkd
 * @date: 2025/1/6 17:19
 * @Version: V1.0
 */
public class LocalFileStoreEngine implements StoreEngine{
    @Override
    public boolean bucketExists(String bucketName) {
        return false;
    }

    @Override
    public boolean removeBucket(String bucketName) {
        return false;
    }

    @Override
    public void createBucket(String bucketName) {

    }

    @Override
    public List<Bucket> getAllBucket() {
        return List.of();
    }

    @Override
    public List<S3ObjectSummary> listObjects(String bucketName) {
        return List.of();
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, String localFileName) {
        return false;
    }

    @Override
    public boolean upload(String bucketName, String objectKey, MultipartFile file) {
        return false;
    }

    @Override
    public boolean delete(String bucketName, String objectKey) {
        return false;
    }

    @Override
    public String getDownloadUrl(String bucketName, String remoteFileName, long timeout, TimeUnit unit) {
        return "";
    }

    @Override
    public void download2Response(String bucketName, String objectKey, HttpServletResponse response) {

    }
}
