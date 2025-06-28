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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;

public class PayActivity extends AppCompatActivity {

    private boolean showThanks = true;

    private final String thanksTip = "觉得好用就为我点个赞吧，主要是想知道有多少人在用这个app，但软件本身没有联网，就用这个收款码作为点赞吧。长按下面的图片保存收款码。\n仅需一分钱就能点赞，确定不鼓励一下我吗？";
    private final String payTip = "打赏金额随意，但一定要备注上是因为“消消乐APP”付款的，这样你才有机会上感谢名单。（长按图片保存二维码）";

    private Button btn;
    private TextView tipView;
    private ImageView imageView;

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
        tipView = findViewById(R.id.oaiusfhgoaiusfh);
        btn = findViewById(R.id.ohsosngushfudbsg);
        imageView = findViewById(R.id.pay_img);
        imageView.setOnLongClickListener(v -> {
            if(showThanks){
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pay);
                MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "xiaoxiaolePay",
                        "消消乐的收款码");
            }else{
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reward);
                MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "xiaoxiaolePay2",
                        "消消乐的收款码");
            }
            BaseUtils.alertDialogToShow(PayActivity.this,"提示","应该保存成功了吧，存储地址：根目录/Pictures。如果没保存到就自己截图呗。");
            return true;
        });
        imageView.setOnClickListener(v->{
            BaseUtils.shortTipInCoast(this,"长按保存收款码");
        });
        btn.setOnClickListener(v -> {
            if(showThanks){
                //当前展示的是点赞，需要转换为奶茶
                if(null!=supportActionBar){
                    supportActionBar.setDisplayHomeAsUpEnabled(true);
                    supportActionBar.setTitle("打赏\uD83D\uDCB0");
                }
                tipView.setText(payTip);
                imageView.setImageResource(R.drawable.reward);
                btn.setText("算了，还是1分钱点个赞吧");
            }else{
                //当前展示的是奶茶，需要转换为点赞
                if(null!=supportActionBar){
                    supportActionBar.setDisplayHomeAsUpEnabled(true);
                    supportActionBar.setTitle("点赞👍");
                }
                tipView.setText(thanksTip);
                imageView.setImageResource(R.drawable.pay);
                btn.setText("有赏，请作者喝奶茶");
            }
            showThanks = !showThanks;
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