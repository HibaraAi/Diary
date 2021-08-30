package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryImageAdapter;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-29 13:48
 * @Description: 写日记的界面
 */
public class KeepDiaryActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "KeepDiaryActivity";
    public static final int CHOOSE_PICTURE = 1;

    private EditText diaryInputView;
    private ImageView addPicBtn;
    private ImageView loadLocationBtn;
    private ImageView loadWeatherBtn;
    private ImageView addLabelBtn;
    private TextView locationView;
    private TextView weatherView;
    private TextView labelView;
    private RecyclerView recyclerView = null;

    /**
     * 存储选中图片的cache路径
     */
    private static List<String> imageTempSrcList;
    /**
     * 图片区使用RecyclerView，它的适配器
     */
    @SuppressLint("StaticFieldLeak")
    private static DiaryImageAdapter imageAdapter;

    private DiaryService diaryService = new DiaryServiceImpl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keep_diary);
        bindViewAndSetListener();
    }

    private void bindViewAndSetListener() {
        initToolbar();
        diaryInputView = findViewById(R.id.keep_diary_input);
        addPicBtn = findViewById(R.id.keep_diary_btn_image);
        addLabelBtn = findViewById(R.id.keep_diary_btn_label);
        loadLocationBtn = findViewById(R.id.keep_diary_btn_location);
        loadWeatherBtn = findViewById(R.id.keep_diary_btn_weather);
        locationView = findViewById(R.id.keep_diary_location);
        weatherView = findViewById(R.id.keep_diary_weather);
        labelView = findViewById(R.id.keep_diary_label);
        //处理图片展示区
        imageTempSrcList = new ArrayList<>();
        imageAdapter = new DiaryImageAdapter(imageTempSrcList);
        recyclerView = findViewById(R.id.keep_diary_pic_area);
        GridLayoutManager layoutManager = new GridLayoutManager(KeepDiaryActivity.this, 4);
        recyclerView.setAdapter(imageAdapter);
        recyclerView.setLayoutManager(layoutManager);

        addPicBtn.setOnClickListener(this);
        addLabelBtn.setOnClickListener(this);
        loadLocationBtn.setOnClickListener(this);
        loadWeatherBtn.setOnClickListener(this);
        findViewById(R.id.keep_diary_btn_image_tip).setOnClickListener(this);
        locationView.setOnClickListener(this);
        weatherView.setOnClickListener(this);
        labelView.setOnClickListener(this);
    }

    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.keep_diary_toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#D84214"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Keep A Diary");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_keep_diary,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                if(!"".equals(diaryInputView.getText().toString())){
                    AlertDialog.Builder builder=new AlertDialog.Builder(KeepDiaryActivity.this);
                    builder.setTitle("提示：");
                    builder.setMessage("你即将退出日记编辑，但还未保存此日记，是否将本条记录保存到草稿箱？");
                    builder.setNegativeButton("保存到草稿箱", (dialog, which) -> {
                        BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"保存草稿箱功能待实现");
                        finish();
                    });
                    builder.setPositiveButton("直接退出",(dialog, which) -> finish());
                    builder.show();
                }else{
                    finish();
                }
                break;
            }
            case R.id.toolbar_diary_send:{
                String diaryInputStr = diaryInputView.getText().toString();
                if("".equals(diaryInputStr)){
                    BaseUtils.shortTipInSnack(diaryInputView,"空白日记有什么好记录的呢?");
                }else{
                    SimpleResult result = diaryService.addOneByArgs(diaryInputStr,
                            labelView.getText().toString(),
                            locationView.getText().toString(),
                            weatherView.getText().toString(),
                            imageTempSrcList);
                    if(result.getSuccess()){
                        clearTempPinInEdit();
                        BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"新日记已存储，请手动刷新!");
                        finish();
                    }else{
                        BaseUtils.shortTipInSnack(diaryInputView,result.getMsg());
                    }
                }
                break;
            }
            default:break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep_diary_btn_image_tip:
            case R.id.keep_diary_btn_image:{
                if(imageTempSrcList.size()>=8){
                    BaseUtils.shortTipInSnack(v,"你最多选择8张图片。长按图片可将其移除。");
                }else{
                    BaseUtils.openAlbum(KeepDiaryActivity.this, Constant.OPEN_ALBUM_TYPE_KEEP_DIARY_ADD_PIC,CHOOSE_PICTURE);
                }
                break;
            }
            case R.id.keep_diary_location:
            case R.id.keep_diary_btn_location:{
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                dialog.setTitle("所处位置");
                dialog.setMessage("暂时不支持读取地理位置，请手动输入");
                EditText editText = new EditText(KeepDiaryActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setMinLines(4);
                editText.setMaxLines(4);
                editText.setGravity(Gravity.START);
                editText.setHint("空输入视为删除原有输入");
                editText.setPadding(30,10,30,10);
                dialog.setView(editText);
                dialog.setPositiveButton("添加", (dialog1, which) -> {
                    locationView.setText(editText.getText().toString());
                });
                dialog.setNegativeButton("取消",null);
                dialog.show();
                break;
            }
            case R.id.keep_diary_weather:
            case R.id.keep_diary_btn_weather:{

                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                dialog.setTitle("暂时不支持读取当地天气信息，请手动选择");
                String[] items = {Weather.WEATHER_CLOUDY,Weather.WEATHER_RAIN,
                        Weather.WEATHER_SUNNY,Weather.WEATHER_OVERCAST,
                        Weather.WEATHER_SNOW,Weather.WEATHER_HAIL};
                dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weatherView.setText(items[which]);
                    }
                });
                dialog.setPositiveButton("确定", null);
                dialog.setNegativeButton("取消", (dialog12, which) -> weatherView.setText(""));
                dialog.show();
                break;
            }
            case R.id.keep_diary_label:
            case R.id.keep_diary_btn_label:{
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                dialog.setTitle("请输入标签");
                dialog.setMessage("可输入多个标签，但请用#隔开。\ne.g:#美食##周末#");
                EditText editText = new EditText(KeepDiaryActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setMinLines(4);
                editText.setMaxLines(4);
                editText.setGravity(Gravity.START);
                editText.setHint("空输入视为删除原有输入");
                editText.setPadding(30,10,30,10);
                dialog.setView(editText);
                dialog.setPositiveButton("添加", (dialog1, which) -> {
                    labelView.setText(editText.getText().toString());
                });
                dialog.setNegativeButton("取消",null);
                dialog.show();
                break;
            }
            default:break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PICTURE:{
                if(RESULT_OK==resultCode){
                    Uri uri = data.getData();
                    if(null!=uri){
//                        if(null==imageTempSrcList){
//                            imageTempSrcList = new ArrayList<>();
//                        }
                        imageTempSrcList.add(UriUtils.getFileAbsolutePath(KeepDiaryActivity.this,uri));
                        imageAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearTempPinInEdit();
    }

    /**
     * 删除编辑区中的图片
     * @param src
     */
    @SuppressLint("NotifyDataSetChanged")
    public static void deleteTempPicInEdit(String src){
        imageTempSrcList.remove(src);
        File file = new File(src);
        if(file.exists()){
            file.delete();
            Log.i(TAG,"------删除一张临时图片");
        }
        imageAdapter.notifyDataSetChanged();
    }

    /**
     * 删除选择图片时的所有临时缓存
     */
    private void clearTempPinInEdit(){
        Iterator<String> iterator = imageTempSrcList.iterator();
        while (iterator.hasNext()) {
            String tempSrc = iterator.next();
            File file = new File(tempSrc);
            if(file.exists()){
                file.delete();
                Log.i(TAG,"------删除一张临时图片");
            }
        }
    }
}