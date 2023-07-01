package cn.snowt.diary.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryImageAdapter;
import cn.snowt.diary.adapter.DiaryVideoAdapter;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.TempDiary;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.util.UriUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-29 13:48
 * @Description: 写日记的界面
 */
public class KeepDiaryActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "KeepDiaryActivity";
    public static final String OPEN_FROM_TYPE = "openFrom";
    public static final int OPEN_FROM_TEMP_DIARY = 1; //草稿箱来
    public static final int OPEN_FROM_UPDATE_DIARY = 2; //更新日记
    public static final int OPEN_FROM_QUOTE_ADD = 3; //引用了某条日记的新增

    public static final int CHOOSE_PICTURE = 1;
    public static final int CHOOSE_VIDEO = 2;

    private EditText diaryInputView;
    private ImageView addPicBtn;
    private ImageView loadLocationBtn;
    private ImageView loadWeatherBtn;
    private ImageView addLabelBtn;
    private ImageView addDateBtn;
    private ImageView addVideoBtn;
    private TextView locationView;
    private TextView weatherView;
    private TextView labelView;
    private TextView dateView;
    private RecyclerView picRecyclerView = null;
    private RecyclerView videoRecyclerView = null;

    /**
     * 存储选中图片的cache路径
     */
    private static List<String> imageTempSrcList;
    /**
     * 存储选中视频的cache路径
     */
    private static ArrayList<String> videoTempSrcList;
    /**
     * 图片区使用RecyclerView，它的适配器
     */
    @SuppressLint("StaticFieldLeak")
    private static DiaryImageAdapter imageAdapter;
    /**
     * 视频区使用RecyclerView，它的适配器
     */
    @SuppressLint("StaticFieldLeak")
    private static DiaryVideoAdapter videoAdapter;

    /**
     * 草稿的id
     */
    private int tempDiaryId = -1;

    /**
     * 需要更新的日记ID
     */
    private int updateDiaryId = -1;

    /**
     * 引用日记的id
     */
    private String quoteDiaryId = "";

    private DiaryService diaryService = new DiaryServiceImpl();

    /**
     * 日记图片的最大数量
     */
    private static int IMG_MAX_NUM = 50;

    /**
     * 日记视频的最大数量
     */
    private static int VIDEO_MAX_NUM = 10;

    boolean removeTip = BaseUtils.getDefaultSharedPreferences().getBoolean("removeTip", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_keep_diary);
        bindViewAndSetListener();
        openTip();
        Intent intent = getIntent();
        if(null!=intent){
            switch (intent.getIntExtra(OPEN_FROM_TYPE,-1)) {
                case OPEN_FROM_TEMP_DIARY:{
                    //从草稿箱来，回写草稿到输入框
                    tempDiaryId = intent.getIntExtra("id", -1);
                    List<TempDiary> tempDiaries = LitePal.where("id = ?",tempDiaryId+"").find(TempDiary.class);
                    if(!tempDiaries.isEmpty()){
                        diaryInputView.setText(tempDiaries.get(0).getContent());
                    }
                    break;
                }
                case OPEN_FROM_UPDATE_DIARY:{
                    //从编辑已有日记来,将日记内容回写到输入框
                    updateDiaryId = intent.getIntExtra("id", -1);
                    SimpleResult result = diaryService.getDiaryVoById(updateDiaryId);
                    if(result!=null){
                        DiaryVo vo = (DiaryVo)result.getData();
                        if(null==vo){
                            BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"你刚刚貌似在尝试编辑已删除的日记。");
                            KeepDiaryActivity.this.finish();
                        }else{
                            diaryInputView.setText(vo.getContent());
                        }
                    }
                    addPicBtn.setEnabled(false);
                    addLabelBtn.setEnabled(false);
                    loadLocationBtn.setEnabled(false);
                    loadWeatherBtn.setEnabled(false);
                    addDateBtn.setEnabled(false);
                    findViewById(R.id.keep_diary_btn_image_tip).setEnabled(false);
                    locationView.setEnabled(false);
                    weatherView.setEnabled(false);
                    labelView.setEnabled(false);
                    dateView.setEnabled(false);
                    addVideoBtn.setEnabled(false);
                    findViewById(R.id.keep_diary_video_tip).setEnabled(false);
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setTitle("修改日记错别字");
                    if(!removeTip){
                        BaseUtils.alertDialogToShow(KeepDiaryActivity.this,"提示","此功能仅用于修改日记正文中的错别字，其他的均不能改。后续可能会删除此功能。");
                    }
                    break;
                }
                case OPEN_FROM_QUOTE_ADD:{
                    quoteDiaryId = intent.getStringExtra("uuid");
                    if(null==quoteDiaryId || "".equals(quoteDiaryId)){
                        BaseUtils.shortTipInCoast(this,"貌似出现了未知错误呢...");
                        finish();
                    }else{
                        if("noUuid".equals(quoteDiaryId)){
                            BaseUtils.longTipInCoast(this,"由于设计原因，旧日记缺少一个属性。现尝试修复，稍后再试...");
                            //为没有uuid的日记添加uuid
                            new Thread(() -> {
                                List<Diary> diaryList = LitePal.findAll(Diary.class);
                                diaryList.forEach(diary -> {
                                    if (null==diary.getMyUuid() || "".equals(diary.getMyUuid())){
                                        diary.setMyUuid(UUID.randomUUID().toString());
                                        diary.update(diary.getId());
                                    }
                                });
                            }).start();
                            finish();
                        }else{
                            String quoteDiaryStr = intent.getStringExtra("str");
                            BaseUtils.alertDialogToShow(this,"引用追更的提示","原文为:\n"+quoteDiaryStr);
                        }
                    }
                    break;
                }
                default:break;
            }
        }
    }

    /**
     * 打开这个界面时的提示
     */
    private void openTip() {
        boolean needTip = BaseUtils.getDefaultSharedPreferences().getBoolean("inputTip", false);
        if(needTip){
            BaseUtils.longTipInCoast(KeepDiaryActivity.this,"记得切换单机输入法哦(⊙o⊙)");
        }
    }

    private void bindViewAndSetListener() {
        initToolbar();
        diaryInputView = findViewById(R.id.keep_diary_input);
        addPicBtn = findViewById(R.id.keep_diary_btn_image);
        addLabelBtn = findViewById(R.id.keep_diary_btn_label);
        loadLocationBtn = findViewById(R.id.keep_diary_btn_location);
        loadWeatherBtn = findViewById(R.id.keep_diary_btn_weather);
        addDateBtn = findViewById(R.id.keep_diary_btn_date);
        addVideoBtn = findViewById(R.id.keep_diary_btn_video);
        locationView = findViewById(R.id.keep_diary_location);
        weatherView = findViewById(R.id.keep_diary_weather);
        labelView = findViewById(R.id.keep_diary_label);
        dateView = findViewById(R.id.keep_diary_date);
        //处理图片展示区
        imageTempSrcList = new ArrayList<>();
        imageAdapter = new DiaryImageAdapter((ArrayList<String>) imageTempSrcList);
        picRecyclerView = findViewById(R.id.keep_diary_pic_area);
        GridLayoutManager layoutManager = new GridLayoutManager(KeepDiaryActivity.this, 4);
        picRecyclerView.setAdapter(imageAdapter);
        picRecyclerView.setLayoutManager(layoutManager);
        //处理视频展示区
        videoTempSrcList = new ArrayList<>();
        videoAdapter = new DiaryVideoAdapter((ArrayList<String>) videoTempSrcList);
        videoRecyclerView = findViewById(R.id.keep_diary_video_area);
        GridLayoutManager videoLayoutManager = new GridLayoutManager(KeepDiaryActivity.this, 2);
        videoRecyclerView.setAdapter(videoAdapter);
        videoRecyclerView.setLayoutManager(videoLayoutManager);
        addPicBtn.setOnClickListener(this);
        addLabelBtn.setOnClickListener(this);
        loadLocationBtn.setOnClickListener(this);
        loadWeatherBtn.setOnClickListener(this);
        addDateBtn.setOnClickListener(this);
        addVideoBtn.setOnClickListener(this);
        findViewById(R.id.keep_diary_btn_image_tip).setOnClickListener(this);
        findViewById(R.id.keep_diary_video_tip).setOnClickListener(this);
        locationView.setOnClickListener(this);
        weatherView.setOnClickListener(this);
        labelView.setOnClickListener(this);
        dateView.setOnClickListener(this);
        //输入字数监听
        diaryInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>1950){
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setTitle(s.length()+"(MAX:2000)");
                }
            }
        });
    }

    private void initToolbar(){
        Toolbar toolbar = findViewById(R.id.keep_diary_toolbar);
        //toolbar.setTitleTextColor(Color.parseColor("#D84214"));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("日记编辑");
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
                    askSave(diaryInputView.getText().toString());
                }else{
                    finish();
                }
                break;
            }
            case R.id.toolbar_diary_send:{
                String diaryInputStr = diaryInputView.getText().toString();
                if("".equals(diaryInputStr)){
                    BaseUtils.shortTipInSnack(diaryInputView,"空白日记有什么好记录的呢? OvO");
                }else if(diaryInputStr.length()>2000){
                    BaseUtils.shortTipInSnack(diaryInputView,"你以为写书呢？大于2000字了，禁止保存 ORz");
                }else{
                    Date date = null;
                    String dateStr = dateView.getText().toString();
                    if(!"".equals(dateStr)){
                        date = BaseUtils.stringToDate(dateStr);
                    }
                    SimpleResult result;
                    if(-1!=updateDiaryId){
                        Diary diary = new Diary();
                        diary.setContent(diaryInputStr);
                        diary.setId(updateDiaryId);
                        result = diaryService.updateDiaryContentById(diary);
                    }else{
                        result = diaryService.addOneByArgs(diaryInputStr,
                                labelView.getText().toString(),
                                locationView.getText().toString(),
                                weatherView.getText().toString(),
                                imageTempSrcList,date,videoTempSrcList,quoteDiaryId);
                    }
                    if(result.getSuccess()){
                        clearTempPinInEdit();
                        //又判断一次？？？？吃饱了撑？？？
                        if(-1!=updateDiaryId){
                            BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"日记的文本内容已更新，请手动刷新!");
                        }else{
                            BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"新日记已存储，请手动刷新!");
                        }
                        if(-1!=tempDiaryId){
                            LitePal.delete(TempDiary.class,tempDiaryId);
                        }
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

    /**
     * 如果视频和图片的文件过大，提示文件过大，多等待
     */
    private void romSizeTip() {
        long size  = videoTempSrcList.stream().mapToLong(FileUtils::getFileSize).sum();
        size += imageTempSrcList.stream().mapToLong(FileUtils::getFileSize).sum();
        if(size>200*1024*1024){
            //超过500M，给予提示
            BaseUtils.longTipInCoast(this,"视频和图片超过了200M，需要保存较久" +
                    "\n(卡住属于正常现象，请耐心等待)");
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep_diary_btn_image_tip:
            case R.id.keep_diary_btn_image:{
                //判断权限
                if(!PermissionUtils.haveExternalStoragePermission(KeepDiaryActivity.this)){
                    BaseUtils.alertDialogToShow(v.getContext(),"提示","你并没有授予外部存储的读写权限,在你许可之前，你只能记录纯文字的日记，你可以去修改头像的地方进行授权外部存储的读写权限");
                }else{
                    if(imageTempSrcList.size()>=IMG_MAX_NUM){
                        BaseUtils.shortTipInSnack(v,"你最多选择"+IMG_MAX_NUM+"张图片。长按图片可将其移除。QaQ");
                    }else{
                        BaseUtils.openAlbum(KeepDiaryActivity.this, Constant.OPEN_ALBUM_TYPE_KEEP_DIARY_ADD_PIC,CHOOSE_PICTURE);
                    }
                }
                break;
            }
            case R.id.keep_diary_location:
            case R.id.keep_diary_btn_location:{
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                dialog.setTitle("所处位置");
                if(!removeTip){
                    dialog.setMessage("暂时不支持读取地理位置，请手动输入");
                }
                EditText editText = new EditText(KeepDiaryActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setMinLines(4);
                editText.setMaxLines(4);
                editText.setGravity(Gravity.START);
                editText.setHint("空输入视为删除原有输入");
                editText.setPadding(30,10,30,10);
                dialog.setView(editText);
                dialog.setCancelable(false);
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
                if(!removeTip){
                    dialog.setTitle("暂时不支持读取当地天气信息，请手动选择");
                }
                String[] items = {Weather.WEATHER_CLOUDY,Weather.WEATHER_RAIN,
                        Weather.WEATHER_SUNNY,Weather.WEATHER_HOT,Weather.WEATHER_OVERCAST,
                        Weather.WEATHER_SNOW,Weather.WEATHER_HAIL,"自定义"};
                dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if("自定义".equals(items[which])){
                            android.app.AlertDialog.Builder dialog2 = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                            dialog2.setTitle("手动输入天气");
                            dialog2.setMessage("15字符以内");
                            EditText editText = new EditText(KeepDiaryActivity.this);
                            editText.setBackgroundResource(R.drawable.background_input);
                            editText.setMinLines(4);
                            editText.setMaxLines(4);
                            editText.setGravity(Gravity.START);
                            editText.setHint("天气情况");
                            editText.setPadding(30,10,30,10);
                            dialog2.setView(editText);
                            dialog2.setCancelable(false);
                            dialog2.setPositiveButton("添加", (dialog3, which2) -> {
                                String s = editText.getText().toString();
                                if(s.length()>15){
                                    BaseUtils.longTipInCoast(KeepDiaryActivity.this,"都说了自定义天气只能15字符以内咯");
                                }else{
                                    weatherView.setText(s);
                                }
                            });
                            dialog2.setNegativeButton("取消",null);
                            dialog2.show();
                        }else{
                            weatherView.setText(items[which]);
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", null);
                dialog.setNegativeButton("取消", (dialog12, which) -> weatherView.setText(""));
                dialog.show();
                break;
            }
            case R.id.keep_diary_label:
            case R.id.keep_diary_btn_label:{
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(KeepDiaryActivity.this);
                dialog.setTitle("请输入标签");
                if(!removeTip){
                    dialog.setMessage("可输入多个标签，但请用英文#隔开。\ne.g:#美食##周末#");
                }
                EditText editText = new EditText(KeepDiaryActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setMinLines(4);
                editText.setMaxLines(4);
                editText.setGravity(Gravity.START);
                editText.setHint("空输入视为删除原有输入");
                editText.setPadding(30,10,30,10);
                dialog.setView(editText);
                dialog.setCancelable(false);
                dialog.setPositiveButton("添加", (dialog1, which) -> {
                    String labelStr = editText.getText().toString();
                    labelStr = labelStr.trim();
                    int num = 0;
                    for (char c : labelStr.toCharArray()) {
                        if(c == '#'){
                            num++;
                        }
                    }
                    boolean flag = (num%2==0 && num!=0);
                    if(labelStr.length()<=30 && flag){
                        labelView.setText(labelStr);
                    }else{
                        BaseUtils.longTipInCoast(v.getContext(),"标签总字符数不超过30,格式必须正确");
                    }
                });
                dialog.setNegativeButton("取消",null);
                dialog.show();
                break;
            }
            case R.id.keep_diary_date:
            case R.id.keep_diary_btn_date:{
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        KeepDiaryActivity.this,
                        (view, year, month, dayOfMonth) -> {

                            new TimePickerDialog(KeepDiaryActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    String dateStr = year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute + ":" + "00";
                                    dateView.setText(dateStr);
                                }
                            }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true).show();
                        }, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnCancelListener(dialog -> dateView.setText(""));
                datePickerDialog.show();
                break;
            }
            case R.id.keep_diary_btn_video:
            case R.id.keep_diary_video_tip:{
                //判断权限
                if(!PermissionUtils.haveExternalStoragePermission(KeepDiaryActivity.this)){
                    BaseUtils.alertDialogToShow(v.getContext(),"提示","你并没有授予外部存储的读写权限,在你许可之前，你只能记录纯文字的日记，你可以去修改头像的地方进行授权外部存储的读写权限");
                }else{
                    if(imageTempSrcList.size()>=VIDEO_MAX_NUM){
                        BaseUtils.shortTipInSnack(v,"你最多选择"+VIDEO_MAX_NUM+"个视频。长按视频可将其移除。QaQ");
                    }else{
                        BaseUtils.longTipInCoast(this,"请选择需要的视频...\n视频过大会卡住，请耐心等待");
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("video/*");
                        this.startActivityForResult(intent,CHOOSE_VIDEO);
                    }
                }
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
                        imageTempSrcList.add(UriUtils.getFileAbsolutePath(KeepDiaryActivity.this,uri));
                        imageAdapter.notifyDataSetChanged();
                        //判断视频/图片有多大，给予等待提示
                        romSizeTip();
                    }
                }
                break;
            }
            case CHOOSE_VIDEO:{
                if(RESULT_OK==resultCode){
                    Uri uri = data.getData();
                    if(null!=uri){
                        videoTempSrcList.add(UriUtils.getFileAbsolutePath(KeepDiaryActivity.this,uri));
                        videoAdapter.notifyDataSetChanged();
                        //判断视频/图片有多大，给予等待提示
                        romSizeTip();
                    }
                }
            }
            default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearTempPinInEdit();
    }

    @Override
    public void onBackPressed() {
        if(!"".equals(diaryInputView.getText().toString())){
            askSave(diaryInputView.getText().toString());
        }else{
            finish();
        }
    }

    /**
     * 询问是否保存到草稿箱
     */
    private void askSave(String content){
        if(updateDiaryId==-1){
            //==-1表示非更新日记，才需要问
            AlertDialog.Builder builder=new AlertDialog.Builder(KeepDiaryActivity.this);
            builder.setTitle("提示：");
            builder.setMessage("你即将退出日记编辑，但还未保存此日记，是否将本条记录保存到草稿箱？\n如果你是从草稿箱打开这个页面，不保存的话，草稿箱中的记录会被删除!");
            builder.setNegativeButton("保存到草稿箱", (dialog, which) -> {
                if(content.length()>2000){
                    BaseUtils.shortTipInSnack(diaryInputView,"你以为写书呢？大于2000字了，禁止保存 ORz");
                }else{
                    boolean saveSuccess = false;
                    TempDiary tempDiary = new TempDiary(null, content);
                    if(tempDiaryId!=-1){
                        int update = tempDiary.update(tempDiaryId);
                        if(0!=update){
                            saveSuccess = true;
                        }
                    }else{
                        saveSuccess = tempDiary.save();
                    }
                    if(saveSuccess){
                        finish();
                    }else{
                        BaseUtils.shortTipInCoast(KeepDiaryActivity.this,"保存失败，请重试!");
                    }
                }
            });
            builder.setPositiveButton("直接退出",(dialog, which) -> {
                if(-1!=tempDiaryId){
                    LitePal.delete(TempDiary.class,tempDiaryId);
                }
                finish();
            });
            builder.show();
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("你即将退出日记更新，确定更新请使用发送按钮，不更新请继续退出")
                    .setPositiveButton("继续退出", (dialog, which) -> finish())
                    .setNegativeButton("继续编辑",null)
                    .show();
        }
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
     * 删除编辑区中的视频
     * @param src
     */
    @SuppressLint("NotifyDataSetChanged")
    public static void deleteTempVideoInEdit(String src){
        videoTempSrcList.remove(src);
        File file = new File(src);
        if(file.exists()){
            file.delete();
            Log.i(TAG,"------删除一个临时视频");
        }
        videoAdapter.notifyDataSetChanged();
    }

    /**
     * 删除选择图片/视频时的所有临时缓存
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
        Iterator<String> iterator2 = videoTempSrcList.iterator();
        while (iterator2.hasNext()) {
            String tempSrc = iterator2.next();
            File file = new File(tempSrc);
            if(file.exists()){
                file.delete();
                Log.i(TAG,"------删除一个临时视频");
            }
        }
    }
}