package net.gaokd.gcloudaipan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.gaokd.gcloudaipan.model.AccountFileDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户文件表 Mapper 接口
 * </p>
 *
 * @author gkd,
 * @since 2025-01-20
 */
public interface AccountFileMapper extends BaseMapper<AccountFileDO> {

    //批量插入
    void insertFileBatch(@Param("newAccountFileDOList") List<AccountFileDO> newAccountFileDOList);
}
