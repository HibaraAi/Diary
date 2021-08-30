package cn.snowt.diary.service.impl;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;
import org.litepal.LitePalDB;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Location;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.RSAUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 14:57
 * @Description:
 */
public class DiaryServiceImpl implements DiaryService {
    public static final String TAG = "DiaryServiceImpl";

    private final CommentService commentService = new CommentServiceImpl();

    @Override
    public List<DiaryVo> getDiaryVoList(int startIndex, int needNum) {
        List<DiaryVo> voList = new ArrayList<>();
        //此方法的查询应该更改为连表查询
        //String sql = "SELECT * FROM diary ORDER BY id LIMIt "+startIndex+","+needNum+"";
        List<Diary> all = LitePal.order("id desc").limit(needNum).offset(startIndex).find(Diary.class);
        all.forEach(diary -> {
            DiaryVo vo = new DiaryVo();
            vo.setId(diary.getId());
            //解密
            //vo.setContent(RSAUtils.decode(diary.getContent()));
            vo.setContent(diary.getContent());
            vo.setModifiedDate(BaseUtils.dateToString(diary.getModifiedDate()));
            vo.setLabelStr(diary.getLabel());
            //地址
            if(null!=diary.getLocationId()){
                Location location = LitePal.find(Location.class, diary.getLocationId());
                vo.setLocationStr(location.getLocationString());
            }
            //天气
            if(null!=diary.getWeatherId()){
                Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
                vo.setWeatherStr(weather.getWeather());
            }
            //图片
            List<Drawing> drawingList = LitePal.where("diaryId = ?",diary.getId()+"").find(Drawing.class);
            List<String> picSrcList = new ArrayList<>();
            drawingList.forEach(drawing -> picSrcList.add(drawing.getImgSrc()));
            vo.setPicSrcList(picSrcList);
            //评论
            List<Comment> commentList = LitePal.where("diaryId = ?",diary.getId()+"").order("id desc").find(Comment.class);
//            commentList.forEach(comment ->
//                    comment.setContent(RSAUtils.decode(comment.getContent())));
//            vo.setCommentList(commentList);
            vo.setCommentList(commentList);
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public SimpleResult addOneByArgs(String diaryContent, String labelStr, String locationStr, String weatherStr, List<String> tempImgSrcList) {
        SimpleResult result = new SimpleResult();
        result.setSuccess(true);
        //先保存天气和位置，因为需要他们的主键要当外键
        Weather weather = new Weather(null,weatherStr,null,null);
        Location location = new Location(null,null,null,locationStr);
        if(!"".equals(weather.getWeather())){
            weather.save();
        }
        if(!"".equals(location.getLocationString())){
            location.save();
        }
        //再存Diary，它的主键要当图片的外键
        Diary diary = new Diary(null,
                labelStr,
                diaryContent,
                //RSAUtils.encode(diaryContent),
                new Date(),
                weather.getId(),
                location.getId());
        if (diary.save()) {
            //存储图片关系
            tempImgSrcList.forEach(picSrc->{
                //将缓存图片移动到指定存储目录
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String nowMonth = sdf.format(new Date());
                File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image/"+nowMonth+"/");
                if(!path.exists()){
                    Log.i(TAG,"------创建目录"+path.getAbsolutePath());
                    path.mkdirs();
                }
                String absolutePath = path.getAbsolutePath();
                File finalSavePath = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
                try {
                    finalSavePath.createNewFile();
                    UriUtils.copyStream(new FileInputStream(new File(picSrc)),new FileOutputStream(finalSavePath));
                    Log.i(TAG,"------复制图片成功，新图片为:"+finalSavePath.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,"------保存日记失败,复制图片失败");
                    result.setSuccess(false);
                    result.setMsg("复制图片失败");
                    diary.delete();
                    weather.delete();
                    location.delete();
                    return;
                }
                Drawing drawing = new Drawing(null, finalSavePath.getAbsolutePath(), diary.getId());
                drawing.save();
            });
            Log.i(TAG,"------保存日记成功");
        }else{
            Log.e(TAG,"------保存日记失败,Diary数据库存入失败");
            result.setSuccess(false);
            result.setMsg("Diary数据库存入失败");
        }
        return result;
    }

    @Override
    public SimpleResult deleteById(Integer diaryId) {
        Diary diary = LitePal.find(Diary.class, diaryId);
        if(null==diary){
            return SimpleResult.error().msg("不存在该条日记，或许已经被删除了呢");
        }
        //先删除天气、位置信息
        Weather weather = LitePal.find(Weather.class, diary.getWeatherId());
        if(null!=weather){
            weather.delete();
        }
        Location location = LitePal.find(Location.class, diary.getLocationId());
        if(null!=location){
            location.delete();
        }
        //删除评论
        commentService.deleteByDiaryId(diaryId);
        //删除配图
        List<Drawing> drawings = LitePal.where("diaryId = ?", diaryId+"").find(Drawing.class);
        if(null!=drawings && drawings.size()>0){
            drawings.forEach(drawing -> {
                new File(drawing.getImgSrc()).delete();
                drawing.delete();
            });
        }
        diary.delete();
        return SimpleResult.ok();
    }
}
