package cn.snowt.diary.service;

import java.util.List;
import java.util.Map;

import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 18:04
 * @Description:
 */
public interface LabelService {
    /**
     * 从一长串标签中解析出标签List
     * #1##2#解析成[#1#,#2#]
     * @param labelStr 原始标签
     * @return 没有解析到就返回null
     */
    List<String> parseLabelFromStr(String labelStr);

    /**
     * 添加相同标签
     * @param sameLabel 相同标签
     * @return
     */
    SimpleResult addSameLabel(String sameLabel);

    /**
     * 获取已有的所有相同标签
     * @return 如果库中没有设置同名标签，返回一个空的map
     */
    Map<Integer,String> getAllSameLabel();

    /**
     * 根据id更新已有的相同标签
     * @param id
     * @param newSameLabel 已经处理好的新标签
     * @return
     */
    SimpleResult updateSameLabel(Integer id,String newSameLabel);

    /**
     * 根据一个已有标签，获取所有同名标签
     * @param label 提供已有标签
     * @return 如果没有同名标签则返回只包含自己的list
     */
    List<String> getSameLabelsByOne(String label);
}
