package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.ShareCancelReq;
import net.gaokd.gcloudaipan.controller.req.ShareCreateReq;
import net.gaokd.gcloudaipan.dto.ShareDTO;

/**
 * @ClassName: ShareService
 * @Author: gkd
 * @date: 2025/3/14 11:50
 * @Version: V1.0
 */
public interface ShareService {

    /**
     * 获取我的个人分享列表接口
     */
    ShareDTO listShare(Long accountId);

    /**
     * 创建分享接口
     * @param req
     * @return
     */
    ShareDTO createShare(ShareCreateReq req);

    /**
     * 取消分享接口
     * @param req
     */
    void cancel(ShareCancelReq req);
}
