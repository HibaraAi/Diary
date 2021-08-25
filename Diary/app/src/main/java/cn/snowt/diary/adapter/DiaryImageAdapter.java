package cn.snowt.diary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-24 13:54
 * @Description:
 */
public class DiaryImageAdapter extends RecyclerView.Adapter{
    private Context context;
    private List<String> imageSrcList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View imageArea;
        ImageView diaryImage;
        String imageSrc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageArea = itemView;
            this.diaryImage = itemView.findViewById(R.id.diary_image_item);
        }
    }

    public DiaryImageAdapter(List<String> imageSrcList) {
        this.imageSrcList = imageSrcList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(null==context){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_image_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.diaryImage.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(context,"Click Image");
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        String imageSrc = imageSrcList.get(position);
        newHolder.imageSrc = imageSrc;
        Glide.with(context).load(imageSrc).into(newHolder.diaryImage);
    }

    @Override
    public int getItemCount() {
        return imageSrcList.size();
    }
}
