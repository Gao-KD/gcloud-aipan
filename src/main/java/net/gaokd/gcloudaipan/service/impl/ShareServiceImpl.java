package net.gaokd.gcloudaipan.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.config.AccountConfig;
import net.gaokd.gcloudaipan.controller.req.*;
import net.gaokd.gcloudaipan.dto.*;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.enums.ShareDayTypeEnum;
import net.gaokd.gcloudaipan.enums.ShareStatusEnum;
import net.gaokd.gcloudaipan.enums.ShareTypeEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountFileMapper;
import net.gaokd.gcloudaipan.mapper.AccountMapper;
import net.gaokd.gcloudaipan.mapper.ShareFileMapper;
import net.gaokd.gcloudaipan.mapper.ShareMapper;
import net.gaokd.gcloudaipan.model.AccountDO;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import net.gaokd.gcloudaipan.model.ShareDO;
import net.gaokd.gcloudaipan.model.ShareFileDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.service.ShareService;
import net.gaokd.gcloudaipan.util.JwtUtil;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private AccountFileMapper accountFileMapper;

    /**
     * 获取分享列表
     *
     * @param accountId
     * @return
     */
    @Override
    public List<ShareDTO> listShare(Long accountId) {
        List<ShareDO> shareDOS = shareMapper.selectList(new LambdaQueryWrapper<ShareDO>()
                .eq(ShareDO::getAccountId, accountId)
                .orderByDesc(ShareDO::getGmtCreate));
        return SpringBeanUtil.copyProperties(shareDOS, ShareDTO.class);
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
     *
     * @param req
     */
    @Override
    public void cancel(ShareCancelReq req) {
        List<ShareDO> shareDOS = shareMapper.selectList(new LambdaQueryWrapper<ShareDO>()
                .in(ShareDO::getId, req.getShareIds())
                .eq(ShareDO::getAccountId, req.getAccountId()));
        if (shareDOS.size() != req.getShareIds().size()) {
            throw new BizException(BizCodeEnum.SHARE_CANCEL_ILLEGAL);
        }
        shareMapper.deleteBatchIds(req.getShareIds());
        shareFileMapper.delete(new LambdaQueryWrapper<ShareFileDO>()
                .in(ShareFileDO::getShareId, req.getShareIds()));

    }

    /**
     * * 检查分享状态
     * * 查询分享记录实体
     * * 查询分享者信息
     * * 判断是否需要生成校验码,不需要的话可以直接生成分享token
     *
     * @param shareId
     * @return
     */
    @Override
    public ShareSimpleDTO visit(Long shareId) {
        //分享状态
        ShareDO shareDO = checkStatusShare(shareId);
        ShareSimpleDTO shareSimpleDTO = SpringBeanUtil.copyProperties(shareDO, ShareSimpleDTO.class);

        //查询分享者信息
        ShareAccountDTO shareAccountDTO = getShareAccount(shareDO.getAccountId());
        shareSimpleDTO.setShareAccountDTO(shareAccountDTO);

        //判断是否需要校验码
        if (ShareTypeEnum.NO_CODE.name().equals(shareDO.getShareType())) {
            //不需要校验码
            String shareToken = JwtUtil.geneShareJWT(shareDO.getId());
            shareSimpleDTO.setToken(shareToken);
        }
        return shareSimpleDTO;
    }

    /**
     * 校验分享链接
     *
     * @param req
     * @return
     */
    @Override
    public String checkShareCode(ShareCheckReq req) {
        //分享状态
        ShareDO shareDO = checkStatusShare(req.getShareId());
        //优化一下
        if (shareDO.getShareCode().equals(req.getShareCode()) && shareDO.getShareEndTime().getTime() > System.currentTimeMillis()) {
            //生成分享token
            return JwtUtil.geneShareJWT(shareDO.getId());
        } else {
            log.error("分享链接校验失败: {} ", req.getShareId());
            shareDO.setShareStatus(ShareStatusEnum.EXPIRED.name());
            shareMapper.updateById(shareDO);
            throw new BizException(BizCodeEnum.SHARE_EXPIRED);
        }
    }

    /**
     * 查询分享详情
     *
     * @param shareId
     * @return
     */
    @Override
    public ShareDetailDTO detail(Long shareId) {
        //查询分享记录实体
        ShareDO shareDO = checkStatusShare(shareId);
        ShareDetailDTO shareDetailDTO = SpringBeanUtil.copyProperties(shareDO, ShareDetailDTO.class);

        //查询分享文件信息
        List<AccountFileDO> accountFileDOS = getShareFileInfo(shareId);
        List<AccountFileDTO> accountFileDTOS = SpringBeanUtil.copyProperties(accountFileDOS, AccountFileDTO.class);
        shareDetailDTO.setFileDTOList(accountFileDTOS);
        //查询分享者信息
        ShareAccountDTO shareAccount = getShareAccount(shareDO.getAccountId());
        shareDetailDTO.setShareAccountDTO(shareAccount);
        return shareDetailDTO;
    }

    /**
     * 查询分享文件列表
     * * 检查分享链接状态
     * * 查询分享ID是否在分享的文件列表中（需要获取分享文件列表的全部文件夹和子文件夹）
     * * 分组后获取某个文件夹下面所有的子文件夹
     * * 根据父文件夹ID获取子文件夹列表
     *
     * @param req
     * @return
     */
    @Override
    public List<AccountFileDTO> listShareFile(ShareFileQueryReq req) {
        //检查分享链接状态
        ShareDO shareDO = checkStatusShare(req.getShareId());
        //查询分享ID是否在分享的文件列表中（需要获取分享文件列表的全部文件夹和子文件夹）
        List<AccountFileDO> accountFileDOList = checkShareFileOnStatus(shareDO.getId(), List.of(req.getParentId()));
        List<AccountFileDTO> accountFileDTOS = SpringBeanUtil.copyProperties(accountFileDOList, AccountFileDTO.class);
        //按照文件夹分组
        Map<Long, List<AccountFileDTO>> fileListMap = accountFileDTOS.stream().collect(Collectors.groupingBy(AccountFileDTO::getParentId));
        //获取某个文件夹下面所有的子文件夹
        List<AccountFileDTO> childFileList = fileListMap.get(req.getParentId());
        if (CollectionUtil.isEmpty(childFileList)) {
            return List.of();
        }
        return childFileList;
    }

    /**
     * 文件转移
     * * 分享链接是否状态准确
     * * 转存的文件是否是分享链接里面的文件
     * * 目标文件夹是否是当前用户的
     * * 获取转存的文件
     * * 保存需要转存的文件列表（递归子文件）
     * * 同步更新所有文件的accountId为当前用户的id
     * * 计算存储空间大小，检查是否足够
     * * 更新关联对象信息，存储文件映射关系
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(ShareFileTransferReq req) {
        //查询分享链接状态
        ShareDO shareDO = checkStatusShare(req.getShareId());
        //查询分享ID是否在分享的文件列表中
        checkShareFileOnStatus(req.getShareId(), req.getFileIds());
        //目标文件夹是否是当前用户的
        AccountFileDO targetFileDO = accountFileMapper.selectOne(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, req.getAccountId())
                .eq(AccountFileDO::getFileId, req.getParentId()));
        if (targetFileDO == null) {
            log.error("目标文件夹不是当前用户的: {} ", req.getParentId());
            throw new BizException(BizCodeEnum.SHARE_FILE_ILLEGAL);
        }
        //获取转存的文件
        List<AccountFileDO> transferFileList = accountFileMapper.selectBatchIds(req.getFileIds());
        List<AccountFileDO> batchCopyFileList = accountFileService.findBatchCopyFileWithRecur(transferFileList, req.getParentId());
        //同步更新所有文件的accountId
        batchCopyFileList.forEach(accountFileDO -> {
            accountFileDO.setAccountId(req.getAccountId());
        });

        //计算存储空间大小
        if (!accountFileService.checkAndUpdateCapacity(req.getAccountId(), transferFileList.stream()
                .map(accountFileDO -> accountFileDO.getFileSize() == null ? 0 : accountFileDO.getFileSize())
                .mapToLong(Long::valueOf)
                .sum())) {
            throw new BizException(BizCodeEnum.FILE_STORAGE_NOT_ENOUGH);
        }
        ;

        //更新关联对象信息，存储文件映射关系
        accountFileMapper.insertFileBatch(batchCopyFileList);
    }

    private List<AccountFileDO> checkShareFileOnStatus(Long shareId, List<Long> fileIdList) {
        //需要获取分享文件列表的全部文件夹和子文件内容
        List<AccountFileDO> shareFileInfo = getShareFileInfo(shareId);
        List<AccountFileDO> allAccountFileDOList = new ArrayList<>();
        //获取全部文件，递归
        accountFileService.findAllAccountFileDOWithRecur(allAccountFileDOList, shareFileInfo, false);

        if (CollectionUtil.isEmpty(allAccountFileDOList)) {
            return List.of();
        }
        Set<Long> allFileId = allAccountFileDOList.stream().map(AccountFileDO::getFileId).collect(Collectors.toSet());
        if (!allFileId.containsAll(fileIdList)) {
            log.error("分享ID列表中不在分享的文件列表中: {} ", fileIdList);
            throw new BizException(BizCodeEnum.SHARE_FILE_ILLEGAL);
        }
        return allAccountFileDOList;

    }

    /**
     * 查询分享文件信息
     *
     * @param shareId
     * @return
     */
    private List<AccountFileDO> getShareFileInfo(Long shareId) {
        List<Long> shareFileIdList = getShareFileIdList(shareId);

        //查询分享文件信息
        return accountFileMapper.selectBatchIds(shareFileIdList);
    }

    /**
     * 查询分享文件id列表
     *
     * @param shareId
     * @return
     */
    private List<Long> getShareFileIdList(Long shareId) {
        return shareFileMapper.selectList(new LambdaQueryWrapper<ShareFileDO>()
                        .eq(ShareFileDO::getShareId, shareId)
                        .select(ShareFileDO::getAccountFileId))
                .stream()
                .map(ShareFileDO::getAccountFileId)
                .toList();
    }

    /**
     * 查询分享者信息
     *
     * @param accountId
     * @return
     */
    private ShareAccountDTO getShareAccount(Long accountId) {
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getId, accountId));
        return SpringBeanUtil.copyProperties(accountDO, ShareAccountDTO.class);
    }

    /**
     * 分享状态
     *
     * @param shareId
     * @return
     */
    private ShareDO checkStatusShare(Long shareId) {
        ShareDO shareDO = shareMapper.selectById(shareId);
        if (shareDO == null) {
            log.error("分享链接不存在: {} ", shareId);
            throw new BizException(BizCodeEnum.SHARE_NOT_EXIST);
        }
        if (shareDO.getShareEndTime().before(new Date())) {
            log.error("分享链接已过期: {} ", shareId);
            shareDO.setShareStatus(ShareStatusEnum.EXPIRED.name());
        }
        if (ShareStatusEnum.CANCELED.name().equals(shareDO.getShareStatus())) {
            log.error("分享链接已取消: {} ", shareId);
            throw new BizException(BizCodeEnum.SHARE_CANCELED);
        }
        return shareDO;
    }
}
