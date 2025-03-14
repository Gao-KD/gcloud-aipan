package net.gaokd.gcloudaipan;

import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.controller.req.FileChunkInitReq;
import net.gaokd.gcloudaipan.controller.req.FileChunkMergeReq;
import net.gaokd.gcloudaipan.dto.FileChunkDTO;
import net.gaokd.gcloudaipan.service.FileChunkService;
import net.gaokd.gcloudaipan.util.JsonUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileChunkUploadTest
 * @Author: gkd
 * @date: 2025/2/18 17:12
 * @Version: V1.0
 */
@Slf4j
@SpringBootTest
public class FileChunkUploadTest {

    @Resource
    private FileChunkService fileChunkService;

    private Long accountId = 1888162507808673794L;

    private String identifier = "123123dsaasd";

    /**
     * 存储分片后端文件路径
     */
    private final List<String> chunkFilePaths = new ArrayList<>();

    /**
     * 存储分片上传的临时签名地址
     */
    private final List<String> chunkUploadUrls = new ArrayList<>();

    /**
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片大小 5MB
     */
    private final int chunkSize = 1024 * 1024 * 5;


    //创建分片
    @Test
    public void testCreateFileChunk() {
        //将文件分片
        String filePath = "/Users/gaokd/Downloads/xdclass_tech.zip";

        File file = new File(filePath);

        long fileSize = file.length();

        //计算分片数量
        int chunkCount = (int) Math.ceil((double) fileSize / chunkSize);

        log.info("分片数量: {} ", chunkCount);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[chunkSize];
            for (int i = 0; i < chunkCount; i++) {
                String chunkFileName = filePath + ".part" + (i + 1);
                try (FileOutputStream fos = new FileOutputStream(chunkFileName)) {
                    int read = fis.read(buffer);
                    fos.write(buffer, 0, read);
                    log.info("创建分片文件: {} , 大小: {} ", chunkFileName, read);
                    chunkFilePaths.add(chunkFileName);
                }
            }
        } catch (Exception e) {
            log.error("上传分片失败", e);
        }
        testInitFileChunk();
    }


    /**
     * 1-创建分片上传任务
     */
    private void testInitFileChunk() {
        FileChunkInitReq req = new FileChunkInitReq();
        req.setAccountId(accountId)
                .setIdentifier(identifier)
                .setChunkSize((long) chunkSize)
                .setFileName("xdclass_tech.zip")
                .setTotalSize(28293450L);
        FileChunkDTO fileChunkDTO = fileChunkService.initFileChunkTask(req);

        log.info("创建分片上传任务成功, 文件标识符: {} , 分片数量: {} ", fileChunkDTO.getIdentifier(), fileChunkDTO.getChunkNum());
        uploadId = fileChunkDTO.getUploadId();

        //获取分片临时上传地址
        testGetFileChunkUploadUrl();
    }

    /**
     * 获取分片临时上传地址
     */
    private void testGetFileChunkUploadUrl() {
        for (int i = 0; i < chunkFilePaths.size(); i++) {
            String uploadUrl = fileChunkService.genPreSignUploadUrl(accountId, identifier, i + 1);
            log.info("获取分片临时上传地址: {} ", uploadUrl);
            // 保存上传地址
            chunkUploadUrls.add(uploadUrl);
        }

        //上传分片文件，模拟前端上传
        uploadChunk();

        //合并
        testMergeFileChunk();
    }

    /**
     * 模拟前端上传分片文件
     */
    @SneakyThrows
    private void uploadChunk() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        for (int i = 0; i < chunkUploadUrls.size(); i++) {
            String uploadUrl = chunkUploadUrls.get(i);
            HttpPut httpPut = new HttpPut(uploadUrl);
            httpPut.setHeader("Content-Type", "application/octet-stream");
            File chunkFile = new File(chunkFilePaths.get(i));
            FileEntity fileEntity = new FileEntity(chunkFile);
            httpPut.setEntity(fileEntity);
            CloseableHttpResponse response = httpClient.execute(httpPut);
            log.info("分片: {} ,上传结果响应: {} ", i + 1, response.getStatusLine());
            httpPut.releaseConnection();
        }
        log.info("分片上传结束");
    }

    /**
     * 合并分片
     */
    @Test
    public void testMergeFileChunk() {
        FileChunkMergeReq req = new FileChunkMergeReq();
        req.setAccountId(accountId).setIdentifier(identifier).setParentId(1888162521310138370L);
        fileChunkService.mergeFileChunk(req);
    }

    /**
     * 查询文件上传进度测试
     */
    @Test
    public void testFileChunkProgress() {
        FileChunkDTO fileChunkUploadProgress = fileChunkService.getFileChunkUploadProgress(accountId, identifier);
        log.info("查询文件上传进度: {} ", JsonUtil.obj2Json(fileChunkUploadProgress));
    }

}
