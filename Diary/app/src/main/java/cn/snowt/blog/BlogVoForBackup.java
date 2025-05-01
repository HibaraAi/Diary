package cn.snowt.blog;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class BlogVoForBackup implements Serializable {
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

    /**
     * 媒体资源
     */
    private List<BlogMedia>  blogMediaList;
}
