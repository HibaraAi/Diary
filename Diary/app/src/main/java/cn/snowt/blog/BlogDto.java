package cn.snowt.blog;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2024-12-07 14:21.
 * @Description : Blog写入数据库需要的入参字段
 */
@Data
public class BlogDto {
    /**
     * 标题
     */
    private String title;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 修改时间
     */
    private Date modifiedDate;

    /**
     * 被选中的标签
     */
    private String labelStr;

    /**
     * 配图地址
     */
    private List<String> imgSrcList;

    /**
     * 视频地址
     */
    private List<String> videoSrcList;

}
