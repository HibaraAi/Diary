package cn.snowt.diary.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import lombok.Data;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 11:36
 * @Description: 有趣的统计数据
 */
@Data
public class FunnyInfo implements Serializable {
    /**
     * 程序安装日期
     */
    private Date installDate;

    /**
     * 第一条日记的id
     */
    private Integer firstDiaryId;

    /**
     * 最长的日记id
     */
    private Integer longestDiaryId;

    /**
     * 最长具体多长
     */
    private Integer longestDiaryContentLength;

    /**
     * 最短的日记id
     */
    private Integer shortestDiaryId;

    /**
     * 最短具体多短
     */
    private Integer shortestDiaryContentLength;

    /**
     * 没有携带标签的日记数量
     */
    private Integer noLabelDiaryNum;

    /**
     * 已有标签数
     */
    private Integer totalLabelNum;

    /**
     * 最常使用的标签
     */
    private String mostAppearLabel;

    /**
     * 已经记录的日记数量
     */
    private Integer totalDiaryNum;

    /**
     * 记录最多日记的月份
     */
    private String monthWithTheMostDiary;

    /**
     * 该月具体多少日记
     */
    private Integer monthWithTheMostDiaryCount;

    /**
     * 哪天记录了最多的日记
     */
    private Date dayWithTheMostDiary;

    /**
     * 那天具体多少条日记
     */
    private Integer numOfDayWithTheMostDiary;

    /**
     * 最经常在哪个时间段写日记
     * (哪个小时段，如10:00-10:59用10表示)
     */
    private Integer mostFrequentTimeToKeepDiary;

    /**
     * 每年的日记数量
     */
    private Map<Integer,Integer> numOfDairyPerYear;

    /**
     * 追更过多少次日记
     */
    private Integer commentSum;

    /**
     * 有几条日记是追更过的
     */
    private Integer haveCommentDiarySum;

    /**
     * 追更过最多的日记id
     */
    private Integer mostCommentedDiaryId;

    /**
     * 该日记具体多少条评论
     */
    private Integer mostCommentedDiaryCount;

    /**
     * 已经存了多少张图
     */
    private Integer imageSum;

    /**
     * 最常在什么天气记录日记
     */
    private String mostWeatherInDiary;

    /**
     * 共记录过多少个雨天
     */
    private Integer rainSumInDiary;

    /**
     * 已经记录过多少个纪念日
     */
    private Integer specialDaySum;

    /**
     * 已经写过多少字了(包括评论字数)
     */
    private Long numOfTotalWords;

    /**
     * 扫雷最快通关时间
     */
    private double fastestGame;
}
