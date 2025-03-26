package net.gaokd.gcloudaipan.aspect;

import io.jsonwebtoken.Claims;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.annotation.ShareCodeCheck;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @ClassName: ShareCodeAspect
 * @Author: gkd
 * @date: 2025/3/17 14:18
 * @Version: V1.0
 */
@Aspect
@Component
@Slf4j
public class ShareCodeAspect {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程共享shareId
     *
     * @param shareId
     */
    public static void set(Long shareId) {
        threadLocal.set(shareId);
    }

    /**
     * 获取当前线程共享shareId
     *
     * @return
     */
    public static Long get() {
        return threadLocal.get();
    }

    @Pointcut("@annotation(shareCodeCheck)")
    public void pointcut(ShareCodeCheck shareCodeCheck) {
        log.info("shareCodeAspect pointcut");

    }

    @Around("pointcut(shareCodeCheck)")
    public Object around(ProceedingJoinPoint joinPoint, ShareCodeCheck shareCodeCheck) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String shareToken = request.getHeader("share-token");
        if (StringUtils.isBlank(shareToken)) {
            throw new BizException(BizCodeEnum.SHARE_CODE_ILLEGAL);
        }
        Claims claims = JwtUtil.checkShareJWT(shareToken);
        if (claims == null) {
            log.error("share-token 解析失败");
            throw new BizException(BizCodeEnum.SHARE_CODE_ILLEGAL);
        }
        Long shareId = Long.valueOf(claims.get(JwtUtil.CLAIM_SHARE_KEY) + "");
        set(shareId);
        log.info("环绕通知执行前");
        Object obj = joinPoint.proceed();
        log.info("环绕通知执行后");
        return obj;
    }

}
