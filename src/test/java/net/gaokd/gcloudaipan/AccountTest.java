package net.gaokd.gcloudaipan;

import net.gaokd.gcloudaipan.controller.req.AccountLoginReq;
import net.gaokd.gcloudaipan.controller.req.AccountRegisterReq;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.service.AccountService;
import net.gaokd.gcloudaipan.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: AccountTest
 * @Author: gkd
 * @date: 2025/2/6 05:10
 * @Version: V1.0
 */
@SpringBootTest
public class AccountTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void register(){
        AccountRegisterReq req = AccountRegisterReq.builder()
                .username("gaokd")
                .phone("13157175332")
                .password("12345678")
                .build();
        accountService.register(req);
    }
    //测试登录
    @Test
    public void login(){
        AccountLoginReq req = AccountLoginReq.builder()
                .phone("gaokd")
                .password("12345678")
                .build();
        AccountDTO loginDTO = accountService.login(req);
        String token = JwtUtil.geneLoginJWT(loginDTO);
        System.out.println(token);
    }

    @Test
    public void accountDetail(){
        AccountDTO accountDTO = accountService.queryDetail(1887419344087015426L);
        System.out.println(accountDTO);
    }
}
