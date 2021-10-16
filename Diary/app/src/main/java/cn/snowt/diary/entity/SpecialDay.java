package cn.snowt.diary.entity;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-06 08:39
 * @Description: 纪念日
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpecialDay extends LitePalSupport implements Serializable {
    private Integer id;
    /**
     * 起始日期
     */
    private Date startDate;

    /**
     * 标题
     */
    private String title;

    /**
     * 停止计时时间
     */
    private Date stopDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 配图路径
     */
    private String imageSrc;

    private Boolean needNotice;
}
