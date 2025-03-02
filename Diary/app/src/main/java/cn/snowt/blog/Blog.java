package cn.snowt.blog;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:19.
 * @Description : Blog数据库字段映射
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog extends LitePalSupport implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签，不加密，可搜索
     */
    private String label;

    /**
     * 正文内容，加密，不可搜索
     */
    private String content;

    /**
     * 修改时间
     */
    private Date modifiedDate;

    /**
     * 加密标志
     */
    private Boolean encryption;

    /**
     * myUuid
     */
    private String myUuid;
}
