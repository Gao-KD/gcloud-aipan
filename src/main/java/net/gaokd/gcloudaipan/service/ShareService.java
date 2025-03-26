package net.gaokd.gcloudaipan.service;

import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.dto.ShareDTO;
import net.gaokd.gcloudaipan.dto.ShareDetailDTO;
import net.gaokd.gcloudaipan.dto.ShareSimpleDTO;

import java.util.List;

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
    List<ShareDTO> listShare(Long accountId);

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

    /**
     * 查看分享文件
     * @param shareId
     * @return
     */
    ShareSimpleDTO visit(Long shareId);

    /**
     * 校验分享提取码
     * @param req
     * @return
     */
    String checkShareCode(ShareCheckReq req);

    /**
     * 分享详情
     * @param shareId
     * @return
     */
    ShareDetailDTO detail(Long shareId);

    /**
     * 分享文件列表
     * @param req
     * @return
     */
    List<AccountFileDTO> listShareFile(ShareFileQueryReq req);

    /**
     * 转存文件
     * @param req
     */
    void transfer(ShareFileTransferReq req);
}
