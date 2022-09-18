package cn.snowt.diary.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.mine.MineGameActivity;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-14 22:08
 * @Description: 登录界面
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private  Button[] buttons = new Button[12];
    private TextView password;
    private TextView tip;

    private LoginService loginService = new LoginServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoNight();
        //横屏、竖屏的布局处理
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_login_h);
        } else if (this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_login);
        }
        bindViewAndSetListener();
        doWhenFirstLogin();
        initKeyboard();
    }

    /**
     * 处理自动夜间模式
     */
    private void autoNight() {
        if (MyConfiguration.getInstance().isAutoNight()) {
            String now = BaseUtils.dateToString(new Date());
            int nowTimeInt = Integer.parseInt(now.substring(11, 13)+now.substring(14, 16));
            int nightEnd = MyConfiguration.getInstance().getNightEnd();
            int nightStart = MyConfiguration.getInstance().getNightStart();
            if(nightStart > nightEnd){
                nightEnd += 2400;
                if(nowTimeInt < nightStart){
                    nowTimeInt += 2400;
                }
            }
            if(nowTimeInt>=nightStart && nowTimeInt <=nightEnd){
                if(this.getResources().getConfiguration().uiMode == 0x11){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    BaseUtils.shortTipInCoast(this,"自动切换为夜间模式");
                }
            }else{
                if(this.getResources().getConfiguration().uiMode == 0x21){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    BaseUtils.shortTipInCoast(this,"自动切换为白天模式");
                }
            }
        }
    }

    /**
     * 初始化密码键盘
     */
    private void initKeyboard(){
        Random random = new Random();
         List<Integer> pool = new ArrayList<>();
         pool.add(0);pool.add(1);pool.add(2);pool.add(3);pool.add(4);pool.add(5);
         pool.add(6);pool.add(7);pool.add(8);pool.add(9);
         for(int i=0;i<=9;i++){
             int numIndex = random.nextInt(pool.size());
             buttons[i].setText(pool.get(numIndex)+"");
             pool.remove((Integer)pool.get(numIndex));
         }
    }

    private void bindViewAndSetListener(){
        password = findViewById(R.id.login_text_password);
        tip = findViewById(R.id.login_text_tip);
        buttons[0] = findViewById(R.id.login_btn_num0);
        buttons[1] = findViewById(R.id.login_btn_num1);
        buttons[2] = findViewById(R.id.login_btn_num2);
        buttons[3] = findViewById(R.id.login_btn_num3);
        buttons[4] = findViewById(R.id.login_btn_num4);
        buttons[5] = findViewById(R.id.login_btn_num5);
        buttons[6] = findViewById(R.id.login_btn_num6);
        buttons[7] = findViewById(R.id.login_btn_num7);
        buttons[8] = findViewById(R.id.login_btn_num8);
        buttons[9] = findViewById(R.id.login_btn_num9);
        buttons[10] = findViewById(R.id.login_btn_del);
        buttons[11] = findViewById(R.id.login_btn_login);
        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        buttons[10].setOnLongClickListener(v -> {
            password.setText("");
            return true;
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        BaseUtils.createOneShotByVibrator();
        switch (v.getId()){
            case R.id.login_btn_del:{
                String s = password.getText().toString();
                if(!"".equals(s)){
                    s = s.substring(0,s.length()-1);
                    password.setText(s);
                }
                break;
            }
            case R.id.login_btn_login:{
                String pinUserInput = password.getText().toString();
                if("".equals(pinUserInput)){
                    return;
                }
                //游戏跳转
                String gameTag = "123";
                if(gameTag.equals(pinUserInput)){
                    BaseUtils.gotoActivity(this, MineGameActivity.class);
                    this.finish();
                    return;
                }
                password.setText("");
                SimpleResult result = loginService.login(pinUserInput);
                if(result.getSuccess()){
                    BaseUtils.gotoActivity(this,MainActivity.class);
                    this.finish();
                }else{
                    tip.setText(result.getMsg());
                    //BaseUtils.shortTipInCoast(this,result.getMsg());
                }
                break;
            }
            default:{
                String s = password.getText().toString();
                password.setText(s+((Button) v).getText().toString());
                break;
            }
        }
    }


    /**
     * 由于第一次使用本程序需要跳转设置登录密码，所以此Activity有逻辑代码
     */
    private void doWhenFirstLogin(){
        SharedPreferences sharedPreferences = BaseUtils.getSharedPreference();
        boolean firstUse = sharedPreferences.getBoolean("firstUse", true);
        if(firstUse){
            //第一次使用本程序
            //创建数据库
            LitePal.getDatabase();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //写入帮助日记
                    try {
                        new DiaryServiceImpl().addHelpDiary();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //免责声明
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("免责声明")
                    .setMessage("本软件不会盗取你任何数据，有开源代码可查，开源网址https://github.com/HibaraAi/Diary或https://gitee.com/HibaraAi/Diary。因此，如果你在使用本软件的过程中，产生无论何种形式的损失，都与本作者无关。")
                    .setPositiveButton("了解并接受", (dialog, which) -> {
                        //申请存储权限
                        applyPermission();
                    })
                    .setNegativeButton("不接受并退出",((dialog, which) -> finish()))
                    .show();
        }
    }


    private void applyPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("存储权限申请");
        builder.setMessage("为了能更好的使用本软件，“消消乐”需要你许可外部存储的读写权限，" +
                "你之后可以在“帮助”中看到申请的权限用在何处。" +
                "\n\n你可以拒绝授权，但涉及存储的功能，你都用不了。（你可以在后续使用中重新授予权限）");
        builder.setPositiveButton("了解", (dialog, which) -> PermissionUtils.applyExternalStoragePermission(LoginActivity.this,1));
        builder.show();
    }

    /**
     * 权限授予情况
     * 安卓10
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    BaseUtils.shortTipInCoast(LoginActivity.this,"已获取外部存储的读写权限");
                }else{
                    BaseUtils.shortTipInCoast(LoginActivity.this,"你没有授权外部存储的读写权限");
                }
                //跳转设置登录密码界面
                BaseUtils.gotoActivity(this, SetPasswordActivity.class);
                break;
            }
            default:break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /**
             * Android11
             * 权限授予情况
             */
            case 1:{
                if (PermissionUtils.haveExternalStoragePermission(LoginActivity.this)) {
                    BaseUtils.longTipInCoast(LoginActivity.this,"已获取外部存储的读写权限");
                }else{
                    BaseUtils.alertDialogToShow(LoginActivity.this,"授权失败","你没有授予外部存储的读写权限，你将不能使用大部分功能");
                }
                //跳转设置登录密码界面
                BaseUtils.gotoActivity(this, SetPasswordActivity.class);
                break;
            }
            default:break;
        }
    }
}