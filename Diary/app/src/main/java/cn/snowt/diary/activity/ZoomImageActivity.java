package cn.snowt.diary.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;

import java.io.File;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.view.ZoomImageView;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-28 09:24
 * @Description: 查看大图
 */
public class ZoomImageActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_SRC = "imgSrc";
    public static final String TAG = "ZoomImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);
        ZoomImageView imageView = findViewById(R.id.zoom_image);
        Intent intent = getIntent();
        String imgSrc = intent.getStringExtra(EXTRA_IMAGE_SRC);
        if(imgSrc==null || "".equals(imgSrc)){
            BaseUtils.shortTipInSnack(imageView,"图片有误");
        }else{
            Glide.with(this).load(imgSrc).into(imageView);
        }
    }
}