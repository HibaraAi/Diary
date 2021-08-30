package cn.snowt.diary.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.MainActivity;
import cn.snowt.diary.activity.ZoomImageActivity;
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
            Intent intent = new Intent(context, ZoomImageActivity.class);
            intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,viewHolder.imageSrc);
            context.startActivity(intent);
        });
        viewHolder.diaryImage.setOnLongClickListener(v->{
            ViewParent parent1 = viewHolder.diaryImage.getParent();
            ViewParent parent2 = parent1.getParent();
            RecyclerView recyclerView = (RecyclerView) parent2;
            if(recyclerView.getId()==R.id.keep_diary_pic_area){
                //编辑日记的区域
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("提示：");
                builder.setMessage("你确定要移出这张照片吗？");
                builder.setNegativeButton("移除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KeepDiaryActivity.deleteTempPicInEdit(viewHolder.imageSrc);
                    }
                });
                builder.setPositiveButton("刚刚点错了",null);
                builder.show();
            }else if(recyclerView.getId()==R.id.item_pic_area){
                //主屏幕区的图片
                BaseUtils.shortTipInSnack(viewHolder.diaryImage,"别长按，屏幕按爆了也没反应 >_<");
            }
            return true;
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
