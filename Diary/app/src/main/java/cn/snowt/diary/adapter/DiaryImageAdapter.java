package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.TypedValue;
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
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.TitleBarStyle;

import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.BigImgActivity;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.ZoomImageActivity;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.GlideEngine;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-24 13:54
 * @Description:
 */
public class DiaryImageAdapter extends RecyclerView.Adapter{
    private Context context;
    private ArrayList<String> imageSrcList;
    private ArrayList<LocalMedia> mediaList;

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
        this.mediaList = new ArrayList<>(imageSrcList.size());
        imageSrcList.forEach(s -> {
            LocalMedia localMedia = LocalMedia.generateLocalMedia(LitePalApplication.getContext(), s);
            mediaList.add(localMedia);
        });
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
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri uri = Uri.parse(viewHolder.imageSrc);
//                intent.setDataAndType(uri, "video/*");
//                context.startActivity(intent);
                this.mediaList = new ArrayList<>(imageSrcList.size());
                imageSrcList.forEach(s -> {
                    LocalMedia localMedia = LocalMedia.generateLocalMedia(LitePalApplication.getContext(), s);
                    localMedia.setMimeType("video/*");
                    mediaList.add(localMedia);
                });
                PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
                if(context.getResources().getConfiguration().uiMode == 0x11){
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
                    TitleBarStyle titleBarStyle = new TitleBarStyle();
                    titleBarStyle.setTitleBackgroundColor(typedValue.data);
                    pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
                }
                PictureSelector.create(context)
                        .openPreview()
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setSelectorUIStyle(pictureSelectorStyle)
                        .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                            @Override
                            public void onPreviewDelete(int position) {

                            }
                            @Override
                            public boolean onLongPressDownload(Context context, LocalMedia media) {
                                return false;
                            }
                        }).startActivityPreview(viewHolder.mPosition, false, mediaList);
            }else{
                //其他的为之前的默认情况——图片单击
//                if(imageSrcList.size()==1){
//                    Intent intent1 = new Intent(context, ZoomImageActivity.class);
//                    intent1.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,imageSrcList.get(0));
//                    context.startActivity(intent1);
//                }else{
//                    Intent intent = new Intent(context, BigImgActivity.class);
//                    intent.putExtra(BigImgActivity.INTENT_DATA_IMG_POSITION,viewHolder.mPosition);
//                    intent.putStringArrayListExtra(BigImgActivity.INTENT_DATA_IMG_LIST,imageSrcList);
//                    context.startActivity(intent);
//                }
                if(null==mediaList || mediaList.isEmpty() || mediaList.size()!=imageSrcList.size()){
                    this.mediaList = new ArrayList<>(imageSrcList.size());
                    imageSrcList.forEach(s -> {
                        LocalMedia localMedia = LocalMedia.generateLocalMedia(LitePalApplication.getContext(), s);
                        mediaList.add(localMedia);
                    });
                }
                PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
                if(context.getResources().getConfiguration().uiMode == 0x11){
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
                    TitleBarStyle titleBarStyle = new TitleBarStyle();
                    titleBarStyle.setTitleBackgroundColor(typedValue.data);
                    pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
                }
                PictureSelector.create(context)
                        .openPreview()
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setSelectorUIStyle(pictureSelectorStyle)
                        .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                            @Override
                            public void onPreviewDelete(int position) {

                            }
                            @Override
                            public boolean onLongPressDownload(Context context, LocalMedia media) {
                                return false;
                            }
                        }).startActivityPreview(viewHolder.mPosition, false, mediaList);
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
                        //BaseUtils.shortTipInSnack(viewHolder.itemView,"应该保存成功了😂");
                        BaseUtils.alertDialogToShow(context,"提示","图片应该保存成功了，存储路径为：\n"+finalName);
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
                BaseUtils.shortTipInCoast(context,"未定义的操作...DiaryImageAdapter");
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
