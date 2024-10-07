package cn.snowt.diary.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.RSAUtils;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-03 20:22
 * @Description: 修改RSA加密密钥
 */
public class SetRSAActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView privateKeyView;
    private TextView publicKeyView;
    private TextView tipView;
    private Button createBtn;
    private Button testBtn;
    private Button saveBtn;

    private String publicKey;
    private String privateKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_rsaactivity);
        bindViewAndSetListener();
        checkModifiable();
    }

    private void checkModifiable() {
        String publicKey = BaseUtils.getSharedPreference().getString(Constant.SHARE_PREFERENCES_PUBLIC_KEY, "");
        if(null!=publicKey && !"".equals(publicKey)){
            //已经修改过了
            String path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"output/").getAbsolutePath();
            String tip = "你已经修改过加密密钥了，不准再次修改，也不准再次保存密钥。初次导出的密钥存储在："+path;
            privateKeyView.setText(tip);
            tipView.setText(tip);
            publicKeyView.setText(tip);
            createBtn.setEnabled(false);
        }
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("设置加密密钥");
        }
        privateKeyView = findViewById(R.id.rsa_private);
        publicKeyView = findViewById(R.id.rsa_public);
        tipView = findViewById(R.id.rsa_tip);
        createBtn = findViewById(R.id.rsa_create_key);
        testBtn = findViewById(R.id.rsa_test_key);
        saveBtn = findViewById(R.id.rsa_save_key);

        createBtn.setOnClickListener(this);
        testBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        privateKeyView.setMovementMethod(ScrollingMovementMethod.getInstance());
        publicKeyView.setMovementMethod(ScrollingMovementMethod.getInstance());
        testBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        tipView.setText("提示：请先点击“生成密钥”");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            default:break;
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rsa_create_key:{
                if(!PermissionUtils.haveExternalStoragePermission(SetRSAActivity.this)){
                    BaseUtils.alertDialogToShow(v.getContext(),"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能启用加密功能，因为加密密钥必须存储在外部存储中给你。你可以去主界面长按背景图进行授权外部存储的读写权限");
                }else{
                    List<String> randomKey = RSAUtils.getRandomKey();
                    publicKey = randomKey.get(0);
                    privateKey = randomKey.get(1);
                    publicKeyView.setText(publicKey);
                    privateKeyView.setText(privateKey);
                    testBtn.setEnabled(true);
                    saveBtn.setEnabled(false);
                    tipView.setText("已生成密钥，请测试");
                }
                break;
            }
            case R.id.rsa_test_key:{
                String testStr = "现在是2021-09-03 20:48。Hibara编写";
                String encode = RSAUtils.encode(testStr, publicKey);
                String decode = RSAUtils.decode(encode, privateKey);
                if(testStr.equals(decode)){
                    tipView.setText("密钥可用，可以保存");
                    saveBtn.setEnabled(true);
                }else{
                    tipView.setText("密钥测试失败，请重新生成");
                    saveBtn.setEnabled(false);
                    testBtn.setEnabled(false);
                    publicKey = null;
                    privateKey = null;
                }
                break;
            }
            case R.id.rsa_save_key:{
                boolean flag1 = FileUtils.saveAsFileWriter(privateKey, "长密钥.txt");
                boolean flag2 = FileUtils.saveAsFileWriter(publicKey, "短密钥.txt");
                if(flag1 && flag2){
                    SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                    edit.putString(Constant.SHARE_PREFERENCES_PRIVATE_KEY,privateKey);
                    edit.putString(Constant.SHARE_PREFERENCES_PUBLIC_KEY,publicKey);
                    edit.apply();
                    String path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION+"output/").getAbsolutePath();
                    AlertDialog.Builder builder  = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("提示");
                    builder.setMessage("密钥保存在【"+path+"】目录下，请妥善保管长密钥，恢复日记时将用到长密钥。今后都不再支持密钥生成和保存。\n(短密钥用于加密，长密钥用于解密。虽然短密钥对你来说无用，但还是给你一份，让你看看密钥长啥样)\n\n另外，请重启软件。");
                    builder.setPositiveButton("OK,我会妥善保管密钥", (dialog, which) -> finish());
                    builder.show();
                }else{
                    AlertDialog.Builder builder  = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("提示");
                    builder.setMessage("密钥保存失败，请重试");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
                break;
            }
            default:break;
        }
    }
}