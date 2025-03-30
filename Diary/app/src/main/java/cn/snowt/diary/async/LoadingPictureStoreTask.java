package cn.snowt.diary.async;

import android.content.Context;
import android.os.Handler;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.snowt.blog.BlogService;
import cn.snowt.diary.R;
import cn.snowt.diary.activity.PicturesActivity;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

public class LoadingPictureStoreTask extends MyAsyncTask{
    PicturesActivity.BlogPicAdapter adapter;
    private Context context;
    private boolean isLoadingPicture;

    public LoadingPictureStoreTask(Handler handler) {
        super(handler);
    }

    @Override
    void doAsync() {
        if(isLoadingPicture){
            loadingImage();
        }else{
            loadingVideo();
        }
    }

    private void loadingImage(){
        //1.处理日记中的配图
        DrawingService drawingService = new DrawIngServiceImpl();
        BlogService blogService = new BlogService();
        Map<Integer, List<Drawing>> allDiaryPic = drawingService.getAllPic();
        Map<Integer, List<String>> allBlogPic = blogService.getAllBlogPic();
        Map<Integer, List<String>> showMap = new HashMap<>(allDiaryPic.size()+allBlogPic.size());  //最终展示的数据
        if(allDiaryPic.isEmpty() && allBlogPic.isEmpty()){
            result.setSuccess(false);
            result.setMsg("日记和Blog都没有存图片");
        }else{
            if(!allDiaryPic.isEmpty()){
                //去除标签为“图库”的日记
                Set<Integer> integers = allDiaryPic.keySet();
                for (Integer diaryId : integers) {
                    Diary label = LitePal.select("label").where("id = " + diaryId).findFirst(Diary.class);
                    if(null!=label && null!=label.getLabel() && label.getLabel().contains("#图库#")){
                        allDiaryPic.remove((Integer)diaryId);
                    }
                }
                //1.1因为日记配图前期的设计与Blog的不同，需要转换drawingService.getAllPic()获取的map
                allDiaryPic.keySet().forEach(integer -> {
                    List<Drawing> drawings = allDiaryPic.get((Integer) integer);
                    assert drawings != null;
                    ArrayList<String> arrayList = new ArrayList<>(drawings.size());
                    drawings.forEach(drawing -> arrayList.add(drawing.getImgSrc()));
                    showMap.put(integer,arrayList);
                });
            }
            if(!allBlogPic.isEmpty()){
                //将Blog的ID变为负数，以区分是Diary的ID还是Blog的
                Map<Integer, List<String>> tempMap = new HashMap<>(allBlogPic.size());
                Set<Integer> blogIds = allBlogPic.keySet();
                blogIds.forEach(integer -> {
                    tempMap.put(-1*integer,allBlogPic.get((Integer) integer));
                });
                showMap.putAll(tempMap);
            }
        }
        PicturesActivity.BlogPicAdapter adapter = new PicturesActivity.BlogPicAdapter(context, showMap);
        result.setSuccess(true);
        result.setData(adapter);
    }


    private void loadingVideo(){
        //只要返回shouMap就行
        VideoService videoService = new VideoServiceImpl();
        BlogService blogService = new BlogService();
        Map<Integer, List<Video>> allDiaryVideos = videoService.getAllVideos();
        Map<Integer, List<String>> allBlogVideo = blogService.getAllBlogVideo();
        Map<Integer, List<String>> showMap = new HashMap<>(allDiaryVideos.size()+allBlogVideo.size());
        if (allDiaryVideos.isEmpty() && allBlogVideo.isEmpty()) {
            result.setSuccess(false);
            result.setMsg("日记和Blog都没有存视频");
        }else{
            if(!allDiaryVideos.isEmpty()){
                //去除标签为“视频库”的日记
                Set<Integer> integers = allDiaryVideos.keySet();
                List<Integer> ids = new ArrayList<>(integers.size());
                ids.addAll(integers);
                for (Integer diaryId : ids) {
                    Diary label = LitePal.select("label").where("id = " + diaryId).findFirst(Diary.class);
                    if(null!=label && null!=label.getLabel() && label.getLabel().contains("#视频库#")){
                        allDiaryVideos.remove((Integer)diaryId);
                    }
                }
                //1.1因为日记配图前期的设计与Blog的不同，需要转换drawingService.getAllPic()获取的map
                allDiaryVideos.keySet().forEach(integer -> {
                    List<Video> videos = allDiaryVideos.get((Integer) integer);
                    assert videos != null;
                    ArrayList<String> arrayList = new ArrayList<>(videos.size());
                    videos.forEach(video -> arrayList.add(video.getVideoSrc()));
                    showMap.put(integer,arrayList);
                });
            }
            if(!allBlogVideo.isEmpty()){
                //将Blog的ID变为负数，以区分是Diary的ID还是Blog的
                Map<Integer, List<String>> tempMap = new HashMap<>(allBlogVideo.size());
                Set<Integer> blogIds = allBlogVideo.keySet();
                blogIds.forEach(integer -> {
                    tempMap.put(-1*integer,allBlogVideo.get((Integer) integer));
                });
                showMap.putAll(tempMap);
            }
            PicturesActivity.BlogPicAdapter adapter = new PicturesActivity.BlogPicAdapter(context, showMap);
            result.setSuccess(true);
            result.setData(adapter);
        }
    }

    public void getImage(Context context){
        isLoadingPicture = true;
        this.context = context;
        result = new SimpleResult();
        startAsyncTask();
    }

    public void getVideo(Context context){
        isLoadingPicture = false;
        this.context = context;
        result = new SimpleResult();
        startAsyncTask();
    }
}
