package cn.snowt.diary.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-12-23 23:04:06
 * @Description:
 */
public class PicDayAdapter extends RecyclerView.Adapter{

    private final Map<Integer,List<Drawing>> picMap;
    private Context context;
    //排序辅助用
    private final List<Integer> sortDiaryId;

    private static final DiaryService diaryService = new DiaryServiceImpl();

    public PicDayAdapter(Map<Integer, List<Drawing>> picMap) {
        this.picMap = picMap;
        //先排序，希望时间倒序展示
        sortDiaryId = new ArrayList<>();
        DiaryServiceImpl diaryService = new DiaryServiceImpl();
        TreeMap<Date,Integer> treeMap = new TreeMap<>();
        picMap.keySet().forEach(integer -> {
            Date dateById = diaryService.getDateById(integer);
            if(dateById!=null){
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
            dayView = itemView.findViewById(R.id.pic_day_item_date);
            recyclerView = itemView.findViewById(R.id.pic_day_item_ry);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pic_day_item, parent, false);
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
        List<Drawing> drawings = picMap.get((Integer) id);
        ArrayList<String> picStrList = new ArrayList<>();
        drawings.forEach(drawing -> picStrList.add(drawing.getImgSrc()));
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(id);
        List<DiaryVo> voList = diaryService.getSimpleDiaryByIds(integers);
        String modifiedDate = voList.get(0).getModifiedDate();
        newHolder.dayView.setText(modifiedDate);
        //处理图片展示
        RecyclerView imgRecyclerView = newHolder.recyclerView;
        DiaryImageAdapter imgAdapter = new DiaryImageAdapter(picStrList);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        imgRecyclerView.setAdapter(imgAdapter);
        imgRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public int getItemCount() {
        return picMap.size();
    }
}
