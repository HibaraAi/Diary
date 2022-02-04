package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import cn.snowt.diary.R;
import cn.snowt.diary.service.SpecialDayService;
import cn.snowt.diary.service.impl.SpecialDayServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.SpecialDayVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-10 11:46:44
 * @Description:
 */
public class DayDetailActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String OPEN_TYPE = "openFrom";
    public static final int OPEN_TYPE_DETAIL = 1;

    private ActionBar actionBar;
    private ImageView img;
    private TextView daySum;
    private TextView startDateHelp;
    private TextView startDate;
    private TextView endDateHelp;
    private TextView endDate;
    private TextView daySumSmall;
    private TextView disNow;
    private TextView remark;
    private SwitchCompat notice;

    private SpecialDayService specialDayService = new SpecialDayServiceImpl();

    private SpecialDayVo dayVo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);
        bindViewAndSetListener();
        Intent intent = getIntent();
        int openType = intent.getIntExtra(OPEN_TYPE,-1);
        switch (openType) {
            case OPEN_TYPE_DETAIL:{
                dayVo = (SpecialDayVo) intent.getSerializableExtra("dayVo");
                showDayVo();
                break;
            }
            default:{
                BaseUtils.shortTipInCoast(this,"发生错误了");
                this.finish();
            }
        }
    }

    /**
     * 展示详情
     */
    @SuppressLint({"SetTextI18n", "CheckResult"})
    private void showDayVo() {
        if(dayVo==null){
            return;
        }
        actionBar.setTitle(dayVo.getTitle());
        new RequestOptions()
                .placeholder(R.drawable.load_image)
                .fallback( R.drawable.bad_image)
                .error(R.drawable.bad_image);
        RequestOptions options = RequestOptions
                .bitmapTransform(new RoundedCorners(30));
        Glide.with(this)
                .load(dayVo.getImageSrc())
                .apply(options)
                .into(img);
        daySum.setText(dayVo.getSumDay()+"天");
        startDate.setText(dayVo.getStartDate());
        endDate.setText(dayVo.getEndDate());
        daySumSmall.setText(dayVo.getSumDay()+"天 ("+ dayVo.getSumDayYear()+")");
        disNow.setText(dayVo.getDistanceNow()+"天 ("+ dayVo.getDistanceNowYear()+")");
        remark.setText(dayVo.getRemark());
        notice.setChecked(dayVo.getNeedNotice());
        if(dayVo.getStop()){
            notice.setVisibility(View.GONE);
        }
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("纪念日");
        }
        img = findViewById(R.id.day_detail_img);
        daySum = findViewById(R.id.day_detail_daySum);
        startDate = findViewById(R.id.day_detail_date);
        startDateHelp = findViewById(R.id.day_detail_date_help);
        endDate = findViewById(R.id.day_detail_date_end);
        endDateHelp = findViewById(R.id.day_detail_date_end_help);
        daySumSmall = findViewById(R.id.day_detail_daySum_small);
        disNow = findViewById(R.id.day_detail_dis_now);
        remark = findViewById(R.id.day_detail_remark);
        notice = findViewById(R.id.day_detail_notice);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        startDateHelp.setOnClickListener(this);
        endDateHelp.setOnClickListener(this);
        notice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            specialDayService.changeNoticeState(dayVo.getId(),isChecked);
            dayVo.setNeedNotice(isChecked);
        });
        img.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_day_sel:{
                if(dayVo!=null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setMessage("是否删除这个纪念日?");
                    builder.setNegativeButton("刚刚手滑了",null);
                    builder.setPositiveButton("确认删除", (dialog, which) -> {
                        specialDayService.delById(dayVo.getId());
                        this.finish();
                    });
                    builder.setCancelable(false);
                    builder.show();
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
            case R.id.day_detail_date_help:
            case R.id.day_detail_date:{
                String substring = dayVo.getStartDate().substring(0, 10);
                Intent intent = new Intent(this, DiaryListActivity.class);
                intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_TIME_AXIS);
                intent.putExtra(DiaryListActivity.DATE_ONE,substring);
                intent.putExtra(DiaryListActivity.DATE_TWO,substring);
                startActivity(intent);
                break;
            }
            case R.id.day_detail_date_end_help:
            case R.id.day_detail_date_end:{
                if(dayVo.getStop()){
                    String substring = dayVo.getEndDate().substring(0, 10);
                    Intent intent = new Intent(this, DiaryListActivity.class);
                    intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_TIME_AXIS);
                    intent.putExtra(DiaryListActivity.DATE_ONE,substring);
                    intent.putExtra(DiaryListActivity.DATE_TWO,substring);
                    startActivity(intent);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setMessage("是否停止这条纪念日的计数？\n此操作不可逆");
                    builder.setNegativeButton("刚刚手滑了",null);
                    builder.setPositiveButton("停止计数", (dialog, which) -> {
                        specialDayService.stopSpecialDayById(dayVo.getId());
                        notice.setChecked(false);
                        notice.setEnabled(false);
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
                break;
            }
            case R.id.day_detail_img :{
                if(null!=dayVo){
//                    Intent intent = new Intent(DayDetailActivity.this, BigImgActivity.class);
//                    intent.putExtra(BigImgActivity.INTENT_DATA_IMG_POSITION,dayVo.getImageSrc());
//                    ArrayList<String> list = new ArrayList<>(1);
//                    list.add(dayVo.getImageSrc());
//                    intent.putStringArrayListExtra(BigImgActivity.INTENT_DATA_IMG_LIST,list);
//                    DayDetailActivity.this.startActivity(intent);
                    Intent intent = new Intent(DayDetailActivity.this, ZoomImageActivity.class);
                    intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,dayVo.getImageSrc());
                    DayDetailActivity.this.startActivity(intent);
                }
            }
            default:break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_day_detail,menu);
        return true;
    }
}