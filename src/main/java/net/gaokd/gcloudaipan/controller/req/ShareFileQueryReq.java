package net.gaokd.gcloudaipan.controller.req;

import lombok.Data;

/**
 * @ClassName: ShareFileQueryReq
 * @Author: gkd
 * @date: 2025/3/17 14:36
 * @Version: V1.0
 */
@Data
public class ShareFileQueryReq {


    private Long shareId;

    /**
     * 进入的目标文件夹
     */
    private Long parentId;


}
