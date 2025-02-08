package net.gaokd.gcloudaipan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.gaokd.gcloudaipan.controller.req.AccountLoginReq;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.interceptor.LoginInterceptor;
import net.gaokd.gcloudaipan.service.AccountService;
import net.gaokd.gcloudaipan.util.JsonData;
import net.gaokd.gcloudaipan.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName: AccountController
 * @Author: gkd
 * @date: 2025/2/4 16:23
 * @Version: V1.0
 */
@RestController
@RequestMapping("/api/account/v1")
@Tag(name = "账户管理", description = "账户相关的操作接口")
public class AccountController {

    @Resource
    private AccountService accountService;

    /**
     * 注册接口
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public JsonData register(@RequestBody AccountRegisterReq req) {
        accountService.register(req);
        return JsonData.buildSuccess();
    }

    /**
     * 头像上传接口
     */
    @PostMapping("/upload_avatar")
    @Operation(summary = "上传头像", description = "上传用户头像接口")
    public JsonData uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = accountService.uploadAvatar(file);
        return JsonData.buildSuccess(url);
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public JsonData login(@RequestBody AccountLoginReq req) {
        AccountDTO dto = accountService.login(req);
        //生成token,前端一般保存在localStorage里面或sessionStorage;
        String token = JwtUtil.geneLoginJWT(dto);
        return JsonData.buildSuccess(token);
    }

    /**
     * 获取用户详情接口
     */
    @GetMapping("/detail")
    @Operation(summary = "获取用户详情", description = "获取当前登录用户详情接口")
    public JsonData detail() {
        AccountDTO accountDTO = accountService.queryDetail(LoginInterceptor.threadLocal.get().getId());
        return JsonData.buildSuccess(accountDTO);
    }

}
