package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.config.AccountConfig;
import net.gaokd.gcloudaipan.controller.req.ShareCancelReq;
import net.gaokd.gcloudaipan.controller.req.ShareCreateReq;
import net.gaokd.gcloudaipan.dto.ShareDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.enums.ShareDayTypeEnum;
import net.gaokd.gcloudaipan.enums.ShareStatusEnum;
import net.gaokd.gcloudaipan.enums.ShareTypeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.ShareFileMapper;
import net.gaokd.gcloudaipan.mapper.ShareMapper;
import net.gaokd.gcloudaipan.model.ShareDO;
import net.gaokd.gcloudaipan.model.ShareFileDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.ShareService;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: ShareServiceImpl
 * @Author: gkd
 * @date: 2025/3/14 11:50
 * @Version: V1.0
 */
@Service
@Slf4j
public class ShareServiceImpl implements ShareService {

    @Resource
    private ShareMapper shareMapper;

    @Resource
    private ShareFileMapper shareFileMapper;

    @Resource
    private AccountFileService accountFileService;

    /**
     * 获取分享列表
     *
     * @param accountId
     * @return
     */
    @Override
    public ShareDTO listShare(Long accountId) {
        ShareDO shareDO = shareMapper.selectOne(new LambdaQueryWrapper<ShareDO>()
                .eq(ShareDO::getAccountId, accountId)
                .orderByDesc(ShareDO::getGmtCreate));
        return SpringBeanUtil.copyProperties(shareDO, ShareDTO.class);
    }

    /**
     * 创建分享
     * 1、检查分享文件的权限
     * 2、生成分享链接和持久化数据库
     * 3、生成分享详情和持久化数据库
     *
     * @param req
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareDTO createShare(ShareCreateReq req) {
        //1、检查分享文件的权限
        List<Long> fileIds = req.getFileIds();
        accountFileService.checkFileIdIllegal(fileIds, req.getAccountId());

        //2、生成分享链接和持久化数据库
        Integer dayType = req.getShareDayType();
        Integer shareDays = ShareDayTypeEnum.fromDayType(dayType);
        Long shareId = IdUtil.getSnowflakeNextId();
        //生成分享链接
        String shareUrl = AccountConfig.PAN_FRONT_DOMAIN + shareId;
        log.info("shareUrl:{}", shareUrl);

        ShareDO shareDO = ShareDO.builder()
                .id(shareId)
                .shareName(req.getShareName())
                .shareType(ShareTypeEnum.valueOf(req.getShareType()).name())
                .shareDay(shareDays)
                .shareDayType(dayType)
                .shareUrl(shareUrl)
                .shareStatus(ShareStatusEnum.USED.name())
                .accountId(req.getAccountId()).build();
        if (ShareDayTypeEnum.PERMANENT.getDayType().equals(dayType)) {
            shareDO.setShareEndTime(Date.from(LocalDate.of(9999, 12, 31)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            shareDO.setShareEndTime(new Date(System.currentTimeMillis() + shareDays * 24 * 60 * 60 * 1000));
        }
        if (ShareTypeEnum.NEED_CODE.name().equals(req.getShareType())) {
            //生成提取码
            String shareCode = RandomStringUtils.randomAlphabetic(6).toUpperCase();
            shareDO.setShareCode(shareCode);
        }
        shareMapper.insert(shareDO);

        //3、生成分享详情和持久化数据库
        List<ShareFileDO> shareFileDOS = new ArrayList<>();
        fileIds.forEach(fileId -> {
            shareFileDOS.add(ShareFileDO.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .shareId(shareId)
                    .accountFileId(fileId)
                    .accountId(req.getAccountId())
                    .build());
        });
        shareFileMapper.insertBatch(shareFileDOS);
        return SpringBeanUtil.copyProperties(shareDO, ShareDTO.class);
    }

    /**
     * 取消分享
     * @param req
     */
    @Override
    public void cancel(ShareCancelReq req) {
        List<ShareDO> shareDOS = shareMapper.selectList(new LambdaQueryWrapper<ShareDO>()
                .in(ShareDO::getId, req.getShareIds())
                .eq(ShareDO::getAccountId, req.getAccountId()));
        if (shareDOS.size() != req.getShareIds().size()){
            throw new BizException(BizCodeEnum.SHARE_CANCEL_ILLEGAL);
        }
        shareMapper.deleteBatchIds(req.getShareIds());
        shareFileMapper.delete(new LambdaQueryWrapper<ShareFileDO>()
                .in(ShareFileDO::getId, req.getShareIds()));

    }
}
