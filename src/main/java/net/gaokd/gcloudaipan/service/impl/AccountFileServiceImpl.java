package net.gaokd.gcloudaipan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.controller.req.FolderCreateReq;
import net.gaokd.gcloudaipan.dto.AccountFileDTO;
import net.gaokd.gcloudaipan.enums.BizCodeEnum;
import net.gaokd.gcloudaipan.enums.FolderFlagEnum;
import net.gaokd.gcloudaipan.exception.BizException;
import net.gaokd.gcloudaipan.mapper.AccountFileMapper;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import net.gaokd.gcloudaipan.service.AccountFileService;
import net.gaokd.gcloudaipan.util.SpringBeanUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: AccountFileServiceImpl
 * @Author: gkd
 * @date: 2025/2/6 00:27
 * @Version: V1.0
 */
@Service
@Slf4j
public class AccountFileServiceImpl implements AccountFileService {

    @Resource
    private AccountFileMapper accountFileMapper;

    /**
     * 查询文件列表
     *
     * @param accountId
     * @param parentId
     * @return
     */
    @Override
    public List<AccountFileDTO> listFile(Long accountId, Long parentId) {
        List<AccountFileDO> accountFileDOList = accountFileMapper.selectList(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountId)
                .eq(AccountFileDO::getId, parentId)
                .orderByDesc(AccountFileDO::getIsDir, AccountFileDO::getGmtCreate));
        return SpringBeanUtil.copyProperties(accountFileDOList, AccountFileDTO.class);
    }

    /**
     * 创建文件夹
     *
     * @param req
     */
    @Override
    public Long createFolder(FolderCreateReq req) {
        AccountFileDTO accountFileDTO = AccountFileDTO.builder()
                .accountId(req.getAccountId())
                .parentId(req.getParentId())
                .fileName(req.getFolderName())
                .isDir(FolderFlagEnum.YES.getCode())
                .build();
        return saveAccountFile(accountFileDTO);
    }

    /**
     * 处理用户和文件的关系，存储文件和文件夹都是可以的
     *
     * @param accountFileDTO
     * @return
     */
    private Long saveAccountFile(AccountFileDTO accountFileDTO) {
        //查询父文件夹是否存在
        checkParentFileId(accountFileDTO);

        AccountFileDO accountFileDO = SpringBeanUtil.copyProperties(accountFileDTO, AccountFileDO.class);

        //查询文件夹名是否存在
        processFileNameDuplicate(accountFileDO);
        //保存相关文件
        accountFileMapper.insert(accountFileDO);
        return accountFileDO.getId();
    }

    /**
     * 查询父文件夹是否存在
     *
     * @param accountFileDTO
     */
    private void checkParentFileId(AccountFileDTO accountFileDTO) {
        if (accountFileDTO.getParentId() != null) {
            AccountFileDO accountFileDO = accountFileMapper.selectOne(new LambdaQueryWrapper<AccountFileDO>()
                    .eq(AccountFileDO::getAccountId, accountFileDTO.getAccountId())
                    .eq(AccountFileDO::getId, accountFileDTO.getParentId()));
            if (accountFileDO == null) {
                throw new BizException(BizCodeEnum.FILE_NOT_EXISTS);
            }
        }
    }

    /**
     * 处理文件是否重复
     * 文件夹重复和文件名重复处理规则不一样
     *
     * @param accountFileDO
     */
    private void processFileNameDuplicate(AccountFileDO accountFileDO) {
        Long count = accountFileMapper.selectCount(new LambdaQueryWrapper<AccountFileDO>()
                .eq(AccountFileDO::getAccountId, accountFileDO.getAccountId())
                .eq(AccountFileDO::getParentId, accountFileDO.getParentId())
                .eq(AccountFileDO::getFileName, accountFileDO.getFileName())
                .eq(AccountFileDO::getIsDir, accountFileDO.getIsDir()));
        if (count > 0) {
            //处理重复文件夹
            if (accountFileDO.getIsDir().equals(FolderFlagEnum.YES.getCode())) {
                accountFileDO.setFileName(accountFileDO.getFileName() + "(1)");
            } else {
                //处理重复文件名，提取文件扩展名
                String[] split = accountFileDO.getFileName().split("\\.");
                accountFileDO.setFileName(split[0] + "(1)" + split[1]);
            }
        }
    }
}
