package cn.snowt.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.TitleBarStyle;

import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.blog.view.RichTextView;
import cn.snowt.blog.view.StringUtils;
import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.GlideEngine;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
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
public class BlogDetailActivity extends AppCompatActivity {
    public static final String INTENT_BLOG_ID = "blogId";

    private TextView titleView;
    private TextView timeView;
    private TextView labelView;
    private RichTextView richTextView;
    private Disposable mDisposable;
    private ProgressDialog loadingDialog2;

    private int blogId;
    private String richText;
    private Context context = BlogDetailActivity.this;

    private BlogService blogService = new BlogService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);
        bandingViewAndSetListener();
        Intent intent = getIntent();
        blogId = intent.getIntExtra(INTENT_BLOG_ID, -1);
        showBlog(blogId);
    }


    protected void bandingViewAndSetListener() {
        titleView = findViewById(R.id.act_blog_title);
        timeView = findViewById(R.id.act_blog_time);
        labelView = findViewById(R.id.act_blog_label);
        richTextView = findViewById(R.id.act_blog_rich_text);

        //设置背景色
        ConstraintLayout constraintLayout = (ConstraintLayout) labelView.getParent();
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
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showBlog(int blogId) {
        //BaseUtils.shortTipInCoast(context,"暂不支持显示,id为："+blogId);
        //一进来，马上显示读取动画
        loadingDialog2 = new ProgressDialog(this);
        loadingDialog2.setMessage("数据加载中...");
        loadingDialog2.setCanceledOnTouchOutside(false);
        loadingDialog2.show();
        SimpleResult result = blogService.getBlogVoById(blogId);
        if(result.getSuccess()){
            BlogVo vo = (BlogVo) result.getData();
            titleView.setText(vo.getTitle());
            richText = vo.getContent();
            timeView.setText(vo.getDate());
            labelView.setText(vo.getLabelStr());
            richTextView.post(new Runnable() {
                @Override
                public void run() {
                    dealWithContent();
                }
            });
        }else{
            //读取失败，取消读取动画并显示失败原因
            loadingDialog2.dismiss();
            BaseUtils.shortTipInSnack(timeView,result.getMsg());
        }
    }

    private void dealWithContent(){
        loadingDialog2.dismiss();
        richTextView.clearAllLayout();
        showDataSync(richText);

        // 图片点击事件
        richTextView.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
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
                        if (loadingDialog2 != null){
                            loadingDialog2.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog2 != null){
                            loadingDialog2.dismiss();
                        }
                        BaseUtils.shortTipInSnack(timeView,"解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(String text) {
                        try {
                            if (richTextView !=null) {
                                if (text.contains("<img") && text.contains("src=")) {
                                    //imagePath可能是本地路径，也可能是网络地址
                                    String imagePath = StringUtils.getImgSrc(text);
                                    richTextView.addImageViewAtIndex(richTextView.getLastIndex(), imagePath);
                                } else {
                                    richTextView.addTextViewAtIndex(richTextView.getLastIndex(), text);
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
    private void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
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
                finish();
                break;
            }
            case R.id.toolbar_del:{
                SimpleResult result = blogService.deleteById(blogId);
                if(result.getSuccess()){
                    finish();
                }else{
                    BaseUtils.alertDialogToShow(context,"提示失败原因",result.getMsg());
                }
                break;
            }
            //编辑
            case R.id.toolbar_edit:{
                Intent intent = new Intent(context, EditBlogActivity.class);
                intent.putExtra(EditBlogActivity.INTENT_BLOG_ID,blogId);
                context.startActivity(intent);
                finish();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tb_del_edit,menu);
        return true;
    }

//    /**
//     * 打开大图
//     * @param position 图所在的位置
//     * @param imageSrcList 资源总列表
//     */
//    private void openMediaPreView(int position,List<String> imageSrcList){
//        ArrayList<LocalMedia> mediaList = new ArrayList<>(imageSrcList.size());
//        imageSrcList.forEach(s -> {
//            LocalMedia localMedia = LocalMedia.generateLocalMedia(LitePalApplication.getContext(), s);
//            localMedia.setMimeType("image/*");
//            mediaList.add(localMedia);
//        });
//        PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
//        if(context.getResources().getConfiguration().uiMode == 0x11){
//            TypedValue typedValue = new TypedValue();
//            context.getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
//            TitleBarStyle titleBarStyle = new TitleBarStyle();
//            titleBarStyle.setTitleBackgroundColor(typedValue.data);
//            pictureSelectorStyle.setTitleBarStyle(titleBarStyle);
//        }
//        PictureSelector.create(context)
//                .openPreview()
//                .setImageEngine(GlideEngine.createGlideEngine())
//                .setSelectorUIStyle(pictureSelectorStyle)
//                .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
//                    @Override
//                    public void onPreviewDelete(int position) {
//
//                    }
//                    @Override
//                    public boolean onLongPressDownload(Context context, LocalMedia media) {
//                        return false;
//                    }
//                }).startActivityPreview(position, false, mediaList);
//    }
}