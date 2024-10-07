package cn.snowt.diary.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.async.GetDiaryByIdsTask;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.PDFUtils;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.note.FinishActivity;
import cn.snowt.note.Item;
import cn.snowt.note.NoteActivity;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-17 20:19
 * @Description: 时间升序浏览专用
 */
public class TimeAscActivity extends AppCompatActivity {
    public static final String OPEN_FROM_TYPE = "openFrom";
    public static final int OPEN_FROM_FORMER_YEARS = 1;
    public static final int OPEN_FROM_SIMPLE_DIARY_LIST = 2;
    public static final int OPEN_FROM_DELETE_LIST = 3;
    private List<DiaryVo> voList = new ArrayList<>();
    private DiaryService diaryService = new DiaryServiceImpl();
    private DiaryAdapter diaryAdapter;
    private int nowIndex = 0;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView = null;
    private FloatingActionButton fab;
    private ActionBar actionBar;
    private Integer intExtra;
    private List<Integer> realToDelIds = new ArrayList<>();  //批量删除中，真正需要删除的Id

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    Handler handler = new Handler(new Handler.Callback() {  //处理异步回调
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    ProgressADL.cancel();
                    SimpleResult result = (SimpleResult) msg.obj;
                    if(!result.getSuccess()){
                        BaseUtils.shortTipInSnack(recyclerView,"【BUG】程序异常222...");
                    }else{
                        switch (intExtra) {
                            case OPEN_FROM_SIMPLE_DIARY_LIST:{
                                voList = (List<DiaryVo>) result.getData();
                                afterShowByIds2(getIntent().getIntExtra("sortType",1));
                                diaryAdapter = new DiaryAdapter(voList);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TimeAscActivity.this);
                                recyclerView.setLayoutManager(linearLayoutManager);
                                recyclerView.setAdapter(diaryAdapter);
                                break;
                            }
                            case OPEN_FROM_DELETE_LIST:{
                                voList = (List<DiaryVo>) result.getData();
                                afterNewDeleteList();
                                diaryAdapter = new DiaryAdapter(voList);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TimeAscActivity.this);
                                recyclerView.setLayoutManager(linearLayoutManager);
                                recyclerView.setAdapter(diaryAdapter);
                                break;
                            }
                            default:{
                                BaseUtils.shortTipInSnack(recyclerView,"【BUG】程序异常222...");
                                break;
                            }
                        }

                    }
                    break;
                }
                case MyAsyncTask.START_TASK:{
                    showProgressAlertDialog();
                    break;
                }
                default:break;
            }
            return false;
        }
    });

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_time_asc);
        bindViewAndSetListener();
        Intent intent = getIntent();
        intExtra = intent.getIntExtra(OPEN_FROM_TYPE, -1);
        switch (intExtra){
            case OPEN_FROM_FORMER_YEARS:{
                actionBar.setTitle("往年今日");
                stopRefreshLayout();
                showForMerYears();
                break;
            }
            case OPEN_FROM_SIMPLE_DIARY_LIST:{
                actionBar.setTitle("临时信息流");
                stopRefreshLayout();
                //showByIds(intent.getIntegerArrayListExtra("ids"),intent.getIntExtra("sortType",1));
                showByIds2(intent.getIntegerArrayListExtra("ids"),intent.getIntExtra("sortType",1));
                break;
            }
            case OPEN_FROM_DELETE_LIST:{
//                oldDeleteList();
                newDeleteList();
                break;
            }
            default:{
                getDiaryForFirstShow();
                break;
            }
        }
        diaryAdapter = new DiaryAdapter(voList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(diaryAdapter);
    }

    /**
     * 展示给定的几个id
     * @param ids
     * @param sortType 排序类型，1为时间倒序，2为时间顺序
     */
    private void showByIds(ArrayList<Integer> ids,int sortType) {
        ids.forEach(id->voList.add((DiaryVo) diaryService.getDiaryVoById(id).getData()));
        if(sortType==2){
            System.out.println("************-----------------showByIds");
            voList.sort((o1, o2) -> {
                Date o1Date = BaseUtils.stringToDate(o1.getModifiedDate());
                Date o2Date = BaseUtils.stringToDate(o2.getModifiedDate());
                if (o1Date.after(o2Date)) {
                    return 1;
                } else if (o1Date.before(o2Date)) {
                    return -1;
                } else {
                    return 0;
                }
            });
        }
    }

    /**
     * 展示给定的几个id
     *
     * 异步加载，提供加载动画
     * @param ids
     * @param sortType 排序类型，1为时间倒序，2为时间顺序
     */
    private void showByIds2(ArrayList<Integer> ids,int sortType) {
        GetDiaryByIdsTask task = new GetDiaryByIdsTask(handler);
        task.getDiaryVoByIds(ids);
    }

    private void afterShowByIds2(int sortType){
        if(sortType==2){
            voList.sort((o1, o2) -> {
                Date o1Date = BaseUtils.stringToDate(o1.getModifiedDate());
                Date o2Date = BaseUtils.stringToDate(o2.getModifiedDate());
                if (o1Date.after(o2Date)) {
                    return 1;
                } else if (o1Date.before(o2Date)) {
                    return -1;
                } else {
                    return 0;
                }
            });
        }
    }

    private void oldDeleteList(){
        ArrayList<Integer> delIds = getIntent().getIntegerArrayListExtra("delIds");
        //处理ids到diaryVoList
        delIds.forEach(id->{
            SimpleResult result = diaryService.getDiaryVoById(id);
            if(result.getSuccess()){
                //只有成功查到的才展示
                realToDelIds.add(id);
                voList.add((DiaryVo) result.getData());
            }
        });
        actionBar.setTitle("批量删除-条数:"+realToDelIds.size());
        stopRefreshLayout();
        BaseUtils.alertDialogToShow(this,"最后一次提示","这些是你选中且数据库中存在的项目，请浏览确认。\n\n下一个删除按钮将不再有任何提示！");
    }


    private void newDeleteList() {
        GetDiaryByIdsTask task = new GetDiaryByIdsTask(handler);
        task.getDiaryVoByIds(getIntent().getIntegerArrayListExtra("delIds"));
    }

    private void afterNewDeleteList(){
        voList.forEach(diaryVo -> realToDelIds.add(diaryVo.getId()));
        actionBar.setTitle("批量删除-条数:"+realToDelIds.size());
        stopRefreshLayout();
        BaseUtils.alertDialogToShow(this,"最后一次提示","这些是你选中且数据库中存在的项目，请浏览确认。\n\n下一个删除按钮将不再有任何提示！");

    }

    /**
     * 停用下拉刷新和上拉加载
     */
    private void stopRefreshLayout() {
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            BaseUtils.shortTipInSnack(recyclerView,"不支持刷新 Orz");
            refreshLayout.finishRefresh();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            BaseUtils.shortTipInSnack(recyclerView,"没有更多了 QaQ");
            refreshLayout.finishLoadMore();
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("时间升序浏览");
        }
        fab = findViewById(R.id.asc_fab);
        fab.setOnClickListener(v->{
            recyclerView.scrollToPosition(0);
            BaseUtils.shortTipInSnack(this.recyclerView,"已返回顶部 OvO");
        });
        recyclerView = findViewById(R.id.asc_recyclerview);
        View parent = (View) recyclerView.getParent().getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            //parent.setBackgroundResource(R.drawable.day_detail_bg);
            parent.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
//            parent.setBackgroundResource(R.drawable.night_bg);
            parent.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        refreshLayout = findViewById(R.id.asc_refresh);
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
    }

    /**
     * 原先此Activity只用于时间升序展示日记，但后来增加了其他功能
     */
    private void getDiaryForFirstShow(){
        voList.addAll(diaryService.getDiaryVoListAsc(0, 5));
        nowIndex = voList.size();
    }

    /**
     * 时间升序浏览时用的刷新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    private void refreshDiary() {
        voList.clear();
        getDiaryForFirstShow();
        diaryAdapter.notifyDataSetChanged();
    }

    /**
     * 时间升序浏览时用的加载更多
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        List<DiaryVo> diaryVoList = diaryService.getDiaryVoListAsc(nowIndex, 5);
        if(diaryVoList.size()==0){
            BaseUtils.shortTipInSnack(recyclerView,"没有更多日记了。QaQ");
        }else{
            voList.addAll(diaryVoList);
            nowIndex += diaryVoList.size();
            diaryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载往年今日的日记
     */
    private void showForMerYears(){
        voList = diaryService.getFormerYear(new Date());
        if(voList.isEmpty()){
            BaseUtils.shortTipInSnack(recyclerView,"往年的今日没有记录 UnU");
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_time_asc_flow:{
                Context context = TimeAscActivity.this;
                if(!PermissionUtils.haveExternalStoragePermission(context)){
                    BaseUtils.shortTipInSnack(recyclerView,"没有外部存储权限，不允许导出！");
                    break;
                }
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("验证你的身份");
                builder.setMessage("\n将临时信息流的日记导出到一个PDF文件中，如果你在信息流删除了某个日记，请重新进入信息流，否则会导出失败。");
                EditText pinView = new EditText(context);
                pinView.setBackgroundResource(R.drawable.edge);
                pinView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pinView.setMinLines(2);
                pinView.setMaxLines(2);
                pinView.setHint("输入登录密码");
                pinView.setPadding(10,10,10,10);
                builder.setView(pinView);
                builder.setCancelable(false);
                builder.setPositiveButton("验证密码并导出PDF", (dialog, which) -> {
                    String pinInput = pinView.getText().toString();
                    //空输入
                    if("".equals(pinInput)){
                        BaseUtils.longTipInCoast(context,"你不验证密码我就不导出。");
                    }//密码不对
                    else if (!BaseUtils.getSharedPreference().getString("loginPassword","")
                            .equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+pinInput))){
                        BaseUtils.longTipInCoast(context,"登录密码不对!!!");
                    }//导出PDF
                    else{
                        new Thread(() -> {
                            PDFUtils.createPdf(voList,TimeAscActivity.this,(ViewGroup) fab.getParent());
                            BaseUtils.simpleSysNotice(TimeAscActivity.this,"PDF导出成功！");
                        }).start();
                        final String tip = "已开始导出PDF，完成时将在通知栏通知你。(过程比较慢，在一次导出完成前，请不要再次发起导出PDF请求。)\n\n导出的文件存放路径为【Hibara\\Dairy\\putput\\PDF】\n\n";
                        BaseUtils.alertDialogToShow(this,"提示",tip);
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.show();
                break;
            }
            case R.id.toolbar_day_sel:{
                if(realToDelIds.size()>0){
                    new Thread(() -> realToDelIds.forEach(id-> diaryService.deleteById(id))).start();
                    BaseUtils.shortTipInCoast(this,"删除已在后台进行...");
                }else{
                    BaseUtils.shortTipInCoast(this,"0条数据，不需要执行删除操作.");
                }
                finish();
                break;
            }
            default:break;
        }
        return true;
    }

    /**
     * 根据当前界面类型，动态展示右上角的标题栏按钮，仅在临时信息流可以导出pdf
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (intExtra) {
            //如果是临时信息流，展示“导出为PDF”按钮
            case OPEN_FROM_SIMPLE_DIARY_LIST:{
                getMenuInflater().inflate(R.menu.toolbar_time_asc,menu);
                break;
            }
            //如果是批量删除，展示”删除“按钮
            case OPEN_FROM_DELETE_LIST:{
                getMenuInflater().inflate(R.menu.toolbar_day_detail,menu);
                break;
            }
            default:break;
        }
        return true;
    }

    private void showProgressAlertDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        LinearLayout linearLayout = new LinearLayout(TimeAscActivity.this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(TimeAscActivity.this);
        imageView.setImageResource(R.drawable.loading);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof AnimatedImageDrawable){
            AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
            animatedImageDrawable.start();
        }
        linearLayout.addView(imageView);
        builder.setView(linearLayout);
        ProgressADL = builder.show();
    }

}