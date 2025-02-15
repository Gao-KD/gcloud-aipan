package net.gaokd.gcloudaipan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.FileChunkDTO;
import net.gaokd.gcloudaipan.dto.FolderTreeNodeDTO;
import net.gaokd.gcloudaipan.interceptor.LoginInterceptor;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.FileChunkService;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: AccountFileController
 * @Author: gkd
 * @date: 2025/2/7 15:21
 * @Version: V1.0
 */
@RestController
@RequestMapping("/api/file/v1")
@Tag(name = "文件管理", description = "文件和文件夹相关的操作接口")
public class AccountFileController {

    @Resource
    private AccountFileService accountFileService;

    @Resource
    private FileChunkService fileChunkService;

    /**
     * 查询文件列表接口
     */
    @GetMapping("list")
    @Operation(summary = "查询文件列表", description = "根据上级文件夹ID查询文件列表")
    public JsonData list(@RequestParam(value = "parent_id") Long parentId) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<AccountFileDTO> list = accountFileService.listFile(accountId, parentId);
        return JsonData.buildSuccess(list);
    }

    /**
     * 创建文件夹
     */
    @PostMapping("create_folder")
    @Operation(summary = "创建文件夹", description = "创建一个新的文件夹")
    public JsonData createFolder(@RequestBody FolderCreateReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        Long folderId = accountFileService.createFolder(req);
        return JsonData.buildSuccess(folderId);
    }

    /**
     * 文件重命名
     */
    @PostMapping("rename")
    @Operation(summary = "文件重命名", description = "重命名一个文件")
    public JsonData rename(@RequestBody FolderUpdateReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.renameFile(req);
        return JsonData.buildSuccess();
    }

    /**
     * 多层级文件树
     */
    @GetMapping("/folder/tree")
    @Operation(summary = "文件树", description = "获取文件树")
    public JsonData folderTree() {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<FolderTreeNodeDTO> list = accountFileService.folderTree(accountId);
        return JsonData.buildSuccess(list);
    }

    /**
     * 小文件上传
     */
    @PostMapping("upload")
    @Operation(summary = "小文件上传", description = "上传一个小文件")
    public JsonData upload(FileUploadReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.fileUpload(req);
        return JsonData.buildSuccess();
    }

    /**
     * 批量移动
     */
    @PostMapping("move_batch")
    @Operation(summary = "批量移动", description = "批量移动文件")
    public JsonData moveBatch(@RequestBody FileBatchReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.moveBatch(req);
        return JsonData.buildSuccess();
    }

    /**
     * 文件批量删除
     */
    @PostMapping("del_batch")
    @Operation(summary = "批量删除", description = "批量删除文件")
    public JsonData delBatch(@RequestBody FileDelBatchReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        accountFileService.delBatch(req);
        return JsonData.buildSuccess();
    }


    /**
     * 文件批量复制
     *
     * @param req
     * @return
     */
    @PostMapping("/copy_batch")
    @Operation(summary = "批量复制", description = "批量复制文件")
    public JsonData copyBatch(@RequestBody FileBatchReq req) {
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.copyBatch(req);
        return JsonData.buildSuccess();
    }

    /**
     * 文件秒传接口
     */
    @PostMapping("second_upload")
    @Operation(summary = "文件秒传", description = "文件秒传接口")
    public JsonData secondUpload(@RequestBody FileSecondUpLoadReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        Boolean flag = accountFileService.secondUpload(req);
        return JsonData.buildSuccess(flag);
    }

    /**
     * 1-创建文件分片任务
     */
    @PostMapping("init_file_chunk_task")
    @Operation(summary = "创建文件分片任务", description = "创建文件分片任务")
    public JsonData initFileChunkTask(@RequestBody FileChunkInitReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        FileChunkDTO fileChunkDTO = fileChunkService.initFileChunkTask(req);
        return JsonData.buildSuccess(fileChunkDTO);
    }

    /**
     * 2-获取分片上传地址，返回minio临时签名地址
     */
    @GetMapping("/get_file_chunk_upload_url/{identifier}/{partNumber}")
    @Operation(summary = "获取分片上传地址", description = "获取分片上传地址")
    public JsonData getFileChunkUploadUrl(@PathVariable("identifier") String identifier, @PathVariable("partNumber") Integer partNumber) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        String uploadUrl = fileChunkService.genPreSignUploadUrl(accountId, identifier, partNumber);
        return JsonData.buildSuccess(uploadUrl);
    }

    /**
     * 3-合并分片
     */
    @PostMapping("merge_file_chunk")
    @Operation(summary = "合并分片", description = "合并分片")
    public JsonData mergeFileChunk(@RequestBody FileChunkMergeReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        fileChunkService.mergeFileChunk(req);
        return JsonData.buildSuccess();
    }

    /**
     * 查看分片上传进度
     */
    @GetMapping("/get_file_chunk_upload_progress/{identifier}")
    @Operation(summary = "查看分片上传进度", description = "查看分片上传进度")
    public JsonData getFileChunkUploadProgress(@PathVariable("identifier") String identifier) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        FileChunkDTO dto = fileChunkService.getFileChunkUploadProgress(accountId, identifier);
        return JsonData.buildSuccess(dto);
    }
}
