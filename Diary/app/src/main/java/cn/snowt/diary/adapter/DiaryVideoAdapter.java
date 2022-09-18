package cn.snowt.diary.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-24 13:54
 * @Description:
 */
public class DiaryVideoAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<String> videoSrcList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View videoArea;
        ImageView diaryVideo;
        String videoSrc;
        Integer mPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.videoArea = itemView;
            this.diaryVideo = itemView.findViewById(R.id.diary_image_item);
        }
    }

    public DiaryVideoAdapter(ArrayList<String> videoSrcList) {
        this.videoSrcList = videoSrcList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //parent==RecyclerView:item_video_area

        if(null==context){
            context = parent.getContext();
        }
        //view==CardView:diary_image_item
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_image_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.diaryVideo.setOnClickListener(v->{
            boolean removeTip = BaseUtils.getDefaultSharedPreferences().getBoolean("removeTip", false);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !removeTip){
                //高于或等于Android11
                BaseUtils.longTipInCoast(context,"如果播放异常，可长按保存");
            }
            //为Android 10
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(viewHolder.videoSrc);
            intent.setDataAndType(uri, "video/*");
            context.startActivity(intent);
        });
        viewHolder.diaryVideo.setOnLongClickListener(v->{
            ViewParent parent1 = viewHolder.diaryVideo.getParent();
            ViewParent parent2 = parent1.getParent();
            RecyclerView recyclerView = (RecyclerView) parent2;
            if(recyclerView.getId()==R.id.keep_diary_video_area){
                //编辑日记的区域
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("提示：");
                builder.setMessage("你确定要移出这个视频吗？");
                builder.setNegativeButton("移除", (dialog, which) -> KeepDiaryActivity.deleteTempVideoInEdit(viewHolder.videoSrc));
                builder.setPositiveButton("刚刚点错了",null);
                builder.show();
            }else if(recyclerView.getId()==R.id.item_video_area){
                //主屏幕区的图片
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("二次确认");
                builder.setMessage("即将把这个视频保存到你的系统相册");
                builder.setNegativeButton("手滑了",null);
                builder.setPositiveButton("保存", (dialog, which) -> {
                    String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/消消乐/";
                    File file = new File(absolutePath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    String finalName = absolutePath + UUID.randomUUID().toString() + ".mp4";
                    try {
                        UriUtils.copyStream(new FileInputStream(viewHolder.videoSrc),new FileOutputStream(finalName));
                        //BaseUtils.shortTipInSnack(viewHolder.itemView,"应该保存成功了😂");
                        BaseUtils.alertDialogToShow(context,"提示","视频应该保存成功了，存储路径为：\n"+finalName);
                    } catch (Exception e) {
                        BaseUtils.shortTipInSnack(viewHolder.itemView,"保存失败! ORz");
                    }
                });
                builder.show();
            }else if(recyclerView.getId()==R.id.pic_day_item_ry){
                //图库区域

            }else{
                BaseUtils.shortTipInCoast(context,"未定义长按操作");
            }
            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        String videoSrc = videoSrcList.get(position);
        newHolder.videoSrc = videoSrc;
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.load_image)//图片加载出来前，显示的图片
                .fallback(R.drawable.bad_video) //url为空的时候,显示的图片
                .error(R.drawable.bad_video);//图片加载失败后，显示的图片
        Glide.with(context).load(videoSrc).apply(options).into(newHolder.diaryVideo);
        newHolder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        if(videoSrcList==null){
            return 0;
        }
        return videoSrcList.size();
    }
}
