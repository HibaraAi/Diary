package cn.snowt.diary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.snowt.diary.R;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2022-01-17 16:37:01
 * @Description:
 */
public class VideoDayAdapter extends RecyclerView.Adapter{
    private final Map<Integer, List<Video>> videoMap;
    private Context context;
    //排序辅助用
    private final List<Integer> sortDiaryId;

    private static final DiaryService diaryService = new DiaryServiceImpl();

    public VideoDayAdapter(Map<Integer, List<Video>> videoMap) {
        this.videoMap = videoMap;
        //先排序，希望时间倒序展示
        sortDiaryId = new ArrayList<>();
        TreeMap<Date,Integer> treeMap = new TreeMap<>();
        videoMap.keySet().forEach(integer -> {
            Date dateById = diaryService.getDateById(integer);
            if(dateById!=null){
                //这样子处理会导致，如果多个时间(key)完全一致，日记id(value)只会存一个
                //不过毫秒级的时间完全一致也只会发生在恢复日记中的重复日记里。
                // 所以这个bug不修
                treeMap.put(dateById,integer);
            }
        });
        treeMap.forEach((date, integer) -> sortDiaryId.add(integer));
        Collections.reverse(sortDiaryId);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView dayView;
        RecyclerView recyclerView;
        Integer diaryId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayView = itemView.findViewById(R.id.video_day_item_date);
            recyclerView = itemView.findViewById(R.id.video_day_item_ry);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_day_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        Integer id = sortDiaryId.get(position);
        newHolder.diaryId = id;
        List<Video> videos = videoMap.get((Integer) id);
        ArrayList<String> videoStrList = new ArrayList<>();
        videos.forEach(video -> videoStrList.add(video.getVideoSrc()));
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(id);
        List<DiaryVo> voList = diaryService.getSimpleDiaryByIds(integers);
        String modifiedDate = voList.get(0).getModifiedDate();
        newHolder.dayView.setText(modifiedDate);
        //处理视频缩略图展示
        RecyclerView imgRecyclerView = newHolder.recyclerView;
        DiaryImageAdapter imgAdapter = new DiaryImageAdapter(videoStrList);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        imgRecyclerView.setAdapter(imgAdapter);
        imgRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public int getItemCount() {
        return sortDiaryId.size();
    }
}
