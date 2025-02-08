package net.gaokd.gcloudaipan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FolderTreeNodeDTO
 * @Author: gkd
 * @date: 2025/2/8 18:23
 * @Version: V1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderTreeNodeDTO {

    /**
     * 文件id
     */
    private Long id;
    /**
     * 父文件id
     */
    private Long parentId;

    /**
     * 文件标签
     */
    private String label;

    /**
     * 子节点列表
     */
    private List<FolderTreeNodeDTO> children = new ArrayList<>();
}
