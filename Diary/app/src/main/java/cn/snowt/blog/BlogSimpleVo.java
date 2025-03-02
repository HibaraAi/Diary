package cn.snowt.blog;

import lombok.Data;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-16 19:47.
 * @Description : 用于Activity展示，简略版的，在列表展示时使用
 */
@Data
public class BlogSimpleVo {
    /**
     * id
     */
    private Integer id;
    /**
     * 标题
     */
    private String title;
    /**
     * 简要内容
     */
    private String simpleContent;

    /**
     * 一张图片或视频缩略图，都没有就赋null
     */
    private String mediaSrc;

    /**
     * 创建时间
     */
    private String date;

    /**
     * 标签
     */
    private String labelStr;
}
