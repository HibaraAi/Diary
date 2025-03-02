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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.AddSpecialDayActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.MainActivity;
import cn.snowt.diary.async.SearchTask;
import cn.snowt.diary.util.BaseUtils;

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
        refreshDataForShow();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshDataForShow();
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
        adapter = new BlogAdapter(voList,context);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        System.out.println(voList);
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
                String tip = "Blog类似于非常简易的博客，可以让你边写文本边插入视频、图片。目前已支持加密存储。" +
                        "如果你将Blog界面作为首屏，则视为帮你输入密码进行了登录。" +
                        "Blog暂不支持备份和恢复，将在以后的版本支持（累了，先缓一缓）。" +
                        "虽然提供了修改的功能，但还不太完善，因为目前的修改是通过“删掉已有Blog再重新添加一条新的”实现的。" +
                        "目前Blog是独立于整个APP的，所以不支持搜索Blog，也不支持标签查找、时间轴查找功能，也不会在信息流出现，" +
                        "以后的版本再支持（先缓缓，但立flag）。";
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
}