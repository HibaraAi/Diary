package cn.snowt.diary.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param date 日记时间,如果为null，则自动填写时间
     * @return
     */
    SimpleResult addOneByArgs(String diaryContent, String labelStr, String locationStr, String weatherStr, List<String> tempImgSrcList,Date date);

    /**
     * 通过id删除一条日记，级联删除其他相关内容
     * @param diaryId diaryId
     * @return
     */
    SimpleResult deleteById(Integer diaryId);

    /**
     * 根据给定时间，查找该时间段的日记
     * @param date1 date1
     * @param date2 date2
     * @return 返回的data为diaryVoList,diaryVo
     * 仅包含id，经过处理后的时间Str，截取正文内容Str，和一张图片src(如果有)
     */
    SimpleResult getSimpleDiaryByDate(Date date1, Date date2);

    /**
     * 根据id获取一个详情DiaryVo
     * @param diaryId diaryId
     * @return
     */
    SimpleResult getDiaryVoById(int diaryId);

    /**
     * 搜索日记
     * @param searchValue 搜索值
     * @return 仅返回diaryId集合，如果没有符合的记录返回SimpleResult.error()
     */
    SimpleResult searchDiary(String searchValue);

    /**
     * 根据给定id集合，查找日记
     * @param ids id集合
     * @return diaryVo仅包含id，经过处理后的时间Str，截取正文内容Str，和一张图片src(如果有)
     */
    List<DiaryVo> getSimpleDiaryByIds(ArrayList<Integer> ids);

    /**
     *备份日记
     * @param privateKey privateKey
     * @param publicKey publicKey
     * @param pinKey pinKey
     * @return
     */
    SimpleResult backupDiary(String privateKey,String publicKey,String pinKey);

    /**
     * 从已经读取的备份数据恢复日记
     * @param pinKeyInput 输入的pin口令
     * @param privateKeyInput 输入的私钥
     * @param map 已经读取到的数据
     * @return
     */
    SimpleResult recoveryDiary(String pinKeyInput, String privateKeyInput, Map<String,Object> map);

    /**
     * 根据标签，查找同标签的所有日记
     * @param labelStr 标签
     * @return diaryVo仅包含id，经过处理后的时间Str，截取正文内容Str，和一张图片src(如果有)
     */
    List<DiaryVo> getSimpleDiaryByLabel(String labelStr);

    /**
     * 用于生成第一次使用本程序的帮助日记。
     */
    void addHelpDiary();
}
