package net.gaokd.gcloudaipan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.FolderTreeNodeDTO;
import net.gaokd.gcloudaipan.interceptor.LoginInterceptor;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public JsonData upload(FileUploadReq req){
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

    @PostMapping("/copy_batch")
    public JsonData copyBatch(@RequestBody FileBatchReq req){
        req.setAccountId(LoginInterceptor.threadLocal.get().getId());
        accountFileService.copyBatch(req);
        return JsonData.buildSuccess();
    }

}
