package cn.snowt.diary.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.BigImgActivity;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.ZoomImageActivity;
import cn.snowt.diary.adapter.DiaryCommentAdapter;
import cn.snowt.diary.adapter.DiaryImageAdapter;
import cn.snowt.diary.adapter.DiaryVideoAdapter;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2024-06-10 18:01
 * @Description: PDFUtils
 */
public class PDFUtils {
    static DiaryService diaryService = new DiaryServiceImpl();
    /**
     * 将DiaryVoList的数据导出到PDF中，每一个DiaryVo保存为一张paper，样式使用的是CardView（R.layout.diary_item），
     * 宽度为手机屏幕宽度，高度自适应。所有paper导出到一个PDF文件。
     * @param diaryVos diaryVos
     * @param context context
     * @param parent 随便一个ViewGroup，可以是最外层的layout
     */
    public static void createPdf(List<DiaryVo> diaryVos,Context context, ViewGroup parent){
        PdfDocument document = new PdfDocument();
        PdfDocument.Page page = null;
        for(int i =0;i<diaryVos.size();i++){
            DiaryVo diaryVo = diaryVos.get(i);
            if (!diaryService.existById(diaryVo.getId())){  //如果不存在则跳过
                continue;
            }
            View view = voToView(diaryVo, context, parent);
            view.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = viewToBitMap2(view);
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(parent.getWidth(), view.getMeasuredHeight(), i).create();
            // start a page
            page = document.startPage(pageInfo);
            if (page == null) {
                return;
            }
            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);

        }
        // 1.write the document content
        String pdfDir = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION + "output/").getAbsolutePath()+"/PDF/";
        File file = new File(pdfDir);
        if(!file.exists()){
            file.mkdirs();
        }
        String s = file.getAbsolutePath()+"/"+ BaseUtils.dateToStringWithout(new Date()).substring(0,10)+"-"+ UUID.randomUUID().toString().substring(0,6) + ".pdf";
        try {
            // 1.1write the document content
            document.writeTo(new FileOutputStream(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // close the document
        document.close();
    }

    /**
     * 将View转成Bitmap
     * @param view view
     * @return Bitmap
     */
    private static Bitmap viewToBitMap2(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 将一个View导出为pdf
     * @param content
     */
    public static void saveViewAsPdf(View content){
        // create a new document
        PdfDocument document = new PdfDocument();
        // create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(), content.getHeight(), 1).create();
        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        content.draw(page.getCanvas());
        // finish the page
        document.finishPage(page);

        // add more pages
//        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(content.getWidth(), content.getHeight(), 1).create();
//        PdfDocument.Page page2 = document.startPage(pageInfo2);
//        content.draw(page2.getCanvas());
//        document.finishPage(page2);

        // 1.write the document content
        String pdfDir = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION + "output/").getAbsolutePath()+"/PDF/";
        File file = new File(pdfDir);
        if(!file.exists()){
            file.mkdirs();
        }
        String s = file.getAbsolutePath()+"/"+ BaseUtils.dateToStringWithout(new Date()).substring(0,10)+"-"+UUID.randomUUID().toString().substring(0,6) + ".pdf";
        try {
            // 1.1write the document content
            document.writeTo(new FileOutputStream(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // close the document
        document.close();
        BaseUtils.shortTipInCoast(LitePalApplication.getContext(),"PDF成功导出");
    }

    /**
     * 把DiaryVo转成CardView（R.layout.diary_item）
     * @param diaryVo diaryVo
     * @param context context
     * @param parent 随便一个ViewGroup，可以是最外层的layout
     * @return CardView（R.layout.diary_item）
     */

    private static View voToView(DiaryVo diaryVo, Context context, ViewGroup parent){
        View view = LayoutInflater.from(context).inflate(R.layout.diary_item, parent, false);
        //viewHolder与view文件绑定
        ImageView headImg = view.findViewById(R.id.item_head);
        TextView username = view.findViewById(R.id.item_username);
        TextView modifyDate = view.findViewById(R.id.item_modifyDate);
        TextView weather = view.findViewById(R.id.item_weather);
        TextView location = view.findViewById(R.id.item_location);
        TextView label = view.findViewById(R.id.item_label);
        TextView content = view.findViewById(R.id.item_content);
        Button comment = view.findViewById(R.id.item_btn_comment);
        RecyclerView imageView = view.findViewById(R.id.item_pic_area);
        RecyclerView videoView = view.findViewById(R.id.item_video_area);
        RecyclerView commentView = view.findViewById(R.id.item_comment_area);
        //viewHolder.diaryView = view;
        Button submitCommentBtn = view.findViewById(R.id.item_comment_input_btn);
        EditText commentInput = view.findViewById(R.id.item_comment_input);
        CardView quoteDiaryArea = view.findViewById(R.id.item_quote_diary_area);
        TextView quoteDiaryStr = view.findViewById(R.id.item_quote_diary_content);

        //为viewHolder填写数据
        boolean visible;
        //头像及用户名是每个item都一样，且不会有点击事件
        if(null== MyConfiguration.getInstance().getHeadImg()){
            headImg.setImageResource(R.drawable.nav_icon);
        }else{
            headImg.setImageBitmap(BitmapFactory.decodeFile(MyConfiguration.getInstance().getHeadImg()));
        }
        username.setText(MyConfiguration.getInstance().getUsername());
        modifyDate.setText(diaryVo.getModifiedDate());
        weather.setText(diaryVo.getWeatherStr());
        location.setText(diaryVo.getLocationStr());
        if("".equals(diaryVo.getLabelStr())){
            label.setVisibility(View.GONE);
        }else{
            label.setText(diaryVo.getLabelStr());
            label.setVisibility(View.VISIBLE);
        }
        content.setText(diaryVo.getContent());
        //处理图片展示
        RecyclerView imgRecyclerView = imageView;
        PdfImageAdapter imgAdapter = new PdfImageAdapter((ArrayList<String>) diaryVo.getPicSrcList());
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        imgRecyclerView.setAdapter(imgAdapter);
        imgRecyclerView.setLayoutManager(layoutManager);
        //处理视频展示
        RecyclerView videoRV = videoView;
        PdfVideoAdapter videoAdapter = new PdfVideoAdapter(diaryVo.getVideoSrcList());
        GridLayoutManager videoLayoutManager = new GridLayoutManager(context, 2);
        videoRV.setAdapter(videoAdapter);
        videoRV.setLayoutManager(videoLayoutManager);
        //处理评论区
        //comment.setBackgroundColor(R.attr.colorPrimary);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
        comment.setBackgroundColor(typedValue.data);
        commentInput.setVisibility(View.GONE);
        submitCommentBtn.setVisibility(View.GONE);
        RecyclerView commentRecyclerView = commentView;
        DiaryCommentAdapter commentAdapter = new DiaryCommentAdapter(diaryVo.getCommentList());
        GridLayoutManager layoutManager2 = new GridLayoutManager(context, 1);
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(layoutManager2);
        if(!diaryVo.getCommentList().isEmpty()){
            comment.setText("评论("+diaryVo.getCommentList().size()+")");
            //改为可见
            view.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
            visible = true;
        }else{
            comment.setText("评论");
            //改为不可见
            view.findViewById(R.id.item_comment_area_parent).setVisibility(View.GONE);
            visible = false;
        }
        //处理引用日记
        if(null!=diaryVo.getQuoteDiaryUuid() && !"".equals(diaryVo.getQuoteDiaryUuid())){
            if("del".equals(diaryVo.getQuoteDiaryUuid())){
                quoteDiaryStr.setText("[提示:引用的日记已被删除]");
            }else{
                quoteDiaryStr.setText(diaryVo.getQuoteDiaryStr());
            }
            if (context.getResources().getConfiguration().uiMode == 0x11) {
                quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#EFEAEB"));
            }else{
                quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#525050"));
            }
            quoteDiaryArea.setVisibility(View.VISIBLE);
        }else{
            quoteDiaryArea.setVisibility(View.GONE);
        }
        //设置字体大小
        float fontSize = MyConfiguration.getInstance().getFontSize();
        if(fontSize!=-1){
            content.setTextSize(fontSize);
        }
        return view;
    }

    /**
     * 使用DiaryImageAdapter会导致图片为空白，所以换重写一个Adapter专门服务于输出PDF
     *
     * 貌似是使用Glide加载图片导致的
     */
    private static class PdfImageAdapter extends RecyclerView.Adapter{
        private Context context;
        private ArrayList<String> imageSrcList;

        public PdfImageAdapter(ArrayList<String> imageSrcList) {
            this.imageSrcList = imageSrcList;
        }

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

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(null==context){
                context = parent.getContext();
            }
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.diary_image_item, parent, false);
            final PdfImageAdapter.ViewHolder viewHolder = new PdfImageAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            PdfImageAdapter.ViewHolder newHolder = (PdfImageAdapter.ViewHolder)holder;
            String imageSrc = imageSrcList.get(position);
            newHolder.imageSrc = imageSrc;
            if(new File(imageSrc).exists()){
                newHolder.diaryImage.setImageBitmap(BitmapFactory.decodeFile(imageSrc));

            }else{
                newHolder.diaryImage.setImageResource(R.drawable.bad_image);
            }
            newHolder.mPosition = position;
        }

        @Override
        public int getItemCount() {
            return imageSrcList.size();
        }
    }

    private static class PdfVideoAdapter extends RecyclerView.Adapter{
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

        public PdfVideoAdapter(ArrayList<String> videoSrcList) {
            this.videoSrcList = videoSrcList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if(null==context){
                context = parent.getContext();
            }
            //view==CardView:diary_image_item
            CardView view = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.diary_image_item, parent, false);
            final PdfVideoAdapter.ViewHolder viewHolder = new PdfVideoAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            PdfVideoAdapter.ViewHolder newHolder = (PdfVideoAdapter.ViewHolder)holder;
            String videoSrc = videoSrcList.get(position);
            newHolder.videoSrc = videoSrc;
            if(new File(videoSrc).exists()){
                newHolder.diaryVideo.setImageBitmap(getVideoThumbnail(videoSrc,512,384, MediaStore.Video.Thumbnails.MICRO_KIND));
            }else{
                newHolder.diaryVideo.setImageResource(R.drawable.bad_video);
            }
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

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     * @param videoPath 视频的路径
     * @param width 指定输出视频缩略图的宽度
     * @param height 指定输出视频缩略图的高度度
     * @param kind 参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *      其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    private static Bitmap getVideoThumbnail(String videoPath, int width, int height,int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind); //調用ThumbnailUtils類的靜態方法createVideoThumbnail獲取視頻的截圖；
       //ThumbnailUtils.createVideoThumbnail(new File(videoPath), Size.parseSize("22"), CancellationSignal)
        if(bitmap!= null){
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//調用ThumbnailUtils類的靜態方法extractThumbnail將原圖片（即上方截取的圖片）轉化為指定大小；
        }
        return bitmap;
    }
}
