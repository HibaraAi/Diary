package cn.snowt.diary.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-07 13:31
 * @Description:
 */
@Data
public class SpecialDayVo implements Serializable {
    private Integer id;
    /**
     * 起始日
     */
    private String startDate;
    /**
     * 结束日
     */
    private String endDate;
    private String title;
    /**
     * 备注
     */
    private String remark;
    private String imageSrc;
    /**
     * 起始日到结束日的天数
     */
    private Integer sumDay;
    /**
     * 起始日到结束日的天数(年的形式)
     */
    private String sumDayYear;
    /**
     * 是否已结束纪念日
     */
    private Boolean stop;
    /**
     * 距离现在的天数
     */
    private Integer distanceNow;

    /**
     * 距离现在的天数(年的形式)
     */
    private String distanceNowYear;

    /**
     * 有没有开启提醒
     */
    private Boolean needNotice;
}
