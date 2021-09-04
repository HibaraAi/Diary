package cn.snowt.diary.service.impl;

import android.util.Log;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.Date;
import java.util.List;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-30 17:05
 * @Description:
 */
public class CommentServiceImpl implements CommentService {
    public static final String TAG = "CommentServiceImpl";

    @Override
    public SimpleResult addOneByArgs(String commentStr, Integer diaryId) {
        //判断给日记还存不存在
        List<Diary> diaryList = LitePal.select("id").where("id = ?", diaryId + "").find(Diary.class);
        if(diaryList.size()!=1){
            return SimpleResult.error().msg("该日记貌似不存在了，不能提交评论");
        }
        if(commentStr.length()>2000){
            return SimpleResult.error().msg("你以为出书呢？大于2000字了，禁止保存");
        }
        Comment comment;
        if(MyConfiguration.getInstance().isRequiredAndAbleToEncode()){
            comment = new Comment(null, RSAUtils.encode(commentStr,MyConfiguration.getInstance().getPublicKey()),diaryId,new Date(),true);
        }else{
            comment = new Comment(null, commentStr,diaryId,new Date(),false);
        }
        if (comment.save()) {
            return SimpleResult.ok();
        }else{
            Log.e(TAG,"------保存评论失败，数据库存储错误");
            return SimpleResult.error().msg("保存评论失败，数据库存储错误");
        }
    }

    @Override
    public SimpleResult deleteById(int commentId) {
        Comment comment = LitePal.find(Comment.class, commentId);
        if(null!=comment){
            if(1==comment.delete()){
                return SimpleResult.ok();
            }else{
                return SimpleResult.error().msg("删除失败，请稍后再试");
            }
        }else{
            return SimpleResult.error().msg("不存在该评论，或许已被删除了呢");
        }
    }

    @Override
    public SimpleResult deleteByDiaryId(Integer diaryId) {
        List<Comment> comments = LitePal.where("diaryId = ?", diaryId+"").find(Comment.class);
        if(null!=comments && comments.size()>0){
            comments.forEach(LitePalSupport::delete);
        }
        return SimpleResult.ok();
    }
}
