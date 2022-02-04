package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.BigImgActivity;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-24 13:54
 * @Description:
 */
public class DiaryImageAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<String> imageSrcList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View imageArea;
        ImageView diaryImage;
        String imageSrc;
        Integer mPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageArea = itemView;
            this.diaryImage = itemView.findViewById(R.id.diary_image_item);
        }
    }

    public DiaryImageAdapter(ArrayList<String> imageSrcList) {
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
            ViewParent parent1 = viewHolder.diaryImage.getParent();
            ViewParent parent2 = parent1.getParent();
            RecyclerView recyclerView = (RecyclerView) parent2;
            if(recyclerView.getId()==R.id.video_day_item_ry){
                //视频库中的单击
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(viewHolder.imageSrc);
                intent.setDataAndType(uri, "video/*");
                context.startActivity(intent);
            }else{
                //其他的为之前的默认情况——图片单击
                Intent intent = new Intent(context, BigImgActivity.class);
                intent.putExtra(BigImgActivity.INTENT_DATA_IMG_POSITION,viewHolder.mPosition);
                intent.putStringArrayListExtra(BigImgActivity.INTENT_DATA_IMG_LIST,imageSrcList);
                context.startActivity(intent);
            }
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
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("二次确认");
                builder.setMessage("即将把这张图保存到你的系统相册");
                builder.setNegativeButton("手滑了",null);
                builder.setPositiveButton("保存", (dialog, which) -> {
                    String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/消消乐/";
                    File file = new File(absolutePath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    String finalName = absolutePath + UUID.randomUUID().toString() + ".jpg";
                    try {
                        UriUtils.copyStream(new FileInputStream(viewHolder.imageSrc),new FileOutputStream(finalName));
                        BaseUtils.shortTipInSnack(viewHolder.itemView,"应该保存成功了😂");
                    } catch (Exception e) {
                        BaseUtils.shortTipInSnack(viewHolder.itemView,"保存失败! ORz");
                    }
                });
                builder.show();
            }else if(recyclerView.getId()==R.id.pic_day_item_ry){
                //图库区域
                DrawingService drawIngService = new DrawIngServiceImpl();
                Integer diaryId = drawIngService.getDiaryIdByPicSre(viewHolder.imageSrc);
                Intent intent = new Intent(context, DiaryDetailActivity.class);
                intent.putExtra("id",diaryId);
                context.startActivity(intent);
            }else if (recyclerView.getId()==R.id.video_day_item_ry){
                //视频库区域
                VideoService videoService = new VideoServiceImpl();
                Integer diaryId = videoService.getDiaryIdByVideoSrc(viewHolder.imageSrc);
                Intent intent = new Intent(context, DiaryDetailActivity.class);
                intent.putExtra("id",diaryId);
                context.startActivity(intent);
            }else{
                System.out.println();
            }
            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        String imageSrc = imageSrcList.get(position);
        newHolder.imageSrc = imageSrc;
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.load_image)//图片加载出来前，显示的图片
                .fallback( R.drawable.bad_image) //url为空的时候,显示的图片
                .error(R.drawable.bad_image);//图片加载失败后，显示的图片
        Glide.with(context).load(imageSrc).apply(options).into(newHolder.diaryImage);
        newHolder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return imageSrcList.size();
    }
}
