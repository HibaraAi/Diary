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
import cn.snowt.diary.activity.ZoomImageActivity;
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
                //?????????????????????
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(viewHolder.imageSrc);
                intent.setDataAndType(uri, "video/*");
                context.startActivity(intent);
            }else{
                //???????????????????????????????????????????????????
                if(imageSrcList.size()==1){
                    Intent intent1 = new Intent(context, ZoomImageActivity.class);
                    intent1.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,imageSrcList.get(0));
                    context.startActivity(intent1);
                }else{
                    Intent intent = new Intent(context, BigImgActivity.class);
                    intent.putExtra(BigImgActivity.INTENT_DATA_IMG_POSITION,viewHolder.mPosition);
                    intent.putStringArrayListExtra(BigImgActivity.INTENT_DATA_IMG_LIST,imageSrcList);
                    context.startActivity(intent);
                }
            }
        });
        viewHolder.diaryImage.setOnLongClickListener(v->{
            ViewParent parent1 = viewHolder.diaryImage.getParent();
            ViewParent parent2 = parent1.getParent();
            RecyclerView recyclerView = (RecyclerView) parent2;
            if(recyclerView.getId()==R.id.keep_diary_pic_area){
                //?????????????????????
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("?????????");
                builder.setMessage("????????????????????????????????????");
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KeepDiaryActivity.deleteTempPicInEdit(viewHolder.imageSrc);
                    }
                });
                builder.setPositiveButton("???????????????",null);
                builder.show();
            }else if(recyclerView.getId()==R.id.item_pic_area){
                //?????????????????????
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("????????????");
                builder.setMessage("?????????????????????????????????????????????");
                builder.setNegativeButton("?????????",null);
                builder.setPositiveButton("??????", (dialog, which) -> {
                    String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/?????????/";
                    File file = new File(absolutePath);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    String finalName = absolutePath + UUID.randomUUID().toString() + ".jpg";
                    try {
                        UriUtils.copyStream(new FileInputStream(viewHolder.imageSrc),new FileOutputStream(finalName));
                        //BaseUtils.shortTipInSnack(viewHolder.itemView,"?????????????????????????");
                        BaseUtils.alertDialogToShow(context,"??????","????????????????????????????????????????????????\n"+finalName);
                    } catch (Exception e) {
                        BaseUtils.shortTipInSnack(viewHolder.itemView,"????????????! ORz");
                    }
                });
                builder.show();
            }else if(recyclerView.getId()==R.id.pic_day_item_ry){
                //????????????
                DrawingService drawIngService = new DrawIngServiceImpl();
                Integer diaryId = drawIngService.getDiaryIdByPicSre(viewHolder.imageSrc);
                Intent intent = new Intent(context, DiaryDetailActivity.class);
                intent.putExtra("id",diaryId);
                context.startActivity(intent);
            }else if (recyclerView.getId()==R.id.video_day_item_ry){
                //???????????????
                VideoService videoService = new VideoServiceImpl();
                Integer diaryId = videoService.getDiaryIdByVideoSrc(viewHolder.imageSrc);
                Intent intent = new Intent(context, DiaryDetailActivity.class);
                intent.putExtra("id",diaryId);
                context.startActivity(intent);
            }else{
                BaseUtils.shortTipInCoast(context,"??????????????????...DiaryImageAdapter");
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
                .placeholder(R.drawable.load_image)//???????????????????????????????????????
                .fallback( R.drawable.bad_image) //url???????????????,???????????????
                .error(R.drawable.bad_image);//???????????????????????????????????????
        Glide.with(context).load(imageSrc).apply(options).into(newHolder.diaryImage);
        newHolder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return imageSrcList.size();
    }
}
