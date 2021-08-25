package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.BezierRadarHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAdapter;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-14 22:07
 * @Description:
 */
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private NavigationView navView;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    RefreshLayout refreshLayout;
    RecyclerView recyclerView = null;
    DiaryAdapter diaryAdapter;

    private DiaryService diaryService = new DiaryServiceImpl();
    private List<DiaryVo> voList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViewAndSetListener();
        voList = getDiaryForFirstShow();
        diaryAdapter = new DiaryAdapter(voList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(diaryAdapter);
    }


    @SuppressLint("NonConstantResourceId")
    private void bindViewAndSetListener(){
        toolbar = findViewById(R.id.main_toolbar);
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
                case R.id.nav_settings:{
                    BaseUtils.gotoActivity(MainActivity.this,SetPasswordActivity.class);
                    break;
                }
                case R.id.nav_help:{
                    BaseUtils.gotoActivity(MainActivity.this,HelpActivity.class);
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
        refreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            Log.e(TAG,"在这里刷新");
            refreshDiary();
            refreshLayout.finishRefresh();
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            Log.e(TAG,"在这里加载更多");
            loadMoreDiary();
            refreshLayout.finishLoadMore();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMoreDiary() {
        for (int i =0; i<5;i++){
            Random random = new Random();
            int nextInt = random.nextInt(10);
            String s = nextInt+"结尾说的有点仓促了。阿离对四魂之玉许愿后，就和犬夜叉瞬移到了现代古井，然而他们话都还没说一句，犬夜叉就被古井带回了战国，自此，古井就再也不能进行穿越了。过了3年，阿离对犬夜叉的思念再次打通了古井，穿越到战国和犬夜叉在一起了。在阿离读高中3年的同时，弥勒和珊瑚结婚了，并且生下来一对双胞胎妹妹(超可爱的！)；七宝则进行了狐妖考试，想成为维护世界和平的大妖怪；琥珀也成为了优秀的除妖师，和云母一起战斗，刀刀斋还给琥珀打造了一个蛮酷的武器；玲则是留在了枫婆婆家，杀生丸想让她过着人类的生活先，等她长大了再让玲决定要不要跟着杀生丸。此外还有一个广播剧，杀生丸和玲求婚了。完结撒花";
            s = s+s;
            List<String> list = new ArrayList<>();
            for(int j=0;j<nextInt;j++){
                list.add(j+"");
            }
            DiaryVo diaryVo = new DiaryVo(i+1,s,BaseUtils.dateToString(new Date()),"多云  25","广东省广州市",list);
            voList.add(diaryVo);
        }
        diaryAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDiary() {
        voList.clear();
        for (int i =0; i<5;i++){
            Random random = new Random();
            int nextInt = random.nextInt(9);
            String s = nextInt+"结尾说的有点仓促了。阿离对四魂之玉许愿后，就和犬夜叉瞬移到了现代古井，然而他们话都还没说一句，犬夜叉就被古井带回了战国，自此，古井就再也不能进行穿越了。过了3年，阿离对犬夜叉的思念再次打通了古井，穿越到战国和犬夜叉在一起了。在阿离读高中3年的同时，弥勒和珊瑚结婚了，并且生下来一对双胞胎妹妹(超可爱的！)；七宝则进行了狐妖考试，想成为维护世界和平的大妖怪；琥珀也成为了优秀的除妖师，和云母一起战斗，刀刀斋还给琥珀打造了一个蛮酷的武器；玲则是留在了枫婆婆家，杀生丸想让她过着人类的生活先，等她长大了再让玲决定要不要跟着杀生丸。此外还有一个广播剧，杀生丸和玲求婚了。完结撒花";
            s = s+s+s+s+s+s+s;
            List<String> list = new ArrayList<>();
            for(int j=0;j<nextInt;j++){
                list.add(j+"");
            }
            DiaryVo diaryVo = new DiaryVo(i+1,s,BaseUtils.dateToString(new Date()),"多云  25","广东省广州市",list);
            voList.add(diaryVo);
        }
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
            default:{
                Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }

    private List<DiaryVo> getDiaryForFirstShow(){
        List<DiaryVo> vos= new ArrayList<>();
        for (int i =0; i<5;i++){
            Random random = new Random();
            int nextInt = random.nextInt(10);
            String s = nextInt+"结尾说的有点仓促了。阿离对四魂之玉许愿后，就和犬夜叉瞬移到了现代古井，然而他们话都还没说一句，犬夜叉就被古井带回了战国，自此，古井就再也不能进行穿越了。过了3年，阿离对犬夜叉的思念再次打通了古井，穿越到战国和犬夜叉在一起了。在阿离读高中3年的同时，弥勒和珊瑚结婚了，并且生下来一对双胞胎妹妹(超可爱的！)；七宝则进行了狐妖考试，想成为维护世界和平的大妖怪；琥珀也成为了优秀的除妖师，和云母一起战斗，刀刀斋还给琥珀打造了一个蛮酷的武器；玲则是留在了枫婆婆家，杀生丸想让她过着人类的生活先，等她长大了再让玲决定要不要跟着杀生丸。此外还有一个广播剧，杀生丸和玲求婚了。完结撒花";
            s = s+s;
            List<String> list = new ArrayList<>();
            for(int j=0;j<nextInt;j++){
                list.add(j+"");
            }
            DiaryVo diaryVo = new DiaryVo(i+1,s,BaseUtils.dateToString(new Date()),"多云  25","广东省广州市",list);
            vos.add(diaryVo);
        }
        return vos;
    }
}