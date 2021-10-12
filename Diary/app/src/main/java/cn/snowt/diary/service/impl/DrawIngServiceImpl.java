package cn.snowt.diary.service.impl;

import org.litepal.LitePal;

import java.util.List;

import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.service.DrawingService;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 18:15
 * @Description:
 */
public class DrawIngServiceImpl implements DrawingService {

    @Override
    public List<Drawing> getDrawingsByDiaryId(Integer diaryId) {
        return LitePal.where("diaryId = ?",diaryId+"").find(Drawing.class);
    }
}
