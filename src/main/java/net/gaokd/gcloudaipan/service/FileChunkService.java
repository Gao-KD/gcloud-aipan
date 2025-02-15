package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.FileChunkInitReq;
import net.gaokd.gcloudaipan.controller.req.FileChunkMergeReq;
import net.gaokd.gcloudaipan.dto.FileChunkDTO;

import java.util.Map;

/**
 * @ClassName: FileChunkService
 * @Author: gkd
 * @date: 2025/2/15 16:56
 * @Version: V1.0
 */
public interface FileChunkService {
    /**
     * 初始化文件分片接口
     * @param req
     * @return
     */
    FileChunkDTO initFileChunkTask(FileChunkInitReq req);

    /**
     * 获取临时文件上传地址
     * @param accountId
     * @param identifier
     * @param partNumber
     * @return
     */
    String genPreSignUploadUrl(Long accountId, String identifier, Integer partNumber);

    /**
     * 合并文件分片接口
     * @param req
     */
    void mergeFileChunk(FileChunkMergeReq req);

    /**
     * 获取分片上传进度
     * @param accountId
     * @param identifier
     */
    FileChunkDTO getFileChunkUploadProgress(Long accountId, String identifier);
}
