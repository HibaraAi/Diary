package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.litepal.LitePal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.adapter.PicDayAdapter;
import cn.snowt.diary.adapter.VideoDayAdapter;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.entity.Video;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.VideoService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.service.impl.VideoServiceImpl;
import cn.snowt.diary.util.BaseUtils;

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

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_pictrues);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        int openFromType = intent.getIntExtra(OPEN_FROM_TYPE, OPEN_FROM_PICTURE);
        if(openFromType==OPEN_FROM_PICTURE){
            if(null!=actionBar){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("图库");
            }
            DrawingService drawingService = new DrawIngServiceImpl();
            Map<Integer, List<Drawing>> allPic = drawingService.getAllPic();
            if(allPic.isEmpty()){
                BaseUtils.longTipInCoast(this,"日记暂时没有存图片");
            }else{
                recyclerView = findViewById(R.id.at_pic_rv);
                PicDayAdapter adapter = new PicDayAdapter(allPic);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        }else if (openFromType==OPEN_FROM_VIDEO){
            if(null!=actionBar){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("视频库");
            }
            VideoService videoService = new VideoServiceImpl();
            Map<Integer, List<Video>> allVideos = videoService.getAllVideos();
            if (allVideos.isEmpty()) {
                BaseUtils.longTipInCoast(this,"日记暂时没有存视频");
            }else{
                recyclerView = findViewById(R.id.at_pic_rv);
                VideoDayAdapter adapter = new VideoDayAdapter(allVideos);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(adapter);
            }
        }

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
                String tip = "这里可以展示软件中存储的所有图片。点击图片可以查看该大图，长按图片跳转对应日记详情。";
                BaseUtils.alertDialogToShow(this,"说明",tip);
                break;
            }
        }
        return true;
    }
}