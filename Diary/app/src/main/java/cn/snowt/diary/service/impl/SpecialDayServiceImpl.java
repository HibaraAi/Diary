package cn.snowt.diary.service.impl;

import android.text.format.DateUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.entity.SpecialDay;
import cn.snowt.diary.service.SpecialDayService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.SpecialDayVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-09 22:11:47
 * @Description:
 */
public class SpecialDayServiceImpl implements SpecialDayService {
    @Override
    public List<SpecialDayVo> getAllDays() {
        List<SpecialDay> specialDays = LitePal.select("*").order("startDate desc").find(SpecialDay.class);
        List<SpecialDayVo> list = new ArrayList<>(specialDays.size());
        specialDays.forEach(specialDay -> {
            SpecialDayVo vo = new SpecialDayVo();
            vo.setId(specialDay.getId());
            vo.setImageSrc(specialDay.getImageSrc());
            vo.setTitle(specialDay.getTitle());
            vo.setRemark(specialDay.getRemark());
            vo.setStop(null != specialDay.getStopDate());
            //计算距今的
            vo.setDistanceNow(twoDatesDistance(specialDay.getStartDate(),new Date()));
            vo.setDistanceNowYear(twoDatesDistanceYearStr(specialDay.getStartDate(),new Date()));
            String startDateStr = BaseUtils.dateToString(specialDay.getStartDate());
            startDateStr = startDateStr.replaceAll("00:00:00 ","");
            vo.setStartDate(startDateStr);
            if(vo.getStop()){
                vo.setSumDay(twoDatesDistance(specialDay.getStartDate(),specialDay.getStopDate()));
                vo.setSumDayYear(twoDatesDistanceYearStr(specialDay.getStartDate(),specialDay.getStopDate()));
                String endDateStr = BaseUtils.dateToString(specialDay.getStopDate());
                String substring = endDateStr.substring(0, 10);
                String substring1 = endDateStr.substring(19, 23);
                endDateStr = substring+substring1;
                vo.setEndDate(endDateStr);
            }else{
                vo.setSumDay(vo.getDistanceNow());
                vo.setSumDayYear(vo.getDistanceNowYear());
                vo.setEndDate("");
            }
            list.add(vo);
        });
        return list;
    }

    @Override
    public Boolean addOne(SpecialDay specialDay) {
        return specialDay.save();
    }

    @Override
    public void stopSpecialDayById(Integer id) {
        SpecialDay specialDay = getOneById(id);
        specialDay.setStopDate(new Date());
        specialDay.update(specialDay.getId());
    }

    @Override
    public SpecialDay getOneById(Integer id) {
        return LitePal.find(SpecialDay.class,id);
    }

    @Override
    public Integer getCount() {
        return LitePal.count(SpecialDay.class);
    }

    /**
     * 计算两个日期的相距天数
     * @param date1
     * @param date2
     * @return 天数，数字形式
     */
    private Integer twoDatesDistance(Date date1, Date date2){
        if(date1.after(date2)){
            Date tempDate;
            tempDate = date1;
            date1 = date2;
            date2 = tempDate;
        }
        return (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * 计算两个日期的相距年数
     * @param date1
     * @param date2
     * @return 年数，已转换为友好一点的String形式
     */
    private String twoDatesDistanceYearStr(Date date1, Date date2){
        //保证date1在前面
        if(date1.after(date2)){
            Date tempDate;
            tempDate = date1;
            date1 = date2;
            date2 = tempDate;
        }
        Integer dayNum = twoDatesDistance(date1, date2);
        //计算中间多少个瑞年
        int count = 0;
        int yearSmall = Integer.parseInt(BaseUtils.dateToString(date1).substring(0,4));
        int yearBig = Integer.parseInt(BaseUtils.dateToString(date2).substring(0,4));
        for(int i= yearSmall;i<=yearBig;i++){
            if(isLeapYear(i)){
                count++;
            }
        }
        //修正闰年个数
        Boolean leapYear = isLeapYear(yearSmall);
        Boolean aBoolean = passLeap(yearSmall, date1);
        if(leapYear && aBoolean){
            count--;
        }
        if(isLeapYear(yearBig) && !passLeap(yearBig,date2)){
            count--;
        }
        int yearNum = dayNum/365;
        int surplusDayNum = dayNum%365;
        surplusDayNum -= count;
        if(yearNum==0){
            if(surplusDayNum==0){
                return "就是今天啦\n虽然我也知道显示0很奇怪>_<";
            }
            return surplusDayNum+"天";
        }
        if(surplusDayNum==0){
            return yearNum+"年整";
        }else{
            return yearNum+"年零"+surplusDayNum+"天";
        }
    }

    /**
     * 判断某年是不是瑞年
     * @param i
     * @return true-是闰年
     */
    private Boolean isLeapYear(int i){
        return i % 4 == 0 && i % 100 != 0 || i % 400 == 0;
    }

    /**
     * 年数为瑞年，进一步判断所给天数是否过了该年的瑞年
     * @param yearNum 该年年数
     * @param detailDate 该年中具体的某天
     * @return true-已经过了
     */
    private Boolean passLeap(int yearNum,Date detailDate){
        Date date = BaseUtils.stringToDate(yearNum + "-02-29 00:00:00");
        assert date != null;
        int i = date.compareTo(detailDate);
        return i <= 0;
    }
}
