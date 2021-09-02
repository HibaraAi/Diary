package cn.snowt.diary.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.litepal.LitePalApplication;

import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 16:47
 * @Description:
 */
public class DiaryAxisAdapter extends RecyclerView.Adapter{
    private List<DiaryVo> diaryVoList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView dateView;
        TextView diaryCutView;
        ImageView imageView;
        int diaryId;
        boolean isTempDiary;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            dateView = itemView.findViewById(R.id.axis_item_date);
            diaryCutView = itemView.findViewById(R.id.axis_item_diary_cut);
            imageView = itemView.findViewById(R.id.axis_item_image);
        }
    }

    public DiaryAxisAdapter(List<DiaryVo> diaryVoList) {
        this.diaryVoList = diaryVoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_axis_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.view.setOnClickListener(v->{
            Intent intent;
            if(viewHolder.isTempDiary){
                intent = new Intent(context, KeepDiaryActivity.class);
                intent.putExtra(KeepDiaryActivity.OPEN_FROM_TYPE,KeepDiaryActivity.OPEN_FROM_TEMP_DIARY);
            }else{
                intent = new Intent(context, DiaryDetailActivity.class);
            }
            intent.putExtra("id",viewHolder.diaryId);
            context.startActivity(intent);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        if ("".equals(diaryVo.getModifiedDate())){
            //如果日期为空，则一定为草稿
            newHolder.isTempDiary = true;
        }else{
            newHolder.isTempDiary = false;
        }
        newHolder.diaryId = diaryVo.getId();
        newHolder.dateView.setText((position+1)+"\n"+diaryVo.getModifiedDate());
        newHolder.diaryCutView.setText(diaryVo.getContent());
        Random random = new Random();
        switch (random.nextInt(5)) {
            case 1:{
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time1);
                break;
            }
            case 2:{
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time2);
                break;
            }
            case 3:{
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time3);
                break;
            }
            case 4:{
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time4);
                break;
            }
            case 0:{
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time5);
                break;
            }
            default:break;
        }
        if (diaryVo.getPicSrcList().size()>0){
            Glide.with(context)
                    .load(diaryVo.getPicSrcList().get(0))
                    .into(newHolder.imageView);
        }else{
            newHolder.imageView.setImageResource(R.drawable.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return diaryVoList.size();
    }
}
