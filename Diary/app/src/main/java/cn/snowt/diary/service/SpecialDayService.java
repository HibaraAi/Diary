package cn.snowt.diary.service;

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
}
