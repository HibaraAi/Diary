package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.async.SearchTask;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.MyConfigurationService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.service.impl.MyConfigurationServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.note.NoteActivity;
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
    public static final int REQUEST_CODE_PERMISSION = 3;

    private long firstTime = 0;  //辅助实现双击退出程序


    private NavigationView navView;  //侧滑菜单的View
    private Toolbar toolbar;  //顶部工具栏的View
    private DrawerLayout drawerLayout;  //主界面容器View

    RefreshLayout refreshLayout;  //下拉刷新上拉加载的View
    RecyclerView recyclerView = null;  //主界面的RecyclerView
    private int nowIndex = 0;  //读取日记的游标，初始为0

    private final DiaryService diaryService = new DiaryServiceImpl();
    private final List<DiaryVo> voList = new ArrayList<>();  //存储日记展示的缓存，初始为空，通过getDiaryForFirstShow()默认加载5条

    private CircleImageView headImg;  //侧滑菜单中的头像
    private TextView username;  //侧滑菜单中的用户名
    private TextView motto;  //侧滑菜单中的个性签名
    private ImageView mainImageBg;  //主界面的背景图

    private DiaryAdapter diaryAdapter;  //主界面RecyclerView的适配器
    private final MyConfigurationService configurationService = new MyConfigurationServiceImpl();  //读取用户的默认设置
    boolean removeTip = BaseUtils.getDefaultSharedPreferences().getBoolean("removeTip", false);  //去除提示

    private long eggTimeOne;  //连续点击背景图，触发彩蛋。 第一个时间
    private int eggCount = 0;  //已经点击了多少次
    private long eggTimeTwo;

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    private TextView progressInADL;  //进度条中的进度数字
    private boolean showSearchResult = true;  //如果中途取消，则不展示结果

    Handler handler = new Handler(new Handler.Callback() {  //处理异步回调
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    if(!showSearchResult){
                        showSearchResult = true;
                        break;
                    }
                    ProgressADL.cancel();
                    SimpleResult result = (SimpleResult) msg.obj;
                    if(!result.getSuccess()){
                        BaseUtils.shortTipInSnack(recyclerView,result.getMsg());
                    }else{
                        List<DiaryVo> voList = (List<DiaryVo>) result.getData();
                        Intent intent = new Intent(MainActivity.this,DiaryListActivity.class);
                        intent.putExtra("searchValue",result.getMsg());
                        intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_ASYNC_SEARCH);
                        intent.putExtra("diaryVos", (Serializable) voList);
                        startActivity(intent);
                    }
                    break;
                }
                case MyAsyncTask.START_TASK:{
                    showProgressAlertDialog();
                    break;
                }
                case MyAsyncTask.UPDATE_PROGRESS:{
                    updateProgressAlertDialog(msg.arg1);
                    break;
                }
                default:break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_main);
        //导航栏透明
        //设置状态栏和导航栏颜色为透明
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        //设置导航栏颜色为透明
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        //设置通知栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //读取往年今日等提醒
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LoginService loginService = new LoginServiceImpl();
            if (loginService.isFirstLoginInTheDay()) {
                loginService.doFirstLoginOfTheDay();
            }
        }).start();
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
//                case R.id.nav_mine:{
//                    BaseUtils.gotoActivity(MainActivity.this, MineGameActivity.class);
//                    break;
//                }
                case R.id.nav_note:{
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    intent.putExtra(NoteActivity.OPEN_FROM,NoteActivity.OPEN_FROM_MAIN_ACTIVITY);
                    startActivity(intent);
//                    BaseUtils.gotoActivity(MainActivity.this, NoteActivity.class);
                    break;
                }
                case R.id.nav_settings:{
                    BaseUtils.gotoActivity(MainActivity.this,SettingsActivity.class);
                    break;
                }
                case R.id.nav_help:{
                    Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                    intent.putExtra(HelpActivity.OPEN_TYPE,HelpActivity.OPEN_TYPE_HELP);
                    startActivity(intent);
                    break;
                }
                case R.id.nav_time:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("查找指定时间段的日记");
                    if(!removeTip){
                        builder.setMessage("提示：如果时间段内的日记数量很多，则查找过程可能会很久，尽量缩短查找时间段。\n只选一个日期则查找当天的日记\n");
                    }else{
                        builder.setMessage("\n");
                    }
                    TextView timeOne = new TextView(MainActivity.this);
                    TextView timeTwo = new TextView(MainActivity.this);
                    TextView blank = new TextView(MainActivity.this);
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
                    timeOne.setBackgroundResource(R.drawable.edge);
                    timeOne.setPadding(5,5,5,5);
                    timeOne.setMinLines(2);
                    timeOne.setGravity(Gravity.CENTER_VERTICAL);
                    timeOne.setHint("点击选择日期一");
                    timeTwo.setBackgroundResource(R.drawable.edge);
                    timeTwo.setPadding(5,5,5,5);
                    timeTwo.setMinLines(2);
                    timeTwo.setGravity(Gravity.CENTER_VERTICAL);
                    timeTwo.setHint("点击选择日期二");
                    blank.setText(" ");
                    blank.setTextSize(2);
                    LinearLayout linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(timeOne);
                    linearLayout.addView(blank);
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
//                            BaseUtils.longTipInCoast(MainActivity.this,"一个日期都不选？就是你这种不按照正常逻辑使用软件的人，才导致我们程序员要考虑各种各样的奇怪情况！");
                              BaseUtils.alertDialogToShow(MainActivity.this,"😡怒了😡","一个日期都不选？就是你这种不按照正常逻辑使用软件的人，才导致我们软件开发要考虑各种各样的奇怪情况！");
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
                    Intent intent = new Intent(this, PicturesActivity.class);
                    intent.putExtra(PicturesActivity.OPEN_FROM_TYPE,PicturesActivity.OPEN_FROM_VIDEO);
                    startActivity(intent);
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
                    intent.putExtra(TimeAscActivity.OPEN_FROM_TYPE,TimeAscActivity.OPEN_FROM_FORMER_YEARS);
                    startActivity(intent);
                    break;
                }
                case R.id.nav_special_day:{
                    BaseUtils.gotoActivity(this,SpecialDayActivity.class);
                    break;
                }
                case R.id.nav_pic:{
                    BaseUtils.gotoActivity(this, PicturesActivity.class);
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
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
        refreshLayout.setRefreshHeader(new BezierRadarHeader(this)
                .setEnableHorizontalDrag(true)
                .setPrimaryColor(typedValue.data));
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
        if(!removeTip){
            this.headImg.setOnClickListener(v->{
                BaseUtils.shortTipInSnack(this.headImg,"长按修改头像 OvO");
            });
        }
        this.headImg.setOnLongClickListener(v -> {
            //判断有没有外部存储的写入权限
            if(PermissionUtils.haveExternalStoragePermission(MainActivity.this)){
                //如果有，打开相册
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_HEAD,CHOOSE_HEAD_IMAGE);
            }else{
                //如果没有立马申请
                PermissionUtils.applyExternalStoragePermission(MainActivity.this,REQUEST_CODE_PERMISSION);
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
            editText.setHint("用户名不建议太长");
            editText.setBackgroundResource(R.drawable.edge);
            editText.setMinLines(2);
            editText.setPadding(10,10,10,10);
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
            editText.setHint("在这里输入...");
            editText.setBackgroundResource(R.drawable.edge);
            editText.setMinLines(2);
            editText.setPadding(10,10,10,10);
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
        if(!removeTip){
            mainImageBg.setOnClickListener(v->{
                BaseUtils.shortTipInSnack(mainImageBg,"长按修改背景图 QaQ");
                if (5==eggCount){
                    eggTimeTwo = System.currentTimeMillis();
                    if(eggTimeTwo-eggTimeOne<1000){
                        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                        intent.putExtra(HelpActivity.OPEN_TYPE,HelpActivity.OPEN_TYPE_EGG);
                        startActivity(intent);
                    }
                    eggCount = 0;
                }
                if(0==eggCount){
                    eggTimeOne = System.currentTimeMillis();
                }
                eggCount++;
            });
        }
        mainImageBg.setOnLongClickListener(v->{
            //判断有没有外部存储的写入权限
            if(PermissionUtils.haveExternalStoragePermission(MainActivity.this)){
                //如果有，打开相册
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_MAIN_BG,CHOOSE_MAIN_BG);
            }else{
                //如果没有立马申请
                PermissionUtils.applyExternalStoragePermission(MainActivity.this,REQUEST_CODE_PERMISSION);
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
                    BaseUtils.shortTipInCoast(MainActivity.this,"你没有授权外部存储的读取权限");
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
                break;
            }
            case REQUEST_CODE_PERMISSION:{
                if (PermissionUtils.haveExternalStoragePermission(MainActivity.this)) {
                    BaseUtils.longTipInCoast(MainActivity.this,"已获取外部存储的读写权限,请再次操作");
                }else{
                    BaseUtils.alertDialogToShow(MainActivity.this,"授权失败","你没有授予外部存储的读写权限，你将不能使用大部分功能");
                }
                break;
            }
            default:break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        List<DiaryVo> diaryVoList = diaryService.getDiaryVoList(nowIndex, 5);
        if(diaryVoList.size()==0){
            BaseUtils.shortTipInSnack(recyclerView,"没有更多日记了。OvO");
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
                BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部 OvO");
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
                builder.setMessage("仅搜索日记及评论文本");
                EditText editText = new EditText(MainActivity.this);
                editText.setBackgroundResource(R.drawable.edge);
                editText.setPadding(10,10,10,10);
                editText.setHint("输入搜索内容");
                editText.setMaxLines(3);
                editText.setMinLines(3);
                builder.setView(editText);
                builder.setPositiveButton("搜索", (dialog, which) -> {
                    String searchValue = editText.getText().toString();
                    SearchTask searchTask = new SearchTask(handler);
                    searchTask.fullSearch(searchValue);
                });
                builder.setNegativeButton("取消", null);
                builder.show();
                break;
            }
//            case R.id.toolbar_search:{
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                builder.setCancelable(false);
//                builder.setTitle("搜索日记");
//                builder.setMessage("仅搜索日记标签和未加密的日记内容");
//                EditText editText = new EditText(MainActivity.this);
//                editText.setBackgroundResource(R.drawable.background_input);
//                editText.setHint("输入搜索内容");
//                editText.setMaxLines(3);
//                editText.setMinLines(3);
//                builder.setView(editText);
//                builder.setPositiveButton("搜索", (dialog, which) -> {
//                    String searchValue = editText.getText().toString();
//                    searchValue = searchValue.trim();
//                    if("".equals(searchValue)){
//                        BaseUtils.shortTipInSnack(recyclerView,"不允许搜索空值！！！");
//                        return;
//                    }
//                    SimpleResult result = diaryService.searchDiary(searchValue);
//                    if(result.getSuccess()){
//                        Intent intent = new Intent(MainActivity.this,DiaryListActivity.class);
//                        intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_SEARCH_DIARY);
//                        intent.putIntegerArrayListExtra("ids",(ArrayList<Integer>)result.getData());
//                        intent.putExtra("searchValue",searchValue);
//                        startActivity(intent);
//                    }else{
//                        BaseUtils.longTipInCoast(MainActivity.this,result.getMsg());
//                    }
//                });
//                builder.setNegativeButton("取消",null);
//                builder.show();
//                break;
//            }
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

    private void showProgressAlertDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(MainActivity.this);
        imageView.setImageResource(R.drawable.loading);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        imageView.setColorFilter(Color.parseColor("#FA7298"));
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof AnimatedImageDrawable){
            AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
            animatedImageDrawable.start();
        }
        linearLayout.addView(imageView);
        progressInADL = new TextView(MainActivity.this);
        progressInADL.setGravity(Gravity.CENTER);
        progressInADL.setTextSize(40);
        linearLayout.addView(progressInADL);
        builder.setView(linearLayout);
        builder.setNegativeButton("取消搜索", (dialog, which) -> {
            showSearchResult = false;
        });
        ProgressADL = builder.show();
    }

    private void updateProgressAlertDialog(Integer integer){
        progressInADL.setText(integer+"%");
    }
}