package net.gaokd.gcloudaipan.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.annotation.ShareCodeCheck;
import net.gaokd.gcloudaipan.aspect.ShareCodeAspect;
import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.ShareDTO;
import net.gaokd.gcloudaipan.dto.ShareDetailDTO;
import net.gaokd.gcloudaipan.dto.ShareSimpleDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.interceptor.LoginInterceptor;
import net.gaokd.gcloudaipan.service.ShareService;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName: ShareController
 * @Author: gkd
 * @date: 2025/3/14 11:49
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/share/v1")
public class ShareController {


    @Resource
    private ShareService shareService;

    @GetMapping("list")
    public JsonData list() {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        List<ShareDTO> shareDTO = shareService.listShare(accountId);
        return JsonData.buildSuccess(shareDTO);
    }

    @PostMapping("create")
    public JsonData create(@RequestBody ShareCreateReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        ShareDTO shareDTO = shareService.createShare(req);
        return JsonData.buildSuccess(shareDTO);
    }

    /**
     * 取消分享
     */
    @PostMapping("cancel")
    public JsonData cancel(@RequestBody ShareCancelReq req) {
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        shareService.cancel(req);
        return JsonData.buildSuccess();
    }

    /**
     * 查看分享文件
     * 1、如果链接不需要校验码，则一并返回token
     * 2、如果需要校验码，则返回校验码，调用对应校验码接口，再返回token
     */
    @GetMapping("visit")
    public JsonData visit(@RequestParam(value = "shareId") Long shareId){
        ShareSimpleDTO shareSimpleDTO = shareService.visit(shareId);
        return JsonData.buildSuccess(shareSimpleDTO);
    }

    /**
     * 校验分享码，返回临时token
     * @param req
     * @return
     */
    @PostMapping("checkShareCode")
    public JsonData checkShareCode(@RequestBody ShareCheckReq req){
        String shareToken = shareService.checkShareCode(req);
        if (shareToken == null){
            return JsonData.buildResult(BizCodeEnum.SHARE_NOT_EXIST);
        }
        return JsonData.buildSuccess(shareToken);
    }

    /**
     * 查看分享详情接口
     */
    @GetMapping("detail")
    @ShareCodeCheck
    public JsonData detail(){
        ShareDetailDTO ShareDetailDTO = shareService.detail(ShareCodeAspect.get());
        return JsonData.buildSuccess(ShareDetailDTO);
    }

    @PostMapping("list_share_file")
    @ShareCodeCheck
    public JsonData listShareFile(@RequestBody ShareFileQueryReq req){
        req.setShareId(ShareCodeAspect.get());
        List<AccountFileDTO> accountFileDTOS = shareService.listShareFile(req);
        return JsonData.buildSuccess(accountFileDTOS);
    }

    /**
     * 转存文件
     */
    @PostMapping("transfer")
    @ShareCodeCheck
    public JsonData transfer(@RequestBody ShareFileTransferReq req){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        req.setShareId(ShareCodeAspect.get());
        shareService.transfer(req);
        return JsonData.buildSuccess();
    }

}
