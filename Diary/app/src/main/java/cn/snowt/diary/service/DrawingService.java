package cn.snowt.diary.service;

import org.litepal.LitePal;

import java.util.List;

import cn.snowt.diary.entity.Drawing;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 18:15
 * @Description:
 */
public interface DrawingService {
    /**
     * 根据日记ID，获取该日记的所有配图
     * @param diaryId 日记Id
     * @return
     */
    List<Drawing> getDrawingsByDiaryId(Integer diaryId);
}
