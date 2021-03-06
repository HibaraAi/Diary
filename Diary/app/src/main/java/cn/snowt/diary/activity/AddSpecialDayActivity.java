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
import android.view.WindowManager;
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
import cn.snowt.diary.util.PermissionUtils;
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
        //??????????????????
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_add_special_day);
        bindViewAndSetListener();
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("???????????????");
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
                    builder.setTitle("??????");
                    builder.setMessage("?????????????????????????????????????????????????????????????????????????????????????????????");
                    builder.setNegativeButton("??????????????????",null);
                    builder.setPositiveButton("???????????????", (dialog, which) -> {
                        if(!"".equals(imgSrc)){
                            //????????????
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
                                BaseUtils.shortTipInSnack(img,"????????????????????????????????????????????? ORz");
                            }
                        }
                        if (specialDayService.addOne(specialDay)) {
                            finish();
                        }else{
                            BaseUtils.shortTipInSnack(img,"????????????????????????????????? ORz");
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
     * ????????????????????????SpecialDay??????
     * @return ???????????????????????????null
     */
    private SpecialDay parseInput() {
        SpecialDay specialDay = new SpecialDay();
        String titleStr = title.getText().toString();
        titleStr = titleStr.trim();
        String remarkStr = remark.getText().toString();
        remarkStr = remarkStr.trim();
        String startDateStr = date.getText().toString();
        //????????????
        if(INPUT_LINE.equals(titleStr) || "".equals(titleStr) || titleStr.length()>10){
            BaseUtils.shortTipInSnack(img,"??????????????????,?????????????????????10????????? OvO");
            return null;
        }
        if(remarkStr.length()>100){
            BaseUtils.alertDialogToShow(this,"??????","???????????????????????????100?????????\n" +
                    "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
            return null;
        }
        if(INPUT_LINE.equals(remarkStr)){
            remarkStr = "";
        }
        if(INPUT_LINE.equals(startDateStr) || "".equals(startDateStr)){
            BaseUtils.shortTipInSnack(img,"???????????????????????????????????? OvO");
            return null;
        }
        startDateStr = startDateStr+" 00:00:00";
        Date startDate = BaseUtils.stringToDate(startDateStr);
        if(startDate.after(new Date())){
            BaseUtils.shortTipInSnack(img,"???????????????????????????????????????????????? OvO");
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
                builder.setTitle("???????????????");
                EditText editText = new EditText(this);
                editText.setHint("????????????");
                if(!INPUT_LINE.equals(title.getText())){
                    editText.setText(title.getText());
                }
                builder.setView(editText);
                builder.setNegativeButton("??????",null);
                builder.setPositiveButton("??????", (dialog, which) -> {
                    title.setText(editText.getText().toString());
                    if(editText.getText().toString().length()>10){
                        BaseUtils.shortTipInSnack(title,"????????????????????????10?????????");
                    }
                });
                builder.show();
                break;
            }
            case R.id.day_add_remark_help:
            case R.id.day_add_remark:{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("???????????????");
                EditText editText = new EditText(this);
                editText.setHint("????????????");
                if(!INPUT_LINE.equals(remark.getText())){
                    editText.setText(remark.getText());
                }
                builder.setView(editText);
                builder.setNegativeButton("??????",null);
                builder.setPositiveButton("??????", (dialog, which) -> {
                    remark.setText(editText.getText().toString());
                    if(editText.getText().toString().length()>100){
                        BaseUtils.shortTipInSnack(title,"????????????????????????100?????????");
                    }
                });
                builder.show();
                break;
            }
            case R.id.day_add_img_help:{
                //????????????
                if(!PermissionUtils.haveExternalStoragePermission(AddSpecialDayActivity.this)){
                    BaseUtils.alertDialogToShow(v.getContext(),"??????","?????????????????????????????????????????????,?????????????????????????????????????????????????????????\n????????????????????????????????????????????????????????????????????????");
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
                        //??????????????????????????????????????????????????????????????????????????????????????????
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