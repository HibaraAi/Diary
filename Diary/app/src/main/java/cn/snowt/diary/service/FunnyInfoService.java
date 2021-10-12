package cn.snowt.diary.service;

import cn.snowt.diary.entity.FunnyInfo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 17:16
 * @Description:
 */
public interface FunnyInfoService {
    /**
     * 刷新统计数据，并保存到sharePerformance
     * @return
     */
    FunnyInfo refreshFunnyInfo();

    /**
     * 从sharePerformance获取已存储的统计数据
     * 如果获取失败，返回null
     * @return
     */
    FunnyInfo getFunnyInfoInSp();
}
