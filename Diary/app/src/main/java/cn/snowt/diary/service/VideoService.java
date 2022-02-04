package cn.snowt.diary.service;

import java.util.List;
import java.util.Map;

import cn.snowt.diary.entity.Video;

/**
 * @Author: HibaraAi
 * @Date: 2022-01-17 16:23:15
 * @Description:
 */
public interface VideoService {

    /**
     * 获取所有视频及其对应日记id,相同日记的放在一组，同时过滤掉失效视频
     * @return
     */
    Map<Integer , List<Video>> getAllVideos();

    /**
     * 通过视频地址查找关联的日记id
     * @param videoSrc
     * @return 如果没有则返回-1
     */
    Integer getDiaryIdByVideoSrc(String videoSrc);
}
