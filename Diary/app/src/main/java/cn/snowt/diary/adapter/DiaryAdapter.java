package cn.snowt.diary.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SingleVar;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 13:45
 * @Description:
 */
public class DiaryAdapter extends RecyclerView.Adapter{

    private List<DiaryVo> diaryVoList;
    private Context context;


    static class ViewHolder extends RecyclerView.ViewHolder{
        View diaryView;
        ImageView headImg;
        TextView username;
        TextView modifyDate;
        TextView weather;
        TextView location;
        TextView content;
        RecyclerView imageView;
        Button comment;
        Button like;
        Button del;

        Integer diaryId;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headImg = itemView.findViewById(R.id.item_head);
            username = itemView.findViewById(R.id.item_username);
            modifyDate = itemView.findViewById(R.id.item_modifyDate);
            weather = itemView.findViewById(R.id.item_weather);
            location = itemView.findViewById(R.id.item_location);
            content = itemView.findViewById(R.id.item_content);
            comment = itemView.findViewById(R.id.item_btn_comment);
            like = itemView.findViewById(R.id.item_btn_like);
            del = itemView.findViewById(R.id.item_btn_del);
            imageView = itemView.findViewById(R.id.item_pic_area);
            diaryView = itemView;
        }
    }

    public DiaryAdapter(List<DiaryVo> diaryVoList) {
        this.diaryVoList = diaryVoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.like.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(parent.getContext(),viewHolder.like.getText().toString());
        });
        viewHolder.comment.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(parent.getContext(),viewHolder.comment.getText().toString());
        });
        viewHolder.del.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(parent.getContext(),"长按删除");
        });
        viewHolder.del.setOnLongClickListener(v->{
            BaseUtils.shortTipInCoast(parent.getContext(),"删除逻辑");
            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        newHolder.diaryId = diaryVo.getId();
        if(null==SingleVar.getHeadImg()){
            newHolder.headImg.setImageResource(R.drawable.nav_icon);
        }else{
            Log.e("DiaryAdapter","在这设置动态头像");
            newHolder.headImg.setImageResource(R.drawable.nav_icon);
        }
        newHolder.username.setText(SingleVar.getUsername());
        newHolder.modifyDate.setText(diaryVo.getModifiedDate());
        newHolder.weather.setText(diaryVo.getWeatherStr());
        newHolder.location.setText(diaryVo.getLocationStr());
        newHolder.content.setText(diaryVo.getContent());
        //处理图片展示
        RecyclerView recyclerView = newHolder.imageView;
        DiaryImageAdapter adapter = new DiaryImageAdapter(diaryVo.getPicSrcList());
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        holder = newHolder;
    }

    @Override
    public int getItemCount() {
        return diaryVoList.size();
    }

}
