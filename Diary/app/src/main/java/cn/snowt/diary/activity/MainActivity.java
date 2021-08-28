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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import cn.snowt.diary.service.MyConfigurationService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.MyConfigurationServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
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


    private NavigationView navView;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    RefreshLayout refreshLayout;
    RecyclerView recyclerView = null;
    DiaryAdapter diaryAdapter;

    private DiaryService diaryService = new DiaryServiceImpl();
    private List<DiaryVo> voList;

    private CircleImageView headImg;
    private TextView username;
    private TextView motto;
    private ImageView mainImageBg;

    private MyConfigurationService configurationService = new MyConfigurationServiceImpl();

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
        initNavHeader();
    }

    private void initNavHeader() {
        headImg = navView.getHeaderView(0).findViewById(R.id.nav_header_head);
        String headImgInSp = MyConfiguration.getInstance().getHeadImg();
        if(null!=headImgInSp){
            Glide.with(MainActivity.this).load(headImgInSp).into(headImg);
        }
        this.headImg.setOnClickListener(v->{
            if(null!=headImgInSp){
                Intent intent = new Intent(MainActivity.this,ZoomImageActivity.class);
                intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,headImgInSp);
                startActivity(intent);
                BaseUtils.shortTipInCoast(MainActivity.this,"短按查看大图，长按更换头像");
            }else{
                BaseUtils.shortTipInCoast(MainActivity.this,"短按查看大图，长按更换头像\n(但你还没有更换自己的头像)");
            }
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
            dialog.setView(editText);
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                configurationService.updateUsername(editText.getText().toString());
                username.setText(editText.getText().toString());
                BaseUtils.shortTipInCoast(MainActivity.this,"更新成功，建议重启应用");
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
            dialog.setView(editText);
            dialog.setPositiveButton("确定", (dialog1, which) -> {
                configurationService.updateMotto(editText.getText().toString());
                motto.setText(editText.getText().toString());
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
            BaseUtils.shortTipInCoast(MainActivity.this,"长按修改背景图");
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
                    BaseUtils.shortTipInCoast(MainActivity.this,"你没有授权读取相册");
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