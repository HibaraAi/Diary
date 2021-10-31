package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.BigImgAdapter;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-19, 0019 10:39:08
 * @Description: 查看大图组件，使用ViewPager达到左右滑动切换图片
 */
public class BigImgActivity extends AppCompatActivity {
    public static final String INTENT_DATA_IMG_LIST = "imgSrcList";
    public static final String INTENT_DATA_IMG_POSITION = "position";

    private ViewPager2 vp;
    private Integer position;
    private ArrayList<String> urls = new ArrayList<>();
    private BigImgAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_img);
        ActionBar actionBar;
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("日记配图");
        }
        Intent intent = getIntent();
        urls = intent.getStringArrayListExtra(INTENT_DATA_IMG_LIST);
        position = intent.getIntExtra(INTENT_DATA_IMG_POSITION,0);
        vp = findViewById(R.id.ac_big_img_vp);
        adapter = new BigImgAdapter(urls);
        vp.setAdapter(adapter);
        vp.setCurrentItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
        }
        return true;
    }
}