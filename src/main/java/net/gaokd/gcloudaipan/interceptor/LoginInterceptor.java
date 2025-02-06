package net.gaokd.gcloudaipan.interceptor;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.gaokd.gcloudaipan.dto.AccountDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.util.CommonUtil;
import net.gaokd.gcloudaipan.util.JsonData;
import net.gaokd.gcloudaipan.util.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName: LoginInterceptor
 * @Author: gkd
 * @date: 2025/2/6 02:52
 * @Version: V1.0
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<AccountDTO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS请求
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)){
            token = request.getParameter("token");
        }
        if (!token.isEmpty()){
            Claims claims = JwtUtil.checkLoginJWT(token);
            if (claims == null) {
                // 如果token无效，返回未登录的错误信息
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            // 从JWT中提取用户信息
            Long accountId = Long.valueOf( claims.get("accountId")+"");
            String userName = (String) claims.get("username");
            // 创建 AccountDTO 对象
            AccountDTO accountDTO = AccountDTO.builder()
                    .id(accountId)
                    .username(userName)
                    .build();


            // 将用户信息存入 ThreadLocal
            threadLocal.set(accountDTO);
            return true;
        }
        // 如果没有token，返回未登录的错误信息
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal 中的用户信息
        threadLocal.remove();
    }
}
