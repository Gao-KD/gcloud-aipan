package net.gaokd.gcloudaipan.controller;

import jakarta.annotation.Resource;
import net.gaokd.gcloudaipan.controller.req.AccountLoginReq;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.service.AccountService;
import net.gaokd.gcloudaipan.util.JsonData;
import net.gaokd.gcloudaipan.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName: AccountController
 * @Author: gkd
 * @date: 2025/2/4 16:23
 * @Version: V1.0
 */
@RestController
@RequestMapping("/api/account/v1")
public class AccountController {

    @Resource
    private AccountService accountService;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public JsonData register(@RequestBody AccountRegisterReq req) {
        accountService.register(req);
        return JsonData.buildSuccess();
    }

    /**
     * 头像上传接口
     */
    @PostMapping("/upload_avatar")
    public JsonData uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = accountService.uploadAvatar(file);
        return JsonData.buildSuccess(url);
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public JsonData login(@RequestBody AccountLoginReq req) {
        AccountDTO dto = accountService.login(req);
        //生成token,前端一般保存在localStorage里面或sessionStorage;
        String token = JwtUtil.geneLoginJWT(dto);
        return JsonData.buildSuccess(token);
    }
}
