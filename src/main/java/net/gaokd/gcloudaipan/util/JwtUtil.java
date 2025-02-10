package net.gaokd.gcloudaipan.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.dto.AccountDTO;

import javax.crypto.SecretKey;
import java.util.Date;

//JWT工具
@Slf4j
public class JwtUtil {

    // JWT的主题
    private static final String LOGIN_SUBJECT = "GKDCLASS";


    /**
     * token有效期1小时
     */
    private static final Long SHARE_TOKEN_EXPIRE = 1000L * 60 * 60L;


    //注意这个密钥长度需要足够长, 推荐：JWT的密钥，从环境变量中获取
    private final static String SECRET_KEY = "gaokd.netGaokd.netGaokd.netGaokd.netGaokd";
    // 签名算法
    private final static SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;
    // 使用密钥
    private final static SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    // token过期时间，7天
    private static final long EXPIRED = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成JWT
     *
     * @param accountDTO 登录账户信息
     * @return 生成的JWT字符串
     * @throws NullPointerException 如果传入的accountDTO为空
     */
    public static String geneLoginJWT(AccountDTO accountDTO) {
        if (accountDTO == null) {
            throw new NullPointerException("对象为空");
        }

        // 创建 JWT token
        String token = Jwts.builder()
                .subject(LOGIN_SUBJECT)
                .claim("accountId", accountDTO.getId())
                .claim("username", accountDTO.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRED))
                .signWith(KEY, ALGORITHM)  // 直接使用KEY即可
                .compact();

        // 添加自定义前缀
        return addPrefix(token);
    }

    /**
     * 校验JWT
     *
     * @param token JWT字符串
     * @return JWT的Claims部分
     * @throws IllegalArgumentException 如果传入的token为空或只包含空白字符
     * @throws RuntimeException         如果JWT签名验证失败、JWT已过期或JWT解密失败
     */
    public static Claims checkLoginJWT(String token) {
        try {
            log.debug("开始校验 JWT: {}", token);
            // 校验 Token 是否为空
            if (token == null || token.trim().isEmpty()) {
                log.error("Token 不能为空");
                throw new IllegalArgumentException("Token 不能为空");
            }
            token = token.trim();
            // 移除前缀
            token = removePrefix(token);
            log.debug("移除前缀后的 Token: {}", token);
            // 解析 JWT
            Claims payload = Jwts.parser()
                    .verifyWith(KEY)  //设置签名的密钥, 使用相同的 KEY
                    .build()
                    .parseSignedClaims(token).getPayload();

            log.info("JWT 解密成功，Claims: {}", payload);
            return payload;
        } catch (IllegalArgumentException e) {
            log.error("JWT 校验失败: {}", e.getMessage(), e);
            throw e;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT 签名验证失败: {}", e.getMessage(), e);
            throw new RuntimeException("JWT 签名验证失败", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT 已过期: {}", e.getMessage(), e);
            throw new RuntimeException("JWT 已过期", e);
        } catch (Exception e) {
            log.error("JWT 解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("JWT 解密失败", e);
        }
    }

    /**
     * 给token添加前缀
     *
     * @param token 原始token字符串
     * @return 添加前缀后的token字符串
     */
    private static String addPrefix(String token) {
        return LOGIN_SUBJECT + token;
    }

    /**
     * 移除token的前缀
     *
     * @param token 带前缀的token字符串
     * @return 移除前缀后的token字符串
     */
    private static String removePrefix(String token) {
        if (token.startsWith(LOGIN_SUBJECT)) {
            return token.replace(LOGIN_SUBJECT, "").trim();
        }
        return token;
    }
}