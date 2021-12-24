package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.litepal.LitePal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.adapter.PicDayAdapter;
import cn.snowt.diary.entity.Drawing;
import cn.snowt.diary.service.DrawingService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.DrawIngServiceImpl;
import cn.snowt.diary.util.BaseUtils;

/**
 * 查看所有图片
 */
public class PictruesActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictrues);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
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