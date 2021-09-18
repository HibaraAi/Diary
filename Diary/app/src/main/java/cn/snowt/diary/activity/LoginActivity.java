package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.LoginService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.LoginServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
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
        setContentView(R.layout.activity_login);
        bindViewAndSetListener();
        doWhenFirstLogin();
        initKeyboard();
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
            //写入帮助日记
            new DiaryServiceImpl().addHelpDiary();
            //申请存储权限
            applyPermission();
        }
    }


    private void applyPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("存储权限申请");
        builder.setMessage("为了能更好的使用本软件，“消消乐”需要你许可外部存储的读写权限，本软件不会偷盗你的数据，放心使用。" +
                "\n读权限用在了读取相册中的图片、读取本软件生成的备份文件。" +
                "\n写权限用在了日记配图、头像、背景图、加密密钥和备份文件的存储。" +
                "\n你可以在后续使用过程中再来许可权限，但不敢保证会不会出现闪退现象，因此本软件强烈建议你在此时授予权限" +
                "\n你可在帮助和关于中找到权限使用的详细说明");
        builder.setPositiveButton("了解", (dialog, which) -> {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        });
        builder.show();
    }

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
}