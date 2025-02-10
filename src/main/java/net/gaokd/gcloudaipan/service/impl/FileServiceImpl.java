package net.gaokd.gcloudaipan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.gaokd.gcloudaipan.mapper.FileSuffixMapper;
import net.gaokd.gcloudaipan.mapper.FileTypeMapper;
import net.gaokd.gcloudaipan.model.FileSuffixDO;
import net.gaokd.gcloudaipan.model.FileTypeDO;
import net.gaokd.gcloudaipan.service.FileService;
import org.springframework.stereotype.Service;


/**
 * @ClassName: FileServiceImpl
 * @Author: gkd
 * @date: 2025/2/7 15:22
 * @Version: V1.0
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileSuffixMapper fileSuffixMapper;

    @Resource
    private FileTypeMapper fileTypeMapper;

    @Override
    public String fileType(String fileSuffix) {
        FileSuffixDO fileSuffixDO = fileSuffixMapper.selectOne(new LambdaQueryWrapper<FileSuffixDO>()
                .eq(FileSuffixDO::getFileSuffix, fileSuffix));
        if (fileSuffix != null) {
            FileTypeDO fileTypeDO = fileTypeMapper.selectOne(new LambdaQueryWrapper<FileTypeDO>()
                    .eq(FileTypeDO::getFileTypeName, fileSuffixDO.getFileTypeId()));
            if (fileTypeDO != null) {
                return fileTypeDO.getFileTypeName();
            }
        }
        return "其他";
    }
}
