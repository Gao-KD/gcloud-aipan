package net.gaokd.gcloudaipan.controller.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: FolderCreateReq
 * @Author: gkd
 * @date: 2025/2/6 00:19
 * @Version: V1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderCreateReq {

    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 上级文件夹id
     */
    private Long parentId;

    /**
     * 用户id
     */
    private Long accountId;
}
