package net.gaokd.gcloudaipan.controller.req;

import lombok.Data;

/**
 * @ClassName: FolderUpdateReq
 * @Author: gkd
 * @date: 2025/2/8 17:24
 * @Version: V1.0
 */
@Data
public class FolderUpdateReq {

    /**
     * 账号id
     */
    private Long accountId;

    /**
     * 文件id
     */
    private Long fileId;


    /**
     * 新文件名
     */
    private String newFileName;

}
