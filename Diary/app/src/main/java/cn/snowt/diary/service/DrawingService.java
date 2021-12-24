package cn.snowt.diary.service;

import java.util.List;
import java.util.Map;

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

    /**
     * 删除在外部存储中无用的图片(数据库中已经不再使用，但外部存储中还存着的)
     */
    void delUselessPic();

    /**
     * 获取所有图片及其对应日记id,相同日记的放在一组，同时过滤掉失效图片
     * @return
     */
    Map<Integer ,List<Drawing>> getAllPic();

    /**
     * 根据图片地址逆推日记id
     * @param imageSrc
     * @return 如果没有则返回-1
     */
    Integer getDiaryIdByPicSre(String imageSrc);
}
