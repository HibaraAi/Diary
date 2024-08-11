package cn.snowt.diary.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.litepal.LitePalApplication;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.snowt.diary.R;
import cn.snowt.diary.entity.Weather;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.util.FileUtils;
import cn.snowt.diary.util.MD5Utils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 11:46
 * @Description: 设置
 */
public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        ActionBar actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("设置");
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

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

    public static class SettingsFragment extends PreferenceFragmentCompat {
        SharedPreferences sharedPreferences = BaseUtils.getDefaultSharedPreferences();
        boolean testFun = sharedPreferences.getBoolean("testFun", false);
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference clearCachePreference = findPreference("clearCache");
            //读取缓存大小并展示
            clearCachePreference.setSummary(
                    FileUtils.getStringForDirSize(
                            LitePalApplication.getContext()
                                    .getExternalCacheDir().getAbsolutePath()));
            //读取外存中的数据大小并展示
            Preference clearROMPreference = findPreference("clearROM");
            clearROMPreference.setSummary(FileUtils.getStringForDirSize(
                    Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION).getAbsolutePath()
            ));
            //测试区功能
            boolean openTestFun = BaseUtils.getSharedPreference().getBoolean("openTestFun", false);
//            findPreference("autoBackup").setEnabled(openTestFun);
            findPreference("removeTip").setEnabled(openTestFun);
            //读取当前字体大小
            float fontSize = MyConfiguration.getInstance().getFontSize();
            if(fontSize!=-1){
                findPreference("setDiarySize").setSummary("当前字体大小："+fontSize+"dp");
            }else{
                findPreference("setDiarySize").setSummary("当前为默认字体大小");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            String autoNightTime = "";
            try {
                int nightStart = MyConfiguration.getInstance().getNightStart();
                int nightEnd = MyConfiguration.getInstance().getNightEnd();
                Date parse;
                if(nightStart<1000){
                    parse = sdf.parse("0" + nightStart);
                }else{
                    parse = sdf.parse(String.valueOf(nightStart));
                }
                Date parse1;
                if(nightEnd<1000){
                    parse1 = sdf.parse("0"+nightEnd);
                }else{
                    parse1 = sdf.parse(String.valueOf(nightEnd));
                }
                autoNightTime = sdf2.format(parse)+"——"+sdf2.format(parse1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            findPreference("autoNightTime").setSummary(autoNightTime);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            Context context = preference.getContext();
            switch (preference.getKey()) {
                case "setPassword":{
                    BaseUtils.gotoActivity((Activity) context,SetPasswordActivity.class);
                    break;
                }
                case "clearCache":{
                    File externalCacheDir = LitePalApplication.getContext().getExternalCacheDir();
                    FileUtils.deleteFolder(externalCacheDir.getAbsolutePath());
                    //重新展示缓存大小
                    Preference clearCachePreference = findPreference("clearCache");
                    //读取缓存大小并展示
                    clearCachePreference.setSummary(
                            FileUtils.getStringForDirSize(
                                    LitePalApplication.getContext()
                                            .getExternalCacheDir().getAbsolutePath()));

                    break;
                }
                case "clearROM":{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("警告");
                    builder.setMessage("此操作仅供卸载前执行。清除外存数据会删除所有软件数据，包括但不限于日记图片、备份文件、密钥文件。\n请输入登录密码确认此操作");
                    EditText editText = new EditText(context);
                    editText.setBackgroundResource(R.drawable.edge);
                    editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD | InputType.TYPE_CLASS_NUMBER );
                    editText.setHint("输入登录密码");
                    editText.setMinLines(2);
                    editText.setMaxLines(2);
                    editText.setPadding(10,5,10,5);
                    builder.setView(editText);
                    builder.setPositiveButton("确认清除数据", (dialog, which) -> {
                        String s = editText.getText().toString();
                        boolean truePassword = BaseUtils.getSharedPreference().getString("loginPassword","").equals(MD5Utils.encrypt(Constant.PASSWORD_PREFIX+s));
                        if(truePassword){
                            String path = Environment.getExternalStoragePublicDirectory(Constant.EXTERNAL_STORAGE_LOCATION).getAbsolutePath();
                            FileUtils.deleteFolder(path);
                            //重新展示缓存大小
                            Preference clearROMPreference = findPreference("clearROM");
                            //读取缓存大小并展示
                            clearROMPreference.setSummary(
                                    FileUtils.getStringForDirSize(path));
                            BaseUtils.longTipInCoast(context,"成功清除外部存储的数据，请放心卸载本程序");
                        }else{
                            BaseUtils.shortTipInCoast(context,"密码校验失败");
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                case "changeEncodeKey":{
                    BaseUtils.gotoActivity((Activity) context,SetRSAActivity.class);
                    break;
                }
                case "useEncode":{
                    BaseUtils.alertDialogToShow(context,"提示","你已更改日记的存储安全策略，请立马重启软件。\n如果你还没有修改过加密密钥，则此项设置无效");
                    break;
                }
                case "backupDiary":{
                    if(!PermissionUtils.haveExternalStoragePermission(context)){
                        BaseUtils.alertDialogToShow(context,"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能使用备份功能，但你可以使用”导出TXT“功能。你可以去修改头像的地方进行授权外部存储的读写权限");
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("设置读取密钥");
                        builder.setMessage("为备份文件设置读取口令\n（提示：从备份文件读取日记时，如果存在被加密过的日记，还需要提供解密密钥(生成的长密钥)，如果你没有长密钥，则整个备份文件会读取失败。)\n" +
                                "\n备份以下数据：\n1.日记正文、评论、对应图片视频路径" +
                                "\n2.纪念日数据" +
                                "\n3.同名标签设置");
                        EditText pinView = new EditText(context);
                        pinView.setHint("设置一个读取口令");
                        pinView.setBackgroundResource(R.drawable.edge);
                        pinView.setMinLines(2);
                        pinView.setMaxLines(2);
                        pinView.setPadding(10,10,10,10);
                        builder.setView(pinView);
                        builder.setCancelable(false);
                        builder.setPositiveButton("备份", (dialog, which) -> {
                            String publicKeyInJson;
                            publicKeyInJson = MyConfiguration.getInstance().getPublicKey();
                            DiaryService diaryService = new DiaryServiceImpl();
                            Log.w(TAG,"------此处应该改为service后台进行操作");
                            String pinInput = pinView.getText().toString();
                            if("".equals(pinInput)){
                                BaseUtils.longTipInCoast(context,"口令为空?你在想什么呢?已停止备份");
                            }else{
                                SimpleResult result = diaryService.backupDiary(publicKeyInJson,pinInput);
                                BaseUtils.alertDialogToShow(context,"提示",result.getMsg());
                            }
                        });
                        builder.setNegativeButton("取消",null);
                        builder.show();
                    }
                    break;
                }
                case "recoveryDiary":{
                    if(!PermissionUtils.haveExternalStoragePermission(context)){
                        BaseUtils.alertDialogToShow(context,"提示","你并没有授予外部存储的读写权限,在你许可之前，你不能使用恢复功能。你可以去修改头像的地方进行授权外部存储的读写权限");
                    }else{
                        BaseUtils.gotoActivity((Activity) context,RecoveryDiaryActivity.class);
                    }
                    break;
                }
                case "testFun":{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("输入测试码");
                    builder.setMessage("测试功能仅共开发者测试使用,输入测试码开启");
                    builder.setCancelable(false);
                    EditText editText = new EditText(context);
                    editText.setBackgroundResource(R.drawable.edge);
                    editText.setMinLines(2);
                    editText.setMaxLines(2);
                    editText.setPadding(10,10,10,10);
                    builder.setView(editText);
                    builder.setPositiveButton("开启", (dialog, which) -> {
                        if(Constant.TEST_FUN_KEY.equals(editText.getText().toString())){
                            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                            edit.putBoolean("openTestFun",true);
                            edit.apply();
//                            findPreference("autoBackup").setEnabled(true);
                            findPreference("removeTip").setEnabled(true);
                        }else{
                            BaseUtils.shortTipInCoast(context,"测试码不正确 UoU");
                        }
                    });
                    builder.setNegativeButton("直接关闭测试功能",(dialog, which) -> {
//                        findPreference("autoBackup").setEnabled(false);
                        findPreference("removeTip").setEnabled(false);
                        SharedPreferences.Editor edit1 = BaseUtils.getDefaultSharedPreferences().edit();
//                        edit1.putBoolean("autoBackup",false);
                        edit1.putBoolean("removeTip",false);
                        edit1.apply();
                        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                        edit.putBoolean("openTestFun",false);
                        edit.apply();
                    });
                    builder.show();
                    break;
                }
                case "setDiarySize":{
                    BaseUtils.gotoActivity((Activity) context,SetDiarySizeActivity.class);
                    break;
                }
                case "txtOutput":{
                    DiaryService diaryService = new DiaryServiceImpl();
                    if(diaryService.getDiaryVoList(0,1).isEmpty()){
                        BaseUtils.alertDialogToShow(context,"提示","没有日记，你导出啥呢???");
                        break;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("验证你的身份");
                    builder.setMessage("将所有日记以纯文本、不加密的方式导出到一个txt文件中。\n提示：导出需要解密数据，此过程非常久，有卡住现象属正常，请耐心等待，完成后自有提示。\n\n" +
                            "导出以下数据：1.日记正文  2.日记时间、地点、天气情况  3.日记评论内容及评论时间");
                    EditText pinView = new EditText(context);
                    pinView.setBackgroundResource(R.drawable.edge);
                    pinView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pinView.setHint("输入登陆密码");
                    pinView.setMinLines(2);
                    pinView.setMaxLines(2);
                    pinView.setPadding(10,10,10,10);
                    builder.setView(pinView);
                    builder.setCancelable(false);
                    builder.setPositiveButton("验证密码并导出", (dialog, which) -> {
//                        DiaryService diaryService = new DiaryServiceImpl();
                        String pinInput = pinView.getText().toString();
                        if("".equals(pinInput)){
                            BaseUtils.longTipInCoast(context,"你不验证密码我就不导出。");
                        }else{
                            SimpleResult result = diaryService.outputForTxt(pinInput);
                            BaseUtils.alertDialogToShow(context,"提示",result.getMsg());
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                case "autoNightTime":{
                    final int[] startTimeInt = {-1};
                    final int[] endTimeInt = {-1};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("设置起止时间");
                    TextView timeOne = new TextView(context);
                    TextView timeTwo = new TextView(context);
                    timeOne.setOnClickListener(v->{
                        new TimePickerDialog(context,
                                (view, hourOfDay, minute) ->{
                                    startTimeInt[0] = hourOfDay*100+minute;
                                    timeOne.setText(hourOfDay+":"+minute);
                                },
                                Calendar.HOUR_OF_DAY,
                                Calendar.MINUTE,
                                true)
                                .show();
                    });
                    timeTwo.setOnClickListener(v->{
                        new TimePickerDialog(context,
                                (view, hourOfDay, minute) ->{
                                    endTimeInt[0] = hourOfDay*100+minute;
                                    timeTwo.setText(hourOfDay+":"+minute);
                                },
                                Calendar.HOUR_OF_DAY,
                                Calendar.MINUTE,
                                true)
                                .show();
                    });
                    timeOne.setBackgroundResource(R.drawable.edge);
                    timeOne.setPadding(10,5,5,5);
                    timeOne.setMinLines(2);
                    timeOne.setGravity(Gravity.CENTER_VERTICAL);
                    timeOne.setHint("开始时间");
                    timeTwo.setBackgroundResource(R.drawable.edge);
                    timeTwo.setPadding(10,5,5,5);
                    timeTwo.setMinLines(2);
                    timeTwo.setGravity(Gravity.CENTER_VERTICAL);
                    timeTwo.setHint("结束时间");
                    TextView blank = new TextView(context);
                    blank.setText(" ");
                    blank.setTextSize(2);
                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(timeOne);
                    linearLayout.addView(blank);
                    linearLayout.addView(timeTwo);
                    builder.setView(linearLayout);
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", (dialog1, which) -> {
                        if(startTimeInt[0]!=endTimeInt[0] && (startTimeInt[0]!=-1 && endTimeInt[0]!=-1)){
                            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                            edit.putInt("nightStart",startTimeInt[0]);
                            edit.putInt("nightEnd",endTimeInt[0]);
                            edit.apply();
                            BaseUtils.shortTipInCoast(context,"设置成功，以后启动将会生效");
                        }else{
                            BaseUtils.shortTipInCoast(context,"起止时间不能相同也不能为空");
                        }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                    break;
                }
                case "funnyInfo":{
                    BaseUtils.gotoActivity((Activity) context,FunnyInfoActivity.class);
                    break;
                }
                case "sameLabel":{
                    BaseUtils.gotoActivity((Activity) context,SetSameLabelActivity.class);
                    break;
                }
                case "firstLoginNotice":{
                    BaseUtils.alertDialogToShow(context,"提示","开启后，每日首次登录后会检查今天是否有往年今日的消息，是否有逢百天/整年的纪念日，如果有则会通知栏通知，没有则不打扰。\n\n如果你的设备收不到通知，请手动去系统设置开启消消乐的通知权限，根据自己的需要开启声音、震动或横幅通知。");
                    break;
                }
                case "fullSearch":{
                    BaseUtils.gotoActivity((Activity) context,FullSearchActivity.class);
                    break;
                }
                case "themes":{
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
                    dialog.setTitle("选择一个颜色");
                    String[] items = {"默认","黑色","蓝色","绿色","紫色","红色","黄色"};
                    final int[] themesId = new int[1];
                    dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (items[which]) {
                                case "蓝色":{
                                    themesId[0] = R.style.Theme_blue;break;
                                }
                                case "黑色":{
                                    themesId[0] = R.style.Theme_black;break;
                                }
                                case "绿色":{
                                    themesId[0] = R.style.Theme_green;break;
                                }
                                case "红色":{
                                    themesId[0] = R.style.Theme_red;break;
                                }
                                case "黄色":{
                                    themesId[0] = R.style.Theme_yellow;break;
                                }
                                case "紫色":{
                                    themesId[0] = R.style.Theme_purple;break;
                                }
                                default:{
                                    themesId[0] = R.style.Theme_消消乐;break;
                                }
                            }
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", (dialog12, which) -> {
                        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                        edit.putInt("theme", themesId[0]);
                        edit.apply();
                        BaseUtils.shortTipInCoast(context,"重启后生效");
                    });
                    dialog.setNegativeButton("取消", null);
                    dialog.show();
                    break;
                }
                case "ascRead":{
                    BaseUtils.gotoActivity((Activity) context,TimeAscActivity.class);
                    break;
                }
                case "thanks":{
                    BaseUtils.gotoActivity((Activity)context,ThanksActivity.class);
                    break;
                }
                case "autoBackup":{
                    if (PermissionUtils.haveExternalStoragePermission(context)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("自动备份的提示：");
                        builder.setMessage("每三天自动输出一个备份文件，文件名为“"+Constant.AUTO_BACKUP_FILE_NAME+"”，" +
                                "存储在“根目录/Hibara”下。\n如果已经有一个自动备份的文件，则会覆盖。\n登录后才会检查是否需要备份。\n\n" +
                                "现在请为自动备份文件设置读取口令");
                        EditText pinView = new EditText(context);
                        pinView.setHint("设置一个读取口令");
                        pinView.setBackgroundResource(R.drawable.edge);
                        pinView.setMinLines(2);
                        pinView.setMaxLines(2);
                        pinView.setPadding(10,10,10,10);
                        builder.setView(pinView);
                        builder.setCancelable(false);
                        builder.setPositiveButton("设置口令", (dialog, which) -> {
                            String pinStr = pinView.getText().toString();
                            pinStr = pinStr.trim();
                            if("".equals(pinStr)){
                                BaseUtils.longTipInCoast(context,"我真的是服了，口令能空的吗？此次设置失效");
                            }else{
                                SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                                edit.putString(Constant.SHARE_PREFERENCES_AUTO_BACKUP_PIN,pinStr);
                                edit.apply();
                            }
                        });
                        builder.show();
                    }else{
                        BaseUtils.alertDialogToShow(context,"自动备份的提示","你没有授予外部读写权限，此开关无效。");
                    }
                    break;
                }
                case "pay":{
                    BaseUtils.gotoActivity((Activity)context,PayActivity.class);
                    break;
                }
                case "loginType":{
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
                    dialog.setTitle("伪装方式");
                    String[] items = {"手动登录(默认)","自动登录","便签界面(新增一个“123”便签跳转登录)"};
                    final int[] selectId = new int[1];
                    dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 1:{
                                    selectId[0] = LoginActivity.LOGIN_TYPE_AUTO_LOGIN; break;
                                }
                                case 2:{
                                    selectId[0] = LoginActivity.LOGIN_TYPE_GOTO_NOTE;break;
                                }
                                default:{
                                    selectId[0] = LoginActivity.LOGIN_TYPE_DEFAULT;break;
                                }
                            }
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", (dialog12, which) -> {
                        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                        edit.putInt("loginType", selectId[0]);
                        edit.apply();
                        BaseUtils.shortTipInCoast(context,"重启后生效");
                    });
                    dialog.setNegativeButton("取消", null);
                    dialog.show();
                    break;
                }
                case "pdfOutput":{
                    final String tip = "一开始想设置一键导出所有日记到PDF中的，想了一下不太合理，" +
                            "我可能只想导出某个标签的，或者某个时间段的，况且一键导出没有预览。TIP：将时间范围选的很大，涵盖所有日记即可导出所有。\n\n" +
                            "说这么多只是想告诉你，真正的导出PDF在“临时信息流”（时间轴、标签集、搜索结果-->右上角跳转信息流），" +
                            "临时信息流预览的什么样导出就是什么样，且需要你先选好时间顺序还是倒叙。" +
                            "PDF中默认打开评论区（如果评论不为空的话），视频采用一个低画质缩略图展示，图片也是压缩过的，防止PDF文件过大。\n\n" +
                            "没有授权外部存储权限的话，不能导出PDF，但你可以导出TXT。";
                    BaseUtils.alertDialogToShow(context,"这里只是提示",tip);
                    break;
                }
                case "deleteList":{
                    Intent intent = new Intent(context, DiaryListActivity.class);
                    intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_DELETE_LIST);
                    context.startActivity(intent);
                    break;
                }
                case "updateInfo":{
                    Intent intent = new Intent(context, HelpActivity.class);
                    intent.putExtra(HelpActivity.OPEN_TYPE,HelpActivity.OPEN_TYPE_UPDATE);
                    context.startActivity(intent);
                    break;
                }
                default:return false;
            }
            return true;
        }
    }
}