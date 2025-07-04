package cn.snowt.diary.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.diary.vo.DiaryVoForFunny;

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
     * @param tempVideoSrcList  读取到的视频缓存Src
     * @param quoteDiaryId 被引用日记的uuid
     * @return
     */
    SimpleResult addOneByArgs(
            String diaryContent,
            String labelStr,
            String locationStr,
            String weatherStr,
            List<String> tempImgSrcList,
            Date date,
            ArrayList<String> tempVideoSrcList
            ,String quoteDiaryId);

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
     * 仅包含id，经过处理后的时间Str，截取正文内容Str（大概88个汉字以内），和一张图片src(如果有)
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
     * @param publicKey publicKey 系统中的加密公钥，用于读取的时候验证解密密钥是否正确
     * @param pinKey pinKey 设置的口令
     * @return
     */
    SimpleResult backupDiary(String publicKey,String pinKey);

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
    void addHelpDiary() throws InterruptedException;

    /**
     * 将所有日记以明文的形式导出到txt文本，需验证登录密码
     * @param pinInput 用户输入的登陆密码
     * @return 成功与否都将提示写入result.msg
     */
    SimpleResult outputForTxt(String pinInput);

    /**
     * 时间升序获取指定条数的DiaryVo。
     * 与getDiaryVoList完全一样，应该修改该方法的参数，而不是另起一个方法
     * @param startIndex
     * @param needNum
     * @return
     */
    List<DiaryVo> getDiaryVoListAsc(int startIndex, int needNum);

    /**
     * 展示系统中已有的所有标签，已分割成单个标签
     * 应该使用一张表将所有标签存起来，而不是现在这样直接存字符串，现在找所有标签还得一条一条解析。。。
     * @return
     */
    Set<String> getAllLabels();

    /**
     * 展示系统中已有的所有标签，没有分割的标签
     * 应该使用一张表将所有标签存起来，而不是现在这样直接存字符串，现在找所有标签还得一条一条解析。。。
     * @return
     */
    Set<String> getAllLabelsUnsplit();

    /**
     * 更新日记文本
     * @param diary
     * @return
     */
    SimpleResult updateDiaryContentById(Diary diary);

    /**
     * 更新日记文本
     * @param diary
     * @return
     */
    SimpleResult updateDiaryLabelById(Diary diary);

    /**
     * 获取全部DiaryVoForFunny形式的日记
     * @return
     */
    List<DiaryVoForFunny> getDiaryVoForFunny();

    /**
     * 获取某天的所有日记
     * @param date
     * @return
     */
    List<DiaryVo> getDiaryVoByDate(Date date);

    /**
     * 获取往年今日的日记
     * @param date 需要提供哪日
     * @return 如果没有，则返回一个空的list
     */
    List<DiaryVo> getFormerYear(Date date);

    /**
     * 解密搜索
     * @param searchValue
     * @return diaryVo仅包含id，经过处理后的时间Str，截取正文内容Str，和一张图片src(如果有)
     */
    SimpleResult decodeSearch(String searchValue);

    /**
     * 根据日记id获取该日记的记录时间
     * @param id 日记id
     * @return 如果没有该日记，返回null
     */
    Date getDateById(Integer id);

    /**
     * 检查日记的uuid是否已存在
     * 如果uuid为null或""，视为不存在
     * @param uuid uuid
     * @return true已存在
     */
    Boolean diaryUuidAlreadyInDb(String uuid);

    /**
     * 根据uuid获取主键id
     * @param uuid uuid
     * @return 如果找不到则返回-1
     */
    Integer diaryUuidToId(String uuid);

    /**
     * 自动备份日记
     * @return 如果备份成功，返回true
     */
    boolean autoBackupDiary();

    /**
     * 通过id来确认是否存在这个日记
     * @param id 需要检查的id
     * @return true-存在    false-不存在
     */
    boolean existById(Integer id);

    /**
     * 给定一个资源地址，看看是不是日记的配图
     * @param src 资源地址
     * @return 返回true就是图片资源，返回false只能表示它不是图片，可能是不存在也不能是视频
     */
    boolean isImageSrc(String src);

    /**
     * 由于1.7.1强制使用加密，需要查询数据库中未加密的数据，将他们进行加密加密
     */
    void encodeDataInDB();

}
