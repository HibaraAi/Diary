package cn.snowt.diary.service;

import java.util.Date;
import java.util.List;

import cn.snowt.diary.entity.SpecialDay;
import cn.snowt.diary.vo.SpecialDayVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-09 22:11:18
 * @Description:
 */
public interface SpecialDayService {
    /**
     * 获取所有已存储的纪念日
     * @return 如果没有，则返回一个空List
     */
    List<SpecialDayVo> getAllDays();

    /**
     * 新增一个纪念日
     * @param specialDay
     * @return 添加成功返回true
     */
    Boolean addOne(SpecialDay specialDay);

    /**
     * 通过id停止一个纪念日的计数
     * @param id
     */
    void stopSpecialDayById(Integer id);

    /**
     * 根据id查询一个SpecialDay
     * @param id
     * @return 如果没有则返回null
     */
    SpecialDay getOneById(Integer id);

    /**
     * 获取已存纪念日数量
     * @return
     */
    Integer getCount();

    /**
     * 今天有纪念日逢整百天/整年吗？，如果有返回String提示。
     * 如“开学的第100天，完成项目的第2年”
     * @return 如果没有则返回""
     */
    String haveSpecialCount();

    /**
     * 更改日期的提醒
     * @param needNotice true-需要提醒
     */
    void changeNoticeState(Integer id,boolean needNotice);

    /**
     * 通过id删除一个纪念日
     * @param id
     */
    void delById(Integer id);

    /**
     * 获取纪念日的原始数据，供备份使用
     * @return
     */
    List<SpecialDay> getAll();

    /**
     * 更换纪念日的结束日期
     * @param id
     * @param date
     */
    void changeEndDate(Integer id, Date date);
}
