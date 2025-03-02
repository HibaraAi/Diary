package cn.snowt.blog;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author: HibaraAi
 * @Date: 2025-02-15 17:10
 * @Description: Blog媒体资源的数据库映射
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogMedia extends LitePalSupport implements Serializable {
    public static final Integer TYPE_IMAGE = 1;
    public static final Integer TYPE_VIDEO = 2;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 媒体资源的地址
     */
    private String mediaSrc;

    /**
     * 媒体类型
     */
    private Integer mediaType;

    /**
     * 外键,BlogId
     */
    private Integer blogId;
}
