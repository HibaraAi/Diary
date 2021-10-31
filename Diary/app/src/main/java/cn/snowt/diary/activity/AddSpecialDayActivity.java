package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.entity.SpecialDay;
import cn.snowt.diary.service.SpecialDayService;
import cn.snowt.diary.service.impl.SpecialDayServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.UriUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-11, 0011 16:06:58
 * @Description:
 */
public class AddSpecialDayActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int CHOOSE_PICTURE = 1;
    private static final String INPUT_LINE = "___________________";

    private ActionBar actionBar;
    private TextView date;
    private TextView dateHelp;
    private TextView title;
    private TextView titleHelp;
    private TextView remark;
    private TextView remarkHelp;
    private TextView imgHelp;
    private ImageView img;
    private SwitchCompat notice;

    private String imgSrc = "";
    private SpecialDayService specialDayService = new SpecialDayServiceImpl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_special_day);
        bindViewAndSetListener();
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("新增纪念日");
        }
        date = findViewById(R.id.day_add_startDate);
        dateHelp = findViewById(R.id.day_add_startDate_help);
        title = findViewById(R.id.day_add_title);
        titleHelp = findViewById(R.id.day_add_title_help);
        remarkHelp = findViewById(R.id.day_add_remark_help);
        remark = findViewById(R.id.day_add_remark);
        imgHelp = findViewById(R.id.day_add_img_help);
        img = findViewById(R.id.day_add_img);
        notice = findViewById(R.id.day_add_notice);
        date.setOnClickListener(this);
        dateHelp.setOnClickListener(this);
        title.setOnClickListener(this);
        titleHelp.setOnClickListener(this);
        remarkHelp.setOnClickListener(this);
        remark.setOnClickListener(this);
        imgHelp.setOnClickListener(this);
        img.setOnClickListener(this);
        remark.setMovementMethod(ScrollingMovementMethod.getInstance());
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
                finish();
                break;
            }
            case R.id.toolbar_diary_send:{
                SpecialDay specialDay = parseInput();
                if(null!=specialDay){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setMessage("纪念日一旦提交，今后再也不能修改任何内容，也不能删除，你要再检查一下这条纪念日的设置吗？");
                    builder.setNegativeButton("我再检查一下",null);
                    builder.setPositiveButton("现在就提交", (dialog, which) -> {
                        if(!"".equals(imgSrc)){
                            //存储图片
                            File path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"image/SpecialDay/");
                            if(!path.exists()){
                                path.mkdirs();
                            }
                            String absolutePath = path.getAbsolutePath();
                            File finalSavePath = new File((absolutePath + "/" + UUID.randomUUID().toString() + ".hibara"));
                            try {
                                UriUtils.copyStream(new FileInputStream(imgSrc),new FileOutputStream(finalSavePath));
                                specialDay.setImageSrc(finalSavePath.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                                BaseUtils.shortTipInSnack(img,"图片存储失败，已停止保存纪念日 ORz");
                            }
                        }
                        if (specialDayService.addOne(specialDay)) {
                            finish();
                        }else{
                            BaseUtils.shortTipInSnack(img,"不明原因导致提交失败了 ORz");
                        }

                    });
                    builder.show();
                }
                break;
            }
            default:break;
        }
        return true;
    }

    /**
     * 将输入解析成一个SpecialDay实例
     * @return 如果解析失败，返回null
     */
    private SpecialDay parseInput() {
        SpecialDay specialDay = new SpecialDay();
        String titleStr = title.getText().toString();
        titleStr = titleStr.trim();
        String remarkStr = remark.getText().toString();
        remarkStr = remarkStr.trim();
        String startDateStr = date.getText().toString();
        //数据判断
        if(INPUT_LINE.equals(titleStr) || "".equals(titleStr) || titleStr.length()>10){
            BaseUtils.shortTipInSnack(img,"标题不能为空,且字数不能超过10个字符 OvO");
            return null;
        }
        if(remarkStr.length()>100){
            BaseUtils.alertDialogToShow(this,"提示","备注的字数不能超过100个字符\n" +
                    "建议你在纪念日当天新增一条日记存储超长备注，纪念日详情界面可以方便的跳转到该天的所有日记。");
            return null;
        }
        if(INPUT_LINE.equals(remarkStr)){
            remarkStr = "";
        }
        if(INPUT_LINE.equals(startDateStr) || "".equals(startDateStr)){
            BaseUtils.shortTipInSnack(img,"纪念日的起始时间不能为空 OvO");
            return null;
        }
        startDateStr = startDateStr+" 00:00:00";
        Date startDate = BaseUtils.stringToDate(startDateStr);
        if(startDate.after(new Date())){
            BaseUtils.shortTipInSnack(img,"纪念日的起始时间不能晚于当下时间 OvO");
            return null;
        }
        specialDay.setTitle(titleStr);
        specialDay.setImageSrc(imgSrc);
        specialDay.setRemark(remarkStr);
        specialDay.setStartDate(startDate);
        specialDay.setStopDate(null);
        specialDay.setNeedNotice(notice.isChecked());
        return specialDay;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.day_add_startDate:
            case R.id.day_add_startDate_help:{
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            date.setText(year+"-"+(month+1)+"-"+dayOfMonth);
                        }, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                break;
            }
            case R.id.day_add_title_help:
            case R.id.day_add_title:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请输入标题");
                EditText editText = new EditText(this);
                editText.setHint("在此输入");
                if(!INPUT_LINE.equals(title.getText())){
                    editText.setText(title.getText());
                }
                builder.setView(editText);
                builder.setNegativeButton("取消",null);
                builder.setPositiveButton("确定", (dialog, which) -> {
                    title.setText(editText.getText().toString());
                    if(editText.getText().toString().length()>10){
                        BaseUtils.shortTipInSnack(title,"标题字数不能大于10个字符");
                    }
                });
                builder.show();
                break;
            }
            case R.id.day_add_remark_help:
            case R.id.day_add_remark:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("请输入备注");
                EditText editText = new EditText(this);
                editText.setHint("在此输入");
                if(!INPUT_LINE.equals(remark.getText())){
                    editText.setText(remark.getText());
                }
                builder.setView(editText);
                builder.setNegativeButton("取消",null);
                builder.setPositiveButton("确定", (dialog, which) -> {
                    remark.setText(editText.getText().toString());
                    if(editText.getText().toString().length()>100){
                        BaseUtils.shortTipInSnack(title,"备注字数不能大于100个字符");
                    }
                });
                builder.show();
                break;
            }
            case R.id.day_add_img_help:{
                //判断权限
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    BaseUtils.alertDialogToShow(v.getContext(),"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能为纪念日添加配图。\n你可以去修改头像的地方进行授权外部存储的读写权限");
                }else{
                    BaseUtils.openAlbum(this, Constant.OPEN_ALBUM_TYPE_ADD_DAY_ADD_PIC,CHOOSE_PICTURE);
                }
                break;
            }
            default:break;
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "CheckResult"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PICTURE:{
                if(RESULT_OK==resultCode){
                    Uri uri = data.getData();
                    if(null!=uri){
                        imgSrc = UriUtils.getFileAbsolutePath(this,uri);
                        new RequestOptions()
                                .placeholder(R.drawable.load_image)
                                .fallback( R.drawable.bad_image)
                                .error(R.drawable.bad_image);
                        RequestOptions options = RequestOptions
                                .bitmapTransform(new RoundedCorners(30));
                        Glide.with(this)
                                .load(uri)
                                .apply(options)
                                .into(img);
                    }
                }
                break;
            }
            default:break;
        }
    }
}