package net.gaokd.gcloudaipan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @ClassName: ShareCodeCheck
 * @Author: gkd
 * @date: 2025/3/17 14:16
 * @Version: V1.0
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ShareCodeCheck {
}
