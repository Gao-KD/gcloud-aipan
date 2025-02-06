package net.gaokd.gcloudaipan.config;

/**
 * @ClassName: AccountConfig
 * @Author: gkd
 * @date: 2025/2/4 16:37
 * @Version: V1.0
 */

public class AccountConfig {

    // 用户密码加密盐
    public static final String ACCOUNT_SALT = "gaokd_ai_pan_salt";

    // 默认存储空间 100MB(测试)
    public static final Long DEFAULT_STORAGE_SIZE = 1024 * 1024 * 100L;

    //根文件夹名称
    public static final String ROOT_FOLDER_NAME = "全部文件夹";

    //根文件夹id
    public static final Long ROOT_PARENT_ID = 0L;

}
