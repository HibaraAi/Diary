package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.OutputStream;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;

public class PayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("点赞👍");
        }
        ImageView viewById = findViewById(R.id.pay_img);
        viewById.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pay);
                String s = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "xiaoxiaolePay",
                        "消消乐的收款码");
                BaseUtils.alertDialogToShow(PayActivity.this,"提示","应该保存成功了吧，存储地址：根目录/Pictures。如果没保存到就自己截图呗。");
//                try {
//                    //创建一个保存的Uri
//                    Uri saveUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//                    if (TextUtils.isEmpty(saveUri.toString())) {
//                        BaseUtils.shortTipInCoast(PayActivity.this,"保存失败");
//                    }
//                    OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
//                    //将位图写出到指定的位置
//                    //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
//                    //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
//                    //第三个参数：具体的输出流
//                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)) {
//                        BaseUtils.alertDialogToShow(PayActivity.this,"提示","保存成功，存储地址：根目录/Pictures");
//                    } else {
//                        BaseUtils.shortTipInCoast(PayActivity.this,"保存失败");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                return true;
            }
        });
        viewById.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(this,"长按保存收款码");
        });
    }

    @SuppressLint("NonConstantResourceId")
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