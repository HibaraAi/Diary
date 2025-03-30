package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import cn.snowt.blog.BlogService;
import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryImageAdapter;
import cn.snowt.diary.adapter.DiaryVideoAdapter;
import cn.snowt.diary.async.LoadingPictureStoreTask;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import lombok.Data;

/**
 * 查看所有图片或者所有视频
 */
public class PicturesActivity extends AppCompatActivity {

    /**
     * 标识从哪里打开此Activity，默认为打开图库
     */
    public static String OPEN_FROM_TYPE = "openFrom";
    public static Integer OPEN_FROM_PICTURE = 1; //打开图库
    public static Integer OPEN_FROM_VIDEO = 2; //打开视频库
    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    RecyclerView recyclerView;
    private Integer openType;

    /**
     * 2025年3月30日，发现由于加入了blog图片，打开图库时，会有卡住现象，猜测处理recyclerView的Adapter耗时有点多，
     * 现在增加一个进度条过度一下，装配adapter时显示一个动画
     */
    Handler handler = new Handler(new Handler.Callback() {  //处理异步回调
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    SimpleResult result = (SimpleResult) msg.obj;
                    if(!result.getSuccess()){
                        BaseUtils.shortTipInSnack(recyclerView,result.getMsg());
                    }else{
                        BlogPicAdapter adapter = (BlogPicAdapter) result.getData();
                        recyclerView.setAdapter(adapter);
                    }
                    closeProgressAlertDialog();
                    break;
                }
                case MyAsyncTask.START_TASK:{
                    showProgressAlertDialog();
                    break;
                }
                default:break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_pictrues);
        //绑定控件
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        openType = intent.getIntExtra(OPEN_FROM_TYPE, -1);
        recyclerView = findViewById(R.id.at_pic_rv);
        View parent = (View) recyclerView.getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            parent.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
            parent.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        LoadingPictureStoreTask asyncTask = new LoadingPictureStoreTask(handler);
        if(Objects.equals(openType, OPEN_FROM_PICTURE)){
            //打开的是图库
            if(null!=actionBar){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("图库");
            }
            asyncTask.getImage(PicturesActivity.this);
        }else if (Objects.equals(openType, OPEN_FROM_VIDEO)){
            if(null!=actionBar){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("视频库");
            }
            asyncTask.getVideo(PicturesActivity.this);
        }else{
            BaseUtils.shortTipInCoast(PicturesActivity.this,"BUG...PictureActivity");
        }
    }

    private void showProgressAlertDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(PicturesActivity.this);
        builder.setCancelable(false);
        LinearLayout linearLayout = new LinearLayout(PicturesActivity.this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(PicturesActivity.this);
        imageView.setImageResource(R.drawable.loading);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof AnimatedImageDrawable){
            AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
            animatedImageDrawable.start();
        }
        linearLayout.addView(imageView);
        builder.setView(linearLayout);
        ProgressADL = builder.show();
    }

    private void closeProgressAlertDialog(){
        ProgressADL.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_pictures,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_pictures_top:{
                if(null!=recyclerView){
                    recyclerView.scrollToPosition(0);
                }
                BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部 OvO");
                break;
            }
            case R.id.toolbar_pictures_help:{
                String tip = "这里可以展示软件中存储的所有图片（指日记或Blog的配图）。点击图片可以查看该大图，长按图片跳转对应日记详情（或Blog详情）。";
                BaseUtils.alertDialogToShow(this,"说明",tip);
                break;
            }
        }
        return true;
    }

    /**
     * 图片的适配器
     */
    public static class BlogPicAdapter extends RecyclerView.Adapter{
        private final Map<Integer,List<String>> picMap;
        private Context context;

        //排序辅助用
        private final List<Integer> sortDiaryId;

        private final BlogService blogService = new BlogService();
        private final DiaryService diaryService = new DiaryServiceImpl();

        public BlogPicAdapter(Context context,Map<Integer, List<String>> allBlogPic) {
            this.context = context;
            this.picMap = allBlogPic;
            //先排序，希望时间倒序展示
            sortDiaryId = new ArrayList<>();
            TreeMap<Date,Integer> treeMap = new TreeMap<>();
            picMap.keySet().forEach(integer -> {
                if(integer<0){  //约定了负数为Blog的ID
                    Date dateById = blogService.getDateById(-1*integer);
                    if(dateById!=null){
                        //这样子处理会导致，如果多个时间(key)完全一致，日记id(value)只会存一个
                        //不过毫秒级的时间完全一致也只会发生在恢复日记中的重复日记里。
                        // 所以这个bug不修
                        treeMap.put(dateById,integer);
                    }
                }else{
                    Date dateById = diaryService.getDateById(integer);
                    if(dateById!=null){
                        //这样子处理会导致，如果多个时间(key)完全一致，日记id(value)只会存一个
                        //不过毫秒级的时间完全一致也只会发生在恢复日记中的重复日记里。
                        // 所以这个bug不修
                        treeMap.put(dateById,integer);
                    }
                }

            });
            treeMap.forEach((date, integer) -> sortDiaryId.add(integer));
            Collections.reverse(sortDiaryId);
        }

        static class ViewHolder extends RecyclerView.ViewHolder{
            TextView dayView;
            RecyclerView recyclerView;
            Integer blogId;

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
            BlogPicAdapter.ViewHolder viewHolder = new BlogPicAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            BlogPicAdapter.ViewHolder newHolder = (BlogPicAdapter.ViewHolder) holder;
            Integer id = sortDiaryId.get(position);
            newHolder.blogId = id;
            ArrayList<String> picStrList = new ArrayList<>();
            picStrList.addAll(picMap.get((Integer) id));
            boolean isBlog = false;
            if(id<0){
                isBlog = true;
                Date dateById = blogService.getDateById(-1*id);
                newHolder.dayView.setText(BaseUtils.dateToString(dateById).substring(0, 10));
            }else{
                Date dateById = diaryService.getDateById(id);
                newHolder.dayView.setText(BaseUtils.dateToString(dateById).substring(0, 10));
            }
            if(isPicture(picStrList.get(0),isBlog)){
                //处理图片展示
                RecyclerView imgRecyclerView = newHolder.recyclerView;
                DiaryImageAdapter imgAdapter = new DiaryImageAdapter(picStrList);
                GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
                imgRecyclerView.setAdapter(imgAdapter);
                imgRecyclerView.setLayoutManager(layoutManager);
            }else{
                RecyclerView imgRecyclerView = newHolder.recyclerView;
                DiaryVideoAdapter imgAdapter = new DiaryVideoAdapter(picStrList);
                GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
                imgRecyclerView.setAdapter(imgAdapter);
                imgRecyclerView.setLayoutManager(layoutManager);
            }

        }

        /**
         * 判断是不是图片资源
         * @param src 资源地址
         * @param isBlog 这个资源是不是Blog
         * @return true仅仅表示他的确是一个存在的图片资源
         */
        private boolean isPicture(String src,boolean isBlog) {
            if(isBlog){
                return new BlogService().isImageSrc(src);
            }else{
                DiaryService diaryService1 = new DiaryServiceImpl();
                return diaryService1.isImageSrc(src);
            }
        }

        @Override
        public int getItemCount() {
            return sortDiaryId.size();
        }
    }
}