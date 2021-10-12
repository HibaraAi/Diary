package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.wrapper.RefreshFooterWrapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.MyConfigurationService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.MyConfigurationServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.mine.MineGameActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-14 22:07
 * @Description:
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final int CHOOSE_HEAD_IMAGE = 1;
    public static final int CHOOSE_MAIN_BG = 2;

    private long firstTime = 0;


    private NavigationView navView;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    RefreshLayout refreshLayout;
    RecyclerView recyclerView = null;
    private int nowIndex = 0;

    private DiaryService diaryService = new DiaryServiceImpl();
    private List<DiaryVo> voList = new ArrayList<>();

    private CircleImageView headImg;
    private TextView username;
    private TextView motto;
    private ImageView mainImageBg;

    private DiaryAdapter diaryAdapter;
    private MyConfigurationService configurationService = new MyConfigurationServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_main);
        bindViewAndSetListener();
        getDiaryForFirstShow();
        diaryAdapter = new DiaryAdapter(voList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(diaryAdapter);
    }


    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        toolbar = findViewById(R.id.main_toolbar);
        SharedPreferences sharedPreferences = BaseUtils.getDefaultSharedPreferences();
        boolean showUsernameInBar = sharedPreferences.getBoolean("showUsernameInBar", false);
        if(showUsernameInBar){
            CollapsingToolbarLayout toolbarLayout = findViewById(R.id.main_title);
            toolbarLayout.setTitle(MyConfiguration.getInstance().getUsername());
        }
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_activity_main);
        navView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.nav_home);
            actionBar.setTitle("");
        }
        //侧滑菜单的按钮事件在这
        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_mine:{
                    BaseUtils.gotoActivity(MainActivity.this, MineGameActivity.class);
                    break;
                }
                case R.id.nav_settings:{
                    BaseUtils.gotoActivity(MainActivity.this,SettingsActivity.class);
                    break;
                }
                case R.id.nav_help:{
                    BaseUtils.gotoActivity(MainActivity.this,HelpActivity.class);
                    break;
                }
                case R.id.nav_time:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("查找指定时间段的日记");
                    builder.setMessage("提示：如果时间段内的日记数量很多，则查找过程可能会很久，尽量缩短查找时间段。\n只选一个日期则查找当天的日记\n");
                    TextView timeOne = new TextView(MainActivity.this);
                    TextView timeTwo = new TextView(MainActivity.this);
                    timeOne.setOnClickListener(v->{
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                MainActivity.this,
                                (view, year, month, dayOfMonth) -> {
                                    timeOne.setText(year+"-"+(month+1)+"-"+dayOfMonth);
                                }, calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.setCancelable(false);
                        datePickerDialog.show();
                    });
                    timeTwo.setOnClickListener(v->{
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                MainActivity.this,
                                (view, year, month, dayOfMonth) -> {
                                    timeTwo.setText(year+"-"+(month+1)+"-"+dayOfMonth);
                                }, calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.setCancelable(false);
                        datePickerDialog.show();
                    });
                    timeOne.setBackgroundResource(R.drawable.background_input);
                    timeOne.setHint("点击选择日期一");
                    timeTwo.setBackgroundResource(R.drawable.background_input);
                    timeTwo.setHint("点击选择日期二");
                    LinearLayout linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(timeOne);
                    linearLayout.addView(timeTwo);
                    builder.setView(linearLayout);
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", (dialog1, which) -> {
                        String timeOneStr = timeOne.getText().toString();
                        String timeTwoStr = timeTwo.getText().toString();
                        if(!"".equals(timeOneStr) || !"".equals(timeTwoStr)){
                            if("".equals(timeOneStr)){
                                timeOneStr = timeTwoStr;
                            }else if("".equals(timeTwoStr)){
                                timeTwoStr = timeOneStr;
                            }
                            Intent intent = new Intent(MainActivity.this, DiaryListActivity.class);
                            intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_TIME_AXIS);
                            intent.putExtra(DiaryListActivity.DATE_ONE,timeOneStr);
                            intent.putExtra(DiaryListActivity.DATE_TWO,timeTwoStr);
                            startActivity(intent);
                        }else{
                            BaseUtils.longTipInCoast(MainActivity.this,"一个日期都不选？就是你这种不按照正常逻辑使用软件的人，才导致我们程序员要考虑各种各样的奇怪情况！");
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                case R.id.nav_temp:{
                    Intent intent = new Intent(MainActivity.this,DiaryListActivity.class);
                    intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_TEMP_DIARY);
                    startActivity(intent);
                    break;
                }
                case R.id.nav_asc:{
                    BaseUtils.gotoActivity(MainActivity.this,TimeAscActivity.class);
                    break;
                }
                case R.id.nav_label:{
                    Intent intent = new Intent(MainActivity.this,DiaryListActivity.class);
                    intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_LABEL_LIST);
                    startActivity(intent);
                    break;
                }
                case R.id.nav_former_years:{
                    Intent intent = new Intent(MainActivity.this,TimeAscActivity.class);
                    intent.putExtra(TimeAscActivity.OPEN_FROM_TYPE,TimeAscActivity.OPEN_FROM_LABEL_FORMER_YEARS);
                    startActivity(intent);
                    break;
                }
                case R.id.nav_special_day:{
                    BaseUtils.gotoActivity(this,SpecialDayActivity.class);
                    break;
                }
                default:{
                    Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            return true;
        });
        //下拉刷新控件及RecyclerView
        recyclerView = findViewById(R.id.main_recyclerview);
        refreshLayout = findViewById(R.id.main_refresh);
        refreshLayout.setRefreshHeader(new BezierRadarHeader(this)
                .setEnableHorizontalDrag(true)
                .setPrimaryColor(Color.parseColor("#FA7298")));
        refreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.FixedBehind));
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            refreshDiary();
            refreshLayout.finishRefresh();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            loadMoreDiary();
            refreshLayout.finishLoadMore();
        });
        initNavHeader();
    }

    private void initNavHeader() {
        headImg = navView.getHeaderView(0).findViewById(R.id.nav_header_head);
        String headImgInSp = MyConfiguration.getInstance().getHeadImg();
        if(null!=headImgInSp){
            Glide.with(MainActivity.this).load(headImgInSp).into(headImg);
        }
        this.headImg.setOnClickListener(v->{
//            if(null!=headImgInSp){
//                Intent intent = new Intent(MainActivity.this,ZoomImageActivity.class);
//                intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,headImgInSp);
//                startActivity(intent);
//                BaseUtils.shortTipInCoast(MainActivity.this,"短按查看大图，长按更换头像");
//            }else{
//                BaseUtils.shortTipInCoast(MainActivity.this,"短按查看大图，长按更换头像\n(但你还没有更换自己的头像)");
//            }
            BaseUtils.shortTipInSnack(this.headImg,"长按修改头像 OvO");
        });
        this.headImg.setOnLongClickListener(v -> {
            //判断有没有外部存储的写入权限
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                //如果没有立马申请
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else{
                //如果有，打开相册
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_HEAD,CHOOSE_HEAD_IMAGE);
            }
            return true;
        });
        username = navView.getHeaderView(0).findViewById(R.id.nav_header_username);
        username.setText(MyConfiguration.getInstance().getUsername());
        username.setOnClickListener(v->{
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("设置用户名");
            dialog.setMessage("请输入新的用户名");
            EditText editText = new EditText(MainActivity.this);
            editText.setBackgroundResource(R.drawable.background_input);
            dialog.setView(editText);
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                String s = editText.getText().toString();
                if(!"".equals(s)){
                    configurationService.updateUsername(s);
                    username.setText(s);
                    BaseUtils.shortTipInCoast(MainActivity.this,"更新成功，建议重启应用");
                }else{
                    BaseUtils.shortTipInCoast(this,"不准为空!  (•_•)");
                }
            });
            dialog.setNegativeButton("取消",null);
            dialog.show();
        });
        motto = navView.getHeaderView(0).findViewById(R.id.nav_header_motto);
        motto.setText(MyConfiguration.getInstance().getMotto());
        motto.setOnClickListener(v->{
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("设置个性签名");
            dialog.setMessage("请输入新的个性签名");
            EditText editText = new EditText(MainActivity.this);
            editText.setBackgroundResource(R.drawable.background_input);
            dialog.setView(editText);
            dialog.setCancelable(false);
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                String s = editText.getText().toString();
                if(!"".equals(s)){
                    configurationService.updateMotto(s);
                    motto.setText(s);
                }else{
                    BaseUtils.shortTipInCoast(this,"不准为空!  (•_•)");
                }
            });
            dialog.setNegativeButton("取消",null);
            dialog.show();
        });
        mainImageBg = findViewById(R.id.main_image_bg);
        String bgImg = MyConfiguration.getInstance().getBgImg();
        if(null!=bgImg){
            Glide.with(MainActivity.this).load(bgImg).into(mainImageBg);
        }
        mainImageBg.setOnClickListener(v->{
            BaseUtils.shortTipInSnack(mainImageBg,"长按修改背景图");
        });
        mainImageBg.setOnLongClickListener(v->{
            //判断有没有外部存储的写入权限
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                //如果没有立马申请
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else{
                //如果有，打开相册
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_MAIN_BG,CHOOSE_MAIN_BG);
            }
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    BaseUtils.shortTipInCoast(MainActivity.this,"已获取权限，请重新操作一次");
                }else{
                    BaseUtils.shortTipInCoast(MainActivity.this,"你没有授权外部存储的都且权限");
                }
                break;
            }
            default:break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_HEAD_IMAGE: {
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    if(null!=uri){
                        SimpleResult result = configurationService.updateHeadImg(uri);
                        if(result.getSuccess()){
                            BaseUtils.shortTipInCoast(MainActivity.this,"更新头像成功,建议重新启动应用");
                            String headSrc = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_HEAD_SRC,null);
                            Glide.with(MainActivity.this).load(headSrc).into(headImg);
                        }else{
                            BaseUtils.shortTipInCoast(MainActivity.this,"更新头像失败");
                        }
                    }
                }
                break;
            }
            case CHOOSE_MAIN_BG:{
                if(RESULT_OK == resultCode){
                    Uri uri = data.getData();
                    if(null!=uri){
                        SimpleResult result = configurationService.updateMainBgImage(uri);
                        if(result.getSuccess()){
                            String bgSrc = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_MAIN_IMG_BG,null);
                            Glide.with(MainActivity.this).load(bgSrc).into(mainImageBg);
                        }else{
                            BaseUtils.shortTipInCoast(MainActivity.this,"更新首页背景图失败");
                        }
                    }
                }
            }
            default:break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        List<DiaryVo> diaryVoList = diaryService.getDiaryVoList(nowIndex, 5);
        if(diaryVoList.size()==0){
            BaseUtils.shortTipInSnack(recyclerView,"没有更多日记了。");
            //refreshLayout.setNoMoreData(true);
        }else{
            voList.addAll(diaryVoList);
            nowIndex += diaryVoList.size();
            diaryAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDiary() {
        voList.clear();
        getDiaryForFirstShow();
        diaryAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //toolBar的按钮事件在这
        switch (item.getItemId()){
            case android.R.id.home :{
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.toolbar_goto_top:{
                recyclerView.scrollToPosition(0);
                BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部");
                break;
            }
            case R.id.toolbar_write:{
                BaseUtils.gotoActivity(MainActivity.this,KeepDiaryActivity.class);
                break;
            }
            case R.id.toolbar_search:{
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle("搜索日记");
                builder.setMessage("仅搜索日记标签和未加密的日记内容");
                EditText editText = new EditText(MainActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setHint("输入搜索内容");
                editText.setMaxLines(3);
                editText.setMinLines(3);
                builder.setView(editText);
                builder.setPositiveButton("搜索", (dialog, which) -> {
                    String searchValue = editText.getText().toString();
                    searchValue = searchValue.trim();
                    SimpleResult result = diaryService.searchDiary(searchValue);
                    if(result.getSuccess()){
                        Intent intent = new Intent(MainActivity.this,DiaryListActivity.class);
                        intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_SEARCH_DIARY);
                        intent.putIntegerArrayListExtra("ids",(ArrayList<Integer>)result.getData());
                        intent.putExtra("searchValue",searchValue);
                        startActivity(intent);
                    }else{
                        BaseUtils.longTipInCoast(MainActivity.this,result.getMsg());
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
            default:{
                Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }

    private void getDiaryForFirstShow(){
        voList.addAll(diaryService.getDiaryVoList(0, 5));
        nowIndex = voList.size();
        //查询置顶日记
        int topDiaryId = BaseUtils.getSharedPreference().getInt("topDiary", -1);
        if(-1!=topDiaryId){
            DiaryVo vo = (DiaryVo) diaryService.getDiaryVoById(topDiaryId).getData();
            if(null!=vo){
                vo.setLabelStr("置顶日记");
                voList.add(0, vo);
            }
        }
    }

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else {
            this.finish();
        }
    }
}