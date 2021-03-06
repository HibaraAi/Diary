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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
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
    public static final int REQUEST_CODE_PERMISSION = 3;

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
        //??????????????????
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_main);
        //???????????????
        //??????????????????????????????????????????
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        //??????????????????????????????
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        //??????????????????????????????
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //???????????????????????????
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LoginService loginService = new LoginServiceImpl();
            if (MyConfiguration.getInstance().isNeedFirstLoginNotice() && loginService.isFirstLoginInTheDay()) {
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
        //?????????????????????????????????
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
                    builder.setTitle("??????????????????????????????");
                    builder.setMessage("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????\n??????????????????????????????????????????\n");
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
                    timeOne.setHint("?????????????????????");
                    timeTwo.setBackgroundResource(R.drawable.background_input);
                    timeTwo.setHint("?????????????????????");
                    LinearLayout linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(timeOne);
                    linearLayout.addView(timeTwo);
                    builder.setView(linearLayout);
                    builder.setCancelable(false);
                    builder.setPositiveButton("??????", (dialog1, which) -> {
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
                            BaseUtils.longTipInCoast(MainActivity.this,"????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                        }
                    });
                    builder.setNegativeButton("??????",null);
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
        //?????????????????????RecyclerView
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
//                BaseUtils.shortTipInCoast(MainActivity.this,"???????????????????????????????????????");
//            }else{
//                BaseUtils.shortTipInCoast(MainActivity.this,"???????????????????????????????????????\n(????????????????????????????????????)");
//            }
            BaseUtils.shortTipInSnack(this.headImg,"?????????????????? OvO");
        });
        this.headImg.setOnLongClickListener(v -> {
            //??????????????????????????????????????????
            if(PermissionUtils.haveExternalStoragePermission(MainActivity.this)){
                //????????????????????????
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_HEAD,CHOOSE_HEAD_IMAGE);
            }else{
                //????????????????????????
                PermissionUtils.applyExternalStoragePermission(MainActivity.this,REQUEST_CODE_PERMISSION);
            }
            return true;
        });
        username = navView.getHeaderView(0).findViewById(R.id.nav_header_username);
        username.setText(MyConfiguration.getInstance().getUsername());
        username.setOnClickListener(v->{
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("???????????????");
            dialog.setMessage("????????????????????????");
            EditText editText = new EditText(MainActivity.this);
            editText.setHint("????????????????????????");
            editText.setBackgroundResource(R.drawable.background_input);
            dialog.setView(editText);
            dialog.setCancelable(false);
            dialog.setPositiveButton("??????", (dialog1, which) -> {
                String s = editText.getText().toString();
                if(!"".equals(s)){
                    configurationService.updateUsername(s);
                    username.setText(s);
                    BaseUtils.shortTipInCoast(MainActivity.this,"?????????????????????????????????");
                }else{
                    BaseUtils.shortTipInCoast(this,"????????????!  (???_???)");
                }
            });
            dialog.setNegativeButton("??????",null);
            dialog.show();
        });
        motto = navView.getHeaderView(0).findViewById(R.id.nav_header_motto);
        motto.setText(MyConfiguration.getInstance().getMotto());
        motto.setOnClickListener(v->{
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("??????????????????");
            dialog.setMessage("???????????????????????????");
            EditText editText = new EditText(MainActivity.this);
            editText.setHint("???????????????...");
            editText.setBackgroundResource(R.drawable.background_input);
            dialog.setView(editText);
            dialog.setCancelable(false);
            dialog.setPositiveButton("??????", (dialog1, which) -> {
                String s = editText.getText().toString();
                if(!"".equals(s)){
                    configurationService.updateMotto(s);
                    motto.setText(s);
                }else{
                    BaseUtils.shortTipInCoast(this,"????????????!  (???_???)");
                }
            });
            dialog.setNegativeButton("??????",null);
            dialog.show();
        });
        mainImageBg = findViewById(R.id.main_image_bg);
        String bgImg = MyConfiguration.getInstance().getBgImg();
        if(null!=bgImg){
            Glide.with(MainActivity.this).load(bgImg).into(mainImageBg);
        }
        mainImageBg.setOnClickListener(v->{
            BaseUtils.shortTipInSnack(mainImageBg,"????????????????????? QaQ");
        });
        mainImageBg.setOnLongClickListener(v->{
            //??????????????????????????????????????????
            if(PermissionUtils.haveExternalStoragePermission(MainActivity.this)){
                //????????????????????????
                BaseUtils.openAlbum(MainActivity.this,Constant.OPEN_ALBUM_TYPE_MAIN_BG,CHOOSE_MAIN_BG);
            }else{
                //????????????????????????
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
                    BaseUtils.shortTipInCoast(MainActivity.this,"???????????????????????????????????????");
                }else{
                    BaseUtils.shortTipInCoast(MainActivity.this,"??????????????????????????????????????????");
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
                            BaseUtils.shortTipInCoast(MainActivity.this,"??????????????????,????????????????????????");
                            String headSrc = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_HEAD_SRC,null);
                            Glide.with(MainActivity.this).load(headSrc).into(headImg);
                        }else{
                            BaseUtils.shortTipInCoast(MainActivity.this,"??????????????????");
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
                            BaseUtils.shortTipInCoast(MainActivity.this,"???????????????????????????");
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_PERMISSION:{
                if (PermissionUtils.haveExternalStoragePermission(MainActivity.this)) {
                    BaseUtils.longTipInCoast(MainActivity.this,"????????????????????????????????????,???????????????");
                }else{
                    BaseUtils.alertDialogToShow(MainActivity.this,"????????????","??????????????????????????????????????????????????????????????????????????????");
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
            BaseUtils.shortTipInSnack(recyclerView,"????????????????????????OvO");
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
        //toolBar?????????????????????
        switch (item.getItemId()){
            case android.R.id.home :{
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.toolbar_goto_top:{
                recyclerView.scrollToPosition(0);
                BaseUtils.shortTipInSnack(this.recyclerView,"??????????????? OvO");
                break;
            }
            case R.id.toolbar_write:{
                BaseUtils.gotoActivity(MainActivity.this,KeepDiaryActivity.class);
                break;
            }
            case R.id.toolbar_search:{
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false);
                builder.setTitle("????????????");
                builder.setMessage("????????????????????????????????????????????????");
                EditText editText = new EditText(MainActivity.this);
                editText.setBackgroundResource(R.drawable.background_input);
                editText.setHint("??????????????????");
                editText.setMaxLines(3);
                editText.setMinLines(3);
                builder.setView(editText);
                builder.setPositiveButton("??????", (dialog, which) -> {
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
                builder.setNegativeButton("??????",null);
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
        //??????????????????
        int topDiaryId = BaseUtils.getSharedPreference().getInt("topDiary", -1);
        if(-1!=topDiaryId){
            DiaryVo vo = (DiaryVo) diaryService.getDiaryVoById(topDiaryId).getData();
            if(null!=vo){
                vo.setLabelStr("????????????");
                voList.add(0, vo);
            }
        }
    }

    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else {
            this.finish();
        }
    }
}