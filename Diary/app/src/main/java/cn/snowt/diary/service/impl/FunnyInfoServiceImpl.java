package cn.snowt.diary.service.impl;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSON;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.snowt.diary.entity.FunnyInfo;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.FunnyInfoService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVoForFunny;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 17:20
 * @Description:
 */
public class FunnyInfoServiceImpl implements FunnyInfoService {
    private final DiaryService diaryService = new DiaryServiceImpl();
    public static final String FUNNY_INFO_IN_SP_NAME = "funnyInfo";
    public static final String LAST_FUNNY_INFO_IN_SP_NAME = "lastUpdateFunnyInfo";
    @Override
    public FunnyInfo refreshFunnyInfo() {
        FunnyInfo info = new FunnyInfo();
        //1. 从数据库读取所有数据再分析,这里默认voList不为空，为空的话就不应该调用本方法
        List<DiaryVoForFunny> voList = diaryService.getDiaryVoForFunny();
        //2. 开始分析数据
        //2.1 扫雷
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
//        float mineBestTime = sharedPreference.getFloat("mineBestTime", 3600);
//        BigDecimal bigDecimal = new BigDecimal(String.valueOf(mineBestTime));
//        info.setFastestGame(bigDecimal.doubleValue());
        //2.2 程序安装日期
        try {
            long firstInstallTime = LitePalApplication.getContext().getPackageManager().getPackageInfo(LitePalApplication.getContext().getPackageName(), 0).firstInstallTime;
            info.setInstallDate(new Date(firstInstallTime));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            info.setInstallDate(new Date());
        }
        //2.3 已经存了几个纪念日
        info.setSpecialDaySum(new SpecialDayServiceImpl().getCount());
        //2.4 先处理不需要遍历的
        //2.4.1 已经有多少条日记
        info.setTotalDiaryNum(voList.size());
        //2.4.2 最早一条日记的Id
        info.setFirstDiaryId(voList.get(info.getTotalDiaryNum()-1).getId());
        //2.5 处理需要遍历才能出结果的
        final int[] longestDiaryId = {voList.get(0).getId()}; //最长的日记id
        final int[] longestDiaryContentLength = {voList.get(0).getContent().length()}; //具体多长
        final int[] shortestDiaryId = {voList.get(0).getId()}; //最短的日记id
        final int[] shortestDiaryContentLength = {longestDiaryContentLength[0]};  //具体多短
        final int[] noLabelDiarySum = {0}; //没有标签的日记数量
        Map<String,Integer> labelMap = new HashMap<>(); //用于统计每个标签的出现次数（这里同时包含了一共有多少个标签）
        Map<String,Integer> mostDiaryInMonth = new HashMap<>(); //最多日记的月份（月份使用2021-01这种String作为key）
        Map<String,Integer> mostDiaryInDay = new HashMap<>(); //最多日记的天数
        Map<Integer,Integer> mostDiaryInYear = new HashMap<>(); //最多日记的年数 (年数使用2021这种Integer作为key)
        Map<Integer,Integer>  mostFrequentTimeToKeepDiary = new HashMap<>(); //记录哪个时间段写了最多日记（如果是20：00-20：59的日记，则使用20作为key）
        final int[] commentSum = {0}; //追更过多少次日记
        final int[] haveCommentDiarySum = {0}; //有几条日记是追更过的
        Map<Integer,Integer> mostCommentedDiaryId = new HashMap<>(); //追更过最多的日记id(次数作为key)
        final int[] imageSum = {0}; //已经存了多少张图
        Map<String,Integer> weatherMap = new HashMap<>(); //用于统计哪个天气下存了多少日记（天气作为key）
        final long[] numOfTotalWords = {0}; //已经写过多少字了
        Map<Integer,Integer> diaryWordSegment = new HashMap<>(); //用于统计字数段
        voList.forEach(vo->{
            int contentLength = vo.getContent().length();
            Integer id = vo.getId();
            //----------------此项变成最长------------
            if(contentLength > longestDiaryContentLength[0]){
                longestDiaryId[0] = id;
                longestDiaryContentLength[0] = contentLength;
            }
            //------------------此项变成最短------------------
            if(contentLength < shortestDiaryContentLength[0]){
                shortestDiaryId[0] = id;
                shortestDiaryContentLength[0] = contentLength;
            }
            //----------------更新已经写了多少字(日记正文)------
            numOfTotalWords[0] += contentLength;
            //----------------字数段统计------
            int wordSegment = contentLength/100;
            Integer wordSegmentNum = diaryWordSegment.get((Integer) wordSegment);
            if(null==wordSegmentNum){
                wordSegmentNum = 0;
            }
            wordSegmentNum++;
            diaryWordSegment.put(wordSegment,wordSegmentNum);
            //----------------------评论相关-------------------------
            List<String> commentList = vo.getCommentList();
            if(!commentList.isEmpty()){
                //不为空，属于追更过的日记
                haveCommentDiarySum[0]++;
                mostCommentedDiaryId.put(commentList.size(),id);
                commentList.forEach(comment->{
                    //更新追根过多少次
                    commentSum[0]++;
                    //更新已经写了多少字(评论字数)
                    numOfTotalWords[0] += comment.length();
                });

            }
            //----------------------标签相关-------------------------
            List<String> labelList = vo.getLabelList();
            if(null==labelList){
                //没有设置标签
                noLabelDiarySum[0]++;
            }else{
                //有标签，要分析
                labelList.forEach(label->{
                    Integer integer = labelMap.get(label);
                    if(null==integer){
                        integer = 0;
                    }
                    integer++;
                    labelMap.put(label,integer);
                });
            }
            //--------------------配图数量更新------------------
            imageSum[0] += vo.getPicNum();
            //---------------------天气相关--------------------
            String weatherStr = vo.getWeatherStr();
            if(null!=weatherStr){
                if(weatherStr.contains("雨")){
                    weatherStr = Weather.WEATHER_RAIN;
                }
                Integer integer = weatherMap.get(weatherStr);
                if(null==integer){
                    integer = 0;
                }
                integer++;
                weatherMap.put(weatherStr,integer);
            }
            //------------------日记时间相关--------------
            Date modifiedDate = vo.getModifiedDate();
            String dayStr = dateToDayStr(modifiedDate);
            Integer integer = mostDiaryInDay.get(dayStr);
            if(null==integer){
                integer = 0;
            }
            integer++;
            mostDiaryInDay.put(dayStr,integer);
            String monthStr = dateToMonthStr(modifiedDate);
            Integer integer1 = mostDiaryInMonth.get(monthStr);
            if(integer1==null){
                integer1 = 0;
            }
            integer1++;
            mostDiaryInMonth.put(monthStr,integer1);
            Integer yearInt = dateToYearInt(modifiedDate);
            Integer integer2 = mostDiaryInYear.get((yearInt));
            if(integer2==null){
                integer2 = 0;
            }
            integer2++;
            mostDiaryInYear.put(yearInt,integer2);
            if(!isManMadeDate(modifiedDate)){
                Integer hourInt = dateToHourInt(modifiedDate);
                Integer integer3 = mostFrequentTimeToKeepDiary.get((Integer) hourInt);
                if(integer3==null){
                    integer3 = 0;
                }
                integer3++;
                mostFrequentTimeToKeepDiary.put(hourInt,integer3);
            }
        });

        //-------------------------将结果写入info-------//
        info.setCommentSum(commentSum[0]);
        info.setLongestDiaryId(longestDiaryId[0]);
        info.setLongestDiaryContentLength(longestDiaryContentLength[0]);
        info.setShortestDiaryId(shortestDiaryId[0]);
        info.setShortestDiaryContentLength(shortestDiaryContentLength[0]);
        info.setNoLabelDiaryNum(noLabelDiarySum[0]);
        final int[] labelMaxCount = {1};
        final String[] mostLabel = new String[1];
        final int[] totalLabel = {0};
        labelMap.forEach((s, integer) -> {
            if(integer> labelMaxCount[0]){
                labelMaxCount[0] = integer;
                mostLabel[0] = s;
            }
            totalLabel[0]++;
        });
        info.setTotalLabelNum(totalLabel[0]);
        info.setMostAppearLabel(mostLabel[0]);
        final int[] mostDiaryInMonthCount = {1};
        final String[] mostMonthStr = new String[1];
        mostDiaryInMonth.forEach((s, integer) -> {
            if(integer > mostDiaryInMonthCount[0]){
                mostDiaryInMonthCount[0] = integer;
                mostMonthStr[0] = s;
            }
        });
        info.setMonthWithTheMostDiary(mostMonthStr[0]);
        info.setMonthWithTheMostDiaryCount(mostDiaryInMonthCount[0]);
        final String[] mostDiaryInDayDate = new String[1];
        final Integer[] mostDiaryInDayDateCount = {0};
        mostDiaryInDay.forEach((dateStr, integer) -> {
            if(integer > mostDiaryInDayDateCount[0]){
                mostDiaryInDayDateCount[0] = integer;
                mostDiaryInDayDate[0] = dateStr;
            }
        });
        info.setDayWithTheMostDiary(BaseUtils.stringToDate(mostDiaryInDayDate[0]+" 00:00:00"));
        info.setNumOfDayWithTheMostDiary(mostDiaryInDayDateCount[0]);
        final int[] timeCount = {0};
        final int[] timeHour = new int[1];
        mostFrequentTimeToKeepDiary.forEach((time,count)->{
            if(count> timeCount[0]){
                timeCount[0] = count;
                timeHour[0] = time;
            }
        });
        info.setMostFrequentTimeToKeepDiary(timeHour[0]);
        info.setNumOfDairyPerYear(mostDiaryInYear);
        info.setHaveCommentDiarySum(haveCommentDiarySum[0]);
        final int[] maxCommentCount = {0};
        mostCommentedDiaryId.forEach((count,id)->{
            if(count> maxCommentCount[0]){
                maxCommentCount[0] = count;
            }
        });
        info.setMostCommentedDiaryId(mostCommentedDiaryId.get((Integer)maxCommentCount[0]));
        info.setMostCommentedDiaryCount(maxCommentCount[0]);
        info.setImageSum(imageSum[0]);
        //日记视频的数量
        info.setVideoSum(LitePal.count(Video.class));
        final int[] maxWeatherCount = {0};
        final String[] mostWeatherStr = new String[1];
        weatherMap.forEach((weather,count)->{
            if(count> maxWeatherCount[0]){
                mostWeatherStr[0] = weather;
                maxWeatherCount[0] = count;
            }
        });
        info.setMostWeatherInDiary(mostWeatherStr[0]);
        info.setRainSumInDiary(weatherMap.get(Weather.WEATHER_RAIN));
        info.setNumOfTotalWords(numOfTotalWords[0]);
        //处理字数段相关
        final int[] maxNum = {0};
        final int[] maxSegment = {0};
        diaryWordSegment.forEach((segment, num) -> {
            if(num > maxNum[0]){
                maxNum[0] = num;
                maxSegment[0] = segment;
            }
        });
        info.setMaximumNumOfWords(maxSegment[0]);
        info.setWordSegmentNum(maxNum[0]);
        //-------------------------info写入完成，将info存入sharePerformances-------//
        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
        edit.putString(FUNNY_INFO_IN_SP_NAME,JSON.toJSONString(info));
        edit.putString(LAST_FUNNY_INFO_IN_SP_NAME,BaseUtils.dateToString(new Date()));
        edit.apply();
        return info;
    }

    @Override
    public FunnyInfo getFunnyInfoInSp() {
        String string = BaseUtils.getSharedPreference().getString(FUNNY_INFO_IN_SP_NAME, null);
        if(null==string || "".equals(string)){
            return null;
        }
        return JSON.parseObject(string, FunnyInfo.class);
    }

    /**
     * 获取某个日期的天数
     * @param date
     * @return 如"2021-10-12"
     */
    private String dateToDayStr(Date date){
        return BaseUtils.dateToString(date).substring(0, 10);
    }

    /**
     * 获取某个日记的月份
     * @param date
     * @return 如"2021-10"
     */
    private String dateToMonthStr(Date date){
        return BaseUtils.dateToString(date).substring(0, 7);
    }

    /**
     * 获取某个日期的年份
     * @param date
     * @return 如2021
     */
    private Integer dateToYearInt(Date date){
        return Integer.valueOf(BaseUtils.dateToString(date).substring(0, 4));
    }

    /**
     * 获取某个日期的小时数
     * @return
     */
    private Integer dateToHourInt(Date date){
        return Integer.valueOf(BaseUtils.dateToString(date).substring(11, 13));
    }

    /**
     * 是不是人为修改的日记时间
     * 因为11：12：00是系统自动添加的时间
     * @param date
     * @return
     */
    private boolean isManMadeDate(Date date){
        String substring = BaseUtils.dateToString(date).substring(11, 19);
        return "11:12:00".equals(substring);
    }
}
