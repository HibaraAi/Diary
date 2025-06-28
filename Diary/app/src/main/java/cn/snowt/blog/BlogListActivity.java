package cn.snowt.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.AddSpecialDayActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.MainActivity;
import cn.snowt.diary.activity.PicturesActivity;
import cn.snowt.diary.async.LoadingBlogListTask;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.async.SearchTask;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:26.
 * @Description :
 */
public class BlogListActivity extends AppCompatActivity {

    public static final Integer OPEN_FROM_MAIN_ACTIVITY = 1;  //从主界面打开的
    public static final Integer OPEN_FROM_BEFORE_LOGIN = 2;  //登录之前就已经打开
    public static final String OPEN_FROM = "open_from";
    //记录这个open_from的值
    private Integer OPEN_FROM_VALUE = 0;

    private Context context;

    private BlogService blogService;
    private List<BlogSimpleVo> voList = new ArrayList<>();
    private BlogAdapter adapter;
    private RecyclerView recyclerView;

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    SimpleResult result = (SimpleResult) msg.obj;
                    if(result.getSuccess()){
                        voList = (List<BlogSimpleVo>) result.getData();
                        adapter = new BlogAdapter(voList,context);
                        GridLayoutManager layoutManager = new GridLayoutManager(context, 1);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                    closeProgressAlertDialog();
                    BaseUtils.shortTipInSnack(recyclerView,result.getMsg());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        context = BlogListActivity.this;
        setContentView(R.layout.activity_blog_list);
        bindViewAndSetListener();
        blogService = new BlogService();
        asyncRefreshDataForShow();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        asyncRefreshDataForShow();
    }

    private void bindViewAndSetListener() {
        //设置背景色
        ConstraintLayout constraintLayout = findViewById(R.id.act_blog_list);
        if(this.getResources().getConfiguration().uiMode == 0x11){
            constraintLayout.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
            constraintLayout.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        //绑定相关控件，包括标题栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        // 初始化设置Toolbar
        toolbar.setTitle("Blog");
        setSupportActionBar(toolbar);
        //标题栏
        ActionBar actionBar = getSupportActionBar();

        OPEN_FROM_VALUE = getIntent().getIntExtra(OPEN_FROM,0);
        if (OPEN_FROM_VALUE.equals(OPEN_FROM_MAIN_ACTIVITY)){
            //如果是主界面来的，显示返回键
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }else if(OPEN_FROM_VALUE.equals(OPEN_FROM_BEFORE_LOGIN)){
            //登录之前就打开了，显示home键
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.nav_home);
            }
        }
        recyclerView = findViewById(R.id.act_blog_list_rw);
    }

    /**
     * 刷新数据
     */
    private void refreshDataForShow() {
        voList = blogService.getAllBlogs();
        voList.sort((o1, o2) -> {
            Date o1Date = BaseUtils.stringToDate(o1.getDate());
            Date o2Date = BaseUtils.stringToDate(o2.getDate());
            if (o1Date.after(o2Date)) {
                return -1;
            } else if (o1Date.before(o2Date)) {
                return 1;
            } else {
                return 0;
            }
        });
        adapter = new BlogAdapter(voList,context);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        System.out.println(voList);
    }

    /**
     * 异步刷新数据
     */
    private void asyncRefreshDataForShow() {
        LoadingBlogListTask loadingBlogListTask = new LoadingBlogListTask(handler);
        loadingBlogListTask.getAllBlogsDesc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_special_day,menu);
        return true;
    }

    /**
     * 响应标题栏按钮的点击事件
     * @param item item
     * @return true
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                if(OPEN_FROM_VALUE.equals(OPEN_FROM_BEFORE_LOGIN)){
                    BaseUtils.gotoActivity(BlogListActivity.this, MainActivity.class);
                }
                finish();
                break;
            }
            case R.id.toolbar_day_help:{
                String tip = "Blog类似于非常简易的博客，" +
                        "可以让你边写文本边插入视频、图片，正文文本支持加密存储，标签、标题、媒体资源不会加密。" +
                        "虽然提供了修改的功能，但还不太完善，因为目前的修改是通过“删掉已有Blog再重新添加一条新的”实现的。\n" +
                        "如果你将Blog界面作为首屏，则视为帮你输入密码进行了登录。\n" +
                        "Blog是后来加入的功能，所以和日记主程序还是融合的不太好，主界面的信息流是不显示Blog的，因为Blog和日记的存储结构是不一样的，这会导致一些问题，" +
                        "例如我并不知道2025年3月15日这条日记的下一个记录是该显示日记还是Blog，" +
                        "所以主界面的信息流不展示Blog。不过，你可以在标签查找、时间轴查找、关键字搜索中同时看到Blog和日记，并由此跳转的“临时信息流”可以同时展示。\n" +
                        "目前Blog暂不支持导出为PDF、另存为图片和批量删除，将在以后的版本支持（继续立Flag）";
                BaseUtils.alertDialogToShow(this,"Blog说明",tip);
                break;
            }
            case R.id.toolbar_day_add:{
                BaseUtils.gotoActivity(this, EditBlogActivity.class);
                break;
            }
            default:{
                Toast.makeText(context, "未定义的按钮", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }

    private void showProgressAlertDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setCancelable(false);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(context);
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

    private void closeProgressAlertDialog(){
        ProgressADL.cancel();
    }
}