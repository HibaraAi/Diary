package cn.snowt.diary.service;

import java.util.List;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-30 17:04
 * @Description:
 */
public interface CommentService {
    /**
     * 新增一条评论
     * @param commentStr 评论内容
     * @param diaryId 评论哪条日记
     * @return
     */
    SimpleResult addOneByArgs(String commentStr,Integer diaryId);

    /**
     * 根据id删除一条评论
     * @param commentId commentId
     * @return
     */
    SimpleResult deleteById(int commentId);

    /**
     * 根据日记id，删除所有和此日记有关的评论
     * @param diaryId 日记id
     * @return
     */
    SimpleResult deleteByDiaryId(Integer diaryId);

    /**
     * 根据日记ID，获取该日记的所有评论，解密后返回
     * @param diaryId 日记Id
     * @return
     */
    List<Comment> getDecodeCommentByDiaryId(Integer diaryId);
}
