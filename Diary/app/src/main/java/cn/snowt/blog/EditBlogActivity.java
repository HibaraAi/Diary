package cn.snowt.blog;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.TitleBarStyle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.snowt.blog.view.RichTextEditor;
import cn.snowt.blog.view.StringUtils;
import cn.snowt.diary.R;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.activity.PicturesActivity;
import cn.snowt.diary.activity.SelectLabelActivity;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.async.SaveBlogTask;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.GlideEngine;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.drawboard.DrawBoardActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:26.
 * @Description :
 */
public class EditBlogActivity extends AppCompatActivity {
    public static final String INTENT_BLOG_ID = "blogId";  //从Intent获取需要修改的BlogId

    private Integer needEditId;  //从Intent获取需要修改的BlogId
    private Context context;  //很多地方都会用到，先存起来

    private RichTextEditor richTextEditor;  //富文本编辑框
    private EditText titleView;  //标题
    private TextView timeView;  //时间
    private TextView labelView;  //标签
    private ProgressDialog loadingDialog2;  //读取数据的Loading弹窗
    private Disposable subsLoading;  //不知道干嘛用的，复制别人的

    private BlogService blogService;
    private Date createDate;  //创建Blog的时间
    private List<String> imgSrcList;  //选中的图片List
    private List<String> videoSrcList;  //选中的图片List
    private String richText;  //暂存富文本数据，辅助渲染

    boolean removeTip = BaseUtils.getDefaultSharedPreferences().getBoolean("removeTip", false);

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    Handler handler = new Handler(new Handler.Callback() {  //处理异步回调
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    SimpleResult result = (SimpleResult) msg.obj;
                    closeProgressAlertDialog();
                    if(result.getSuccess()){
                        BaseUtils.shortTipInCoast(context,"保存成功。");
                        finish();
                    }else{
                        BaseUtils.alertDialogToShow(context,"保存失败",result.getMsg());
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

    public ActivityResultLauncher<Intent> launcher;  //标签选择的Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        context = EditBlogActivity.this;
        setContentView(R.layout.activity_edit_blog);
        this.launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    String beSelectLabelStr = result.getData().getStringExtra("beSelectLabelStr");
                    if(null==beSelectLabelStr || beSelectLabelStr.isEmpty()){
                        BaseUtils.shortTipInSnack(labelView,"要么标签为空，要么读取失败");
                    }else{
                        labelView.setText(beSelectLabelStr.trim());
                    }
                }
            }
        });
        bindViewAndSetListener();
        imgSrcList = new ArrayList<>();
        videoSrcList = new ArrayList<>();
        blogService = new BlogService();
        Intent intent = getIntent();
        int intExtra = intent.getIntExtra(INTENT_BLOG_ID, -1);
        //需要编辑的ID不为-1，则表示此次Activity为修改Blog
        if(-1!=intExtra){
            needEditId = intExtra;
            //尝试展示这个id的Blog
            showBlogInDB();
        }else{
            //不是修改Blog，showBlogInDB()中方法不会被执行
            // 所以图片删除、点击事件要单独拎出来执行
            // 图片删除事件
            richTextEditor.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {
                @Override
                public void onRtImageDelete(String imagePath) {
                    if (!TextUtils.isEmpty(imagePath)) {
                        showSnackbar("删除成功：" + imagePath);
                        boolean remove = imgSrcList.remove(imagePath);
                        if(!remove){
                            videoSrcList.remove(imagePath);
                        }
                    }
                }
            });
            // 图片点击事件
            richTextEditor.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
                @Override
                public void onRtImageClick(View view, String imagePath) {
                    try {
                        ArrayList<String> imageList = StringUtils.getTextFromHtml(richText, true);
                        int currentPosition = imageList.indexOf(imagePath);
                        BaseUtils.openHibaraMediaPreview(context,currentPosition,imageList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void bindViewAndSetListener() {
        //绑定相关控件，包括标题栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        //设置背景色
        ConstraintLayout constraintLayout = (ConstraintLayout) toolbar.getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            constraintLayout.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
            constraintLayout.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        // 初始化设置Toolbar
        toolbar.setTitle("Blog");
        setSupportActionBar(toolbar);
        //标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        richTextEditor = findViewById(R.id.act_blog_rich_edit);
        titleView = findViewById(R.id.act_blog_title);
        timeView = findViewById(R.id.act_blog_time);
        labelView = findViewById(R.id.act_blog_label);
        createDate = new Date();
        timeView.setText(BaseUtils.dateToString(createDate));
        timeView.setOnClickListener(v -> changeSaveTime());
        labelView.setOnClickListener(v -> {
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
            dialog.setTitle("输入1个或多个标签");
            if(!removeTip){
                dialog.setMessage("标签总长度不能超过30个字符。\n点击弹窗外取消修改；长按输入框选择已有标签。\n" +
                        "标签支持以下两种输入方式：\n#美食##周末#\n美食。周末");
            }
            EditText editText = new EditText(context);
            editText.setBackgroundResource(R.drawable.edge);
            editText.setMinLines(4);
            editText.setMaxLines(4);
            editText.setGravity(Gravity.START);
            editText.setHint("两种输入方式最后都展示为#美食##周末#");
            editText.setPadding(30,10,30,10);
            dialog.setView(editText);
            dialog.setCancelable(true);
            dialog.setPositiveButton("添加标签", (dialog1, which) -> {
                String labelStr = editText.getText().toString();
                labelStr = labelStr.trim();
                if(labelStr.length()<=30 && labelStr.contains("。")){  //新的标签解析，用。解析
                    String[] split = labelStr.split("。");
                    if(split.length>0){
                        StringBuilder builder = new StringBuilder();
                        for (String s : split) {
                            builder.append("#").append(s).append("#");
                        }
                        labelView.setText(builder.toString());
                    }else{
                        showSnackbar("标签的总字符数不超过30,且格式必须正确");
                    }
                } else{  //旧的标签解析，用#解析
                    int num = 0;
                    for (char c : labelStr.toCharArray()) {
                        if(c == '#'){
                            num++;
                        }
                    }
                    boolean flag = (num%2==0 && num!=0);
                    if(labelStr.length()<=30 && flag){
                        labelView.setText(labelStr);
                    }else{
                        showSnackbar("标签的总字符数不超过30,且格式必须正确");
                    }
                }
            });
            dialog.setNegativeButton("删除标签",(dialog1, which) -> {
                labelView.setText("");
            });
            AlertDialog alertDialog = dialog.show();
            editText.setOnLongClickListener(v1 -> {
                Intent intent = new Intent(context,SelectLabelActivity.class);
                launcher.launch(intent);
                alertDialog.cancel();
                return true;
            });
        });
        labelView.setOnLongClickListener(v -> {
            //长按选择已有标签
            Intent intent = new Intent(context, SelectLabelActivity.class);
            launcher.launch(intent);
            return false;
        });
    }

    /**
     * 弹出时间选择窗口，修改保存时间
     */
    private void changeSaveTime(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String dateStr = year + "-" + (month + 1) + "-" + dayOfMonth + " " + hourOfDay + ":" + minute + ":" + "00";
                    createDate = BaseUtils.stringToDate(dateStr);
                    timeView.setText(BaseUtils.dateToString(createDate));
                }
            }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(dialog -> timeView.setText(BaseUtils.dateToString(new Date())));
        datePickerDialog.show();
    }

    /**
     * 根据ID展示数据库中的Blog
     */
    private void showBlogInDB() {
        //马上展示读取的弹窗动画
        loadingDialog2 = new ProgressDialog(this);
        loadingDialog2.setMessage("数据加载中...");
        loadingDialog2.setCanceledOnTouchOutside(false);
        loadingDialog2.show();
        //尝试从数据库中读取这个Blog
        SimpleResult result = blogService.getBlogVoById(needEditId);
        if(result.getSuccess()){
            BlogVo vo = (BlogVo) result.getData();
            titleView.setText(vo.getTitle());
            richText = vo.getContent();
            richTextEditor.post(this::dealWithContent);
            timeView.setText(vo.getDate());
            labelView.setText(vo.getLabelStr());
            imgSrcList = vo.getImgSrc();
            videoSrcList = vo.getVideoSrc();
        }else{
            //读取失败，提示原因
            showSnackbar(result.getMsg());
        }
    }

    /**
     * 处理富文本展示
     */
    private void dealWithContent(){
        richTextEditor.clearAllLayout();
        showDataSync(richText);
        // 图片删除事件
        richTextEditor.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {
            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    showSnackbar("删除成功：" + imagePath);
                    boolean remove = imgSrcList.remove(imagePath);
                    if(!remove){
                        videoSrcList.remove(imagePath);
                    }
                }
            }
        });
        // 图片点击事件
        richTextEditor.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, String imagePath) {
                try {
                    ArrayList<String> imageList = StringUtils.getTextFromHtml(richText, true);
                    int currentPosition = imageList.indexOf(imagePath);
                    BaseUtils.openHibaraMediaPreview(context,currentPosition,imageList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 异步方式显示数据
     */
    private void showDataSync(final String html){
        Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) {
                        showEditData(emitter, html);
                    }
                })
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        //富文本读取展示完成，关闭弹窗动画
                        if (loadingDialog2 != null){
                            loadingDialog2.dismiss();
                        }
                        if (richTextEditor != null) {
                            //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                            richTextEditor.addEditTextAtIndex(richTextEditor.getLastIndex(), "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //富文本展示失败，关闭弹窗动画
                        if (loadingDialog2 != null){
                            loadingDialog2.dismiss();
                        }
                        showSnackbar("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsLoading = d;
                    }

                    @Override
                    public void onNext(String text) {
                        try {
                            if (richTextEditor != null) {
                                if (text.contains("<img") && text.contains("src=")) {
                                    //imagePath可能是本地路径，也可能是网络地址
                                    String imagePath = StringUtils.getImgSrc(text);
                                    //插入空的EditText，以便在图片前后插入文字
                                    richTextEditor.addEditTextAtIndex(richTextEditor.getLastIndex(), "");
                                    richTextEditor.addImageViewAtIndex(richTextEditor.getLastIndex(), imagePath);
                                } else {
                                    richTextEditor.addEditTextAtIndex(richTextEditor.getLastIndex(), text);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        }catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    /**
     * 将输入数据转换为DTO
     * @return BlogDto，封装失败则返回null，本方法会提示失败原因
     */
    private BlogDto inputToDto() {
        BlogDto dto = new BlogDto();
        String content = getEditData();
        if("".equals(content)){
            showSnackbar("正文不准为空！");
            return null;
        }
        dto.setContent(getEditData());
        String titleStr = titleView.getText().toString().trim();
        //没有标题，前五个字为标题
        if("".equals(titleStr)){
            titleStr = content.length()>5? content.substring(0,5):content;
        }
        dto.setTitle(titleStr);
        dto.setLabelStr(labelView.getText().toString());
        dto.setModifiedDate(createDate);
        dto.setImgSrcList(imgSrcList);
        dto.setVideoSrcList(videoSrcList);
        return dto;
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        StringBuilder content = new StringBuilder();
        try {
            List<RichTextEditor.EditData> editList = richTextEditor.buildEditData();
            for (RichTextEditor.EditData itemData : editList) {
                if (itemData.inputStr != null) {
                    content.append(itemData.inputStr);
                } else if (itemData.imagePath != null) {
                    content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Snackbar提示
     * @param tip Snackbar提示
     */
    private void showSnackbar(String tip){
        Snackbar snackbar = Snackbar.make(timeView, tip,1500);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    /**
     * 权限提示
     */
    private void permissionTip() {
        BaseUtils.alertDialogToShow(context,"提示","你没有授予外部存储的读写权限，因此不允许使用此功能。 你可以前往主界面的左滑菜单栏长按头像授予读写权限。");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tb_edit_blog,menu);
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
                askSave();
                break;
            }
            case R.id.toolbar_select_picture:{
                if(!PermissionUtils.haveExternalStoragePermission(context)){
                    permissionTip();
                }else{
                    PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
                    if(this.getResources().getConfiguration().uiMode == 0x11){
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
                        TitleBarStyle titleBarStyle = new TitleBarStyle();
                        titleBarStyle.setTitleBackgroundColor(typedValue.data);
                        pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
                    }
                    PictureSelector.create(this)
                            .openGallery(SelectMimeType.ofImage())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .isDisplayCamera(false)
                            .setSelectorUIStyle(pictureSelectorStyle)
                            .forResult(new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(ArrayList<LocalMedia> result) {
                                    result.forEach(localMedia -> {
                                        richTextEditor.insertImage(localMedia.getRealPath());
                                        imgSrcList.add(localMedia.getRealPath());
                                    });
                                    richText = getEditData();
                                }
                                @Override
                                public void onCancel() {

                                }
                            });
                }
                break;
            }
            //添加视频
            case R.id.toolbar_select_video:{
                if(!PermissionUtils.haveExternalStoragePermission(context)){
                    permissionTip();
                }else{
                    PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
                    if(this.getResources().getConfiguration().uiMode == 0x11){
                        TypedValue typedValue = new TypedValue();
                        getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
                        TitleBarStyle titleBarStyle = new TitleBarStyle();
                        titleBarStyle.setTitleBackgroundColor(typedValue.data);
                        pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
                    }
                    PictureSelector.create(this)
                            .openGallery(SelectMimeType.ofVideo())
                            .setImageEngine(GlideEngine.createGlideEngine())
                            .setSelectorUIStyle(pictureSelectorStyle)
                            .isDisplayCamera(false)
                            .forResult(new OnResultCallbackListener<LocalMedia>() {
                                @Override
                                public void onResult(ArrayList<LocalMedia> result) {
                                    result.forEach(localMedia -> {
                                        richTextEditor.insertImage(localMedia.getRealPath());
                                        videoSrcList.add(localMedia.getRealPath());
                                    });
                                    richText = getEditData();
                                }
                                @Override
                                public void onCancel() {

                                }
                            });
                }
                break;
            }
            //保存Blog
            case R.id.toolbar_save_blog:{
                BlogDto dto = inputToDto();
                if(null!=dto){
                    SaveBlogTask saveBlogTask = new SaveBlogTask(handler);
                    if(null!=needEditId && -1!=needEditId){
//                        //更新Blog
//                        SimpleResult result = blogService.updateById(needEditId,dto);
//                        if(result.getSuccess()){
//                            finish();
//                        }else{
//                            showSnackbar(result.getMsg());
//                        }
                        saveBlogTask.asyncSaveBlog(false,dto,needEditId);
                    }else{
                        //保存新的Blog
//                        SimpleResult result = blogService.addOne(dto);
//                        if(result.getSuccess()){
//                            finish();
//                        }else{
//                            BaseUtils.alertDialogToShow(context,"保存失败",result.getMsg());
//                        }
                        saveBlogTask.asyncSaveBlog(true,dto,needEditId);
                    }
                }
                break;
            }
            default:{
                Toast.makeText(context, "未定义的按钮", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        askSave();
    }

    /**
     * 保存提示
     */
    private void askSave() {
        if(getEditData().isEmpty()){
            finish();
        }else{
            androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setTitle("提示：");
            builder.setMessage("还没有保存的哦！继续退出吗？");
            builder.setNegativeButton("留下编辑", null);
            builder.setPositiveButton("不要保存并退出",(dialog, which) -> {
                finish();
            });
            builder.show();
        }
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