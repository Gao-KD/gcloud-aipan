package net.gaokd.gcloudaipan.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.controller.req.ShareCancelReq;
import net.gaokd.gcloudaipan.controller.req.ShareCreateReq;
import net.gaokd.gcloudaipan.dto.ShareDTO;
import net.gaokd.gcloudaipan.interceptor.LoginInterceptor;
import net.gaokd.gcloudaipan.service.ShareService;
import net.gaokd.gcloudaipan.util.JsonData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("list")
    public JsonData list(){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        ShareDTO shareDTO = shareService.listShare(accountId);
        return JsonData.buildSuccess(shareDTO);
    }

    @PostMapping("create")
    public JsonData create(@RequestBody ShareCreateReq req){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        ShareDTO shareDTO = shareService.createShare(req);
        return JsonData.buildSuccess(shareDTO);
    }

    /**
     * 取消分享
     */
    @PostMapping("cancel")
    public JsonData cancel(@RequestBody ShareCancelReq req){
        Long accountId = LoginInterceptor.threadLocal.get().getId();
        req.setAccountId(accountId);
        shareService.cancel(req);
        return JsonData.buildSuccess();
    }

}
