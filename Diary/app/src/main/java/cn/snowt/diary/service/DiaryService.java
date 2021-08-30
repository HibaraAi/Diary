package cn.snowt.diary.service;

import java.util.List;

import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:57
 * @Description:
 */
public interface DiaryService {
    /**
     * 获取指定条数的DiaryVo
     * @param startIndex
     * @param needNum
     * @return
     */
    List<DiaryVo> getDiaryVoList(int startIndex, int needNum);

    /**
     * 通过指定参数添加一条新日记
     * @param diaryContent 日记内容
     * @param labelStr 日记标签
     * @param locationStr 位置Str
     * @param weatherStr 天气Str
     * @param tempImgSrcList 读取到的图片缓存Src
     */
    SimpleResult addOneByArgs(String diaryContent, String labelStr, String locationStr, String weatherStr, List<String> tempImgSrcList);

    /**
     * 通过id删除一条日记，级联删除其他相关内容
     * @param diaryId diaryId
     * @return
     */
    SimpleResult deleteById(Integer diaryId);
}
