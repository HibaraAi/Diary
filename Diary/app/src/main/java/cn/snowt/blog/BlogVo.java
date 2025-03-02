package cn.snowt.blog;

import java.util.List;

import lombok.Data;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-26 09:45.
 * @Description :
 */
@Data
public class BlogVo {
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
    private String Content;
    /**
     * 创建时间
     */
    private String date;
    /**
     * 标签
     */
    private String labelStr;

    /**
     * 图片地址
     */
    private List<String> imgSrc;

    /**
     * 视频地址
     */
    private List<String> videoSrc;
}
