package cn.snowt.diary.service.impl;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.service.VideoService;

/**
 * @Author: HibaraAi
 * @Date: 2022-01-17 16:24:41
 * @Description:
 */
public class VideoServiceImpl implements VideoService {
    @Override
    public Map<Integer, List<Video>> getAllVideos() {
        Map<Integer ,List<Video>> result = new HashMap();
        List<Video> allVideo = LitePal.findAll(Video.class);
        allVideo.forEach(video->{
            String videoSrc = video.getVideoSrc();
            File file = new File(videoSrc);
            if(file.exists()){
                Integer diaryId = video.getDiaryId();
                Set<Integer> keySet = result.keySet();
                if(keySet.contains(diaryId)){
                    List<Video> videos = result.get((Integer) diaryId);
                    videos.add(video);
                }else{
                    List<Video> list = new ArrayList<>();
                    list.add(video);
                    result.put(diaryId,list);
                }
            }
        });
        return result;
    }

    @Override
    public Integer getDiaryIdByVideoSrc(String videoSrc) {
        Video first = LitePal.where("videoSrc like '" + videoSrc + "'").findFirst(Video.class);
        if(null==first){
            return -1;
        }else{
            return first.getDiaryId();
        }
    }
}
