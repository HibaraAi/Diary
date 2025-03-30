package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.blog.BlogDetailActivity;
import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.DiaryListActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.CommentServiceImpl;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.PDFUtils;
import cn.snowt.diary.util.PermissionUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.note.ItemAdapter;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-16 13:45
 * @Description:
 */
public class DiaryAdapter extends RecyclerView.Adapter{

    private List<DiaryVo> diaryVoList;
    private Context context;
    private static CommentService commentService = new CommentServiceImpl();
    private static final DiaryService diaryService = new DiaryServiceImpl();


    static class ViewHolder extends RecyclerView.ViewHolder{
        View diaryView;
        ImageView headImg;
        TextView username;
        TextView modifyDate;
        TextView weather;
        TextView location;
        TextView label;
        TextView content;
        RecyclerView imageView;
        RecyclerView videoView;
        RecyclerView commentView;
        Button comment;
        Button submitCommentBtn;
        EditText commentInput;
        Boolean visible;
        TextView quoteDiaryStr;
        CardView quoteDiaryArea;

        Integer diaryId;
        String quoteDiaryUuid;
        String myUuid;

        boolean isBlog;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headImg = itemView.findViewById(R.id.item_head);
            username = itemView.findViewById(R.id.item_username);
            modifyDate = itemView.findViewById(R.id.item_modifyDate);
            weather = itemView.findViewById(R.id.item_weather);
            location = itemView.findViewById(R.id.item_location);
            label = itemView.findViewById(R.id.item_label);
            content = itemView.findViewById(R.id.item_content);
            comment = itemView.findViewById(R.id.item_btn_comment);
            imageView = itemView.findViewById(R.id.item_pic_area);
            videoView = itemView.findViewById(R.id.item_video_area);
            commentView = itemView.findViewById(R.id.item_comment_area);
            diaryView = itemView;
            submitCommentBtn = itemView.findViewById(R.id.item_comment_input_btn);
            commentInput = itemView.findViewById(R.id.item_comment_input);
            quoteDiaryArea = itemView.findViewById(R.id.item_quote_diary_area);
            quoteDiaryStr = itemView.findViewById(R.id.item_quote_diary_content);
            //评论区可见标记
            visible = false;
        }
    }

    public DiaryAdapter(List<DiaryVo> diaryVoList) {
        this.diaryVoList = diaryVoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        final ViewHolder viewHolder = new ViewHolder(view);
        //头像及用户名是每个item都一样，且不会有点击事件
        if(null== MyConfiguration.getInstance().getHeadImg()){
            viewHolder.headImg.setImageResource(R.drawable.nav_icon);
        }else{
            Glide.with(viewHolder.diaryView).load(MyConfiguration.getInstance().getHeadImg()).into(viewHolder.headImg);
        }
        viewHolder.username.setText(MyConfiguration.getInstance().getUsername());
        //展开或收起评论区
        viewHolder.comment.setOnClickListener(v->{
            if(viewHolder.visible){
                //改为不可见
                viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.GONE);
                viewHolder.visible = false;
            }else{
                viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
                viewHolder.visible = true;
            }
        });
        //特殊地，如果是日记详情页，自动打开评论区
        if(parent.getId()==R.id.detail_recyclerview){
            viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
            viewHolder.visible = true;
        }
        //读取输入的评论
        viewHolder.submitCommentBtn.setOnClickListener(v->{
            String commentInputStr = viewHolder.commentInput.getText().toString();
            if(!"".equals(commentInputStr)){
                SimpleResult result = commentService.addOneByArgs(commentInputStr, viewHolder.diaryId);
                if(result.getSuccess()){
                    BaseUtils.shortTipInCoast(context,"评论成功，请手动刷新 OvO");
//                    BaseUtils.shortTipInSnack(viewHolder.diaryView,"评论成功，请手动刷新 OvO");
                    viewHolder.commentInput.setText("");
                }else{
                    BaseUtils.longTipInSnack(viewHolder.diaryView,result.getMsg());
                }
            }
        });
        //设置字体大小
        float fontSize = MyConfiguration.getInstance().getFontSize();
        if(fontSize!=-1){
            viewHolder.content.setTextSize(fontSize);
        }
        //长按日记文字
        viewHolder.content.setOnLongClickListener(v->{
            AtomicReference<String> select = new AtomicReference<>();
            final String[] items = {"复制日记","置顶日记","引用追更","查看详情","编辑日记","修改标签","存为图片","删除"};
            select.set(items[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("日记菜单");
            builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> {
                select.set(items[i]);
            });
            builder.setPositiveButton("确定", (dialog, which) -> {
                switch (select.get()) {
                    case "修改标签":{
                        Diary diary = LitePal.find(Diary.class, viewHolder.diaryId);
                        if(null!=diary){
                            androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(context);
                            builder2.setTitle("修改标签");
                            EditText editText = new EditText(context);
                            editText.setLines(2);
                            editText.setGravity(Gravity.TOP);
                            editText.setBackgroundResource(R.drawable.edge);
                            editText.setHint("输入......");
                            editText.setText(diary.getLabel());
                            editText.setPadding(5,5,5,5);
                            builder2.setView(editText);
                            builder2.setPositiveButton("修改", (dialog1, which1) -> {
                                String labelStr = editText.getText().toString();
                                String finalLabelStr = new String();
                                boolean needUpdate = true;
                                if(labelStr.length()<=30 && labelStr.contains("。")){  //新的标签解析，用。解析
                                    String[] split = labelStr.split("。");
                                    if(split.length>0){
                                        StringBuilder builder33 = new StringBuilder();
                                        for (String s : split) {
                                            builder33.append("#").append(s).append("#");
                                        }
                                        finalLabelStr = builder33.toString();
                                    }else{
                                        BaseUtils.shortTipInSnack(parent,"标签的总字符数不超过30,且格式必须正确");
                                        needUpdate = false;
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
                                        finalLabelStr = labelStr;
                                    }else{
                                        BaseUtils.shortTipInSnack(parent,"标签的总字符数不超过30,且格式必须正确");
                                        needUpdate = false;
                                    }
                                }
                                if(needUpdate){
                                    diary.setLabel(finalLabelStr);
                                    SimpleResult result = diaryService.updateDiaryLabelById(diary);
                                    if(result.getSuccess()){
                                        BaseUtils.shortTipInSnack(parent,"修改标签成功，请手动刷新。");
                                    }else{
                                        BaseUtils.shortTipInSnack(parent,result.getMsg());
                                    }
                                }
                            });
                            builder2.setNegativeButton("取消",null);
                            builder2.show();
                        }else{
                            BaseUtils.shortTipInSnack(parent,"貌似已被删除了。");
                        }
                        break;
                    }
                    case "存为图片":{
                        //检查权限
                        if (PermissionUtils.haveExternalStoragePermission(context)) {
                            //PDFUtils.saveViewAsPdf(view);
                            saveViewAsImage(view);
                        }else{
                            BaseUtils.alertDialogToShow(context,"提示","在你授予外部存储的读写权限前，你不能导出PDF。\n请在主界面长按背景图进行授权。");
                        }
                        break;
                    }
                    case "引用追更":{
                        Intent intent = new Intent(context,KeepDiaryActivity.class);
                        intent.putExtra(KeepDiaryActivity.OPEN_FROM_TYPE,KeepDiaryActivity.OPEN_FROM_QUOTE_ADD);
                        intent.putExtra("uuid",viewHolder.myUuid);
                        intent.putExtra("str",viewHolder.content.getText());
                        context.startActivity(intent);
                        break;
                    }
                    case "复制日记":{
                        BaseUtils.copyInClipboard(context,viewHolder.content.getText().toString());
                        BaseUtils.shortTipInSnack(viewHolder.content,"日记已复制 OvO");
                        break;
                    }
                    case "删除":{
                        String s = viewHolder.content.getText().toString().replaceAll("\n","");
                        String tip = s.length()>20 ? s.substring(0,20)+"..." : s;
                        new AlertDialog.Builder(context)
                                .setTitle("确定要删除这条日记吗?")
                                .setMessage("\""+tip+"\"")
                                .setPositiveButton("确认删除", (dialog1, which1) -> {
                                    SimpleResult result = diaryService.deleteById(viewHolder.diaryId);
                                    if(result.getSuccess()){
                                        BaseUtils.shortTipInSnack(viewHolder.diaryView,"删除成功，刷新后将正常展示 OvO");
                                    }else{
                                        BaseUtils.shortTipInSnack(viewHolder.diaryView,result.getMsg());
                                    }
                                })
                                .setNegativeButton("刚刚点错了",null)
                                .show();
                        break;
                    }
                    case "查看详情":{
                        Intent intent = new Intent(context, DiaryDetailActivity.class);
                        intent.putExtra("id",viewHolder.diaryId);
                        context.startActivity(intent);
                        break;
                    }
                    case "编辑日记":{
                        Intent intent = new Intent(context, KeepDiaryActivity.class);
                        intent.putExtra(KeepDiaryActivity.OPEN_FROM_TYPE,KeepDiaryActivity.OPEN_FROM_UPDATE_DIARY);
                        intent.putExtra("id",viewHolder.diaryId);
                        context.startActivity(intent);
                        break;
                    }
                    case "置顶日记":{
                        new AlertDialog.Builder(context)
                                .setTitle("说明")
                                .setMessage("将此条日记置顶。(已有则覆盖)\n特殊地，如果对置顶日记本身执行此操作则视为取消置顶\n另外，置顶只是提前展示，你仍会在浏览时看到该日记真身。如果删除置顶日记，则视为真正删除该日记。")
                                .setPositiveButton("继续", (dialog1, which1) -> {
                                    int topDiaryId = BaseUtils.getSharedPreference().getInt("topDiary", -1);
                                    SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                                    if(viewHolder.diaryId==topDiaryId){
                                        //就是这一条，取消置顶
                                        edit.putInt("topDiary",-1);
                                    }else{
                                        //设置为置顶
                                        edit.putInt("topDiary",viewHolder.diaryId);
                                    }
                                    edit.apply();
                                    BaseUtils.shortTipInSnack(viewHolder.diaryView,"已更新置顶日记，刷新后即生效。OvO");
                                })
                                .setNegativeButton("取消",null)
                                .show();
                        break;
                    }
                    default:break;
                }
            });
            builder.setNegativeButton("取消",null);
            builder.setCancelable(false);
            builder.show();
            return true;
        });
        //点击标签
        viewHolder.label.setOnClickListener(v->{
            String allLabel = viewHolder.label.getText().toString();
            AtomicReference<String> selectLabel = new AtomicReference<>();
            final String[] items = allLabel.split("##");
            if(items.length>1){
                items[0] = items[0]+"#";
                items[items.length-1] = "#"+items[items.length-1];
            }
            if(items.length>=3){
                for(int i=1;i<=items.length-2;i++){
                    items[i] = "#"+items[i]+"#";
                }
            }
            selectLabel.set(items[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("按标签查看日记");
            builder.setTitle("请选择一个标签,将展示同标签的所有日记");
            builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> selectLabel.set(items[i]));
            builder.setPositiveButton("查看", (dialog, which) -> {
                Intent intent = new Intent(context, DiaryListActivity.class);
                intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_SEARCH_LABEL);
                intent.putExtra("label",selectLabel.get());
                context.startActivity(intent);
            });
            builder.setNegativeButton("取消",null);
            builder.setCancelable(true);
            builder.show();
        });
        viewHolder.label.setOnLongClickListener(v -> {
            String allLabel = viewHolder.label.getText().toString();
            BaseUtils.copyInClipboard(context,allLabel);
//            BaseUtils.shortTipInCoast(context,"已复制: "+allLabel);
            BaseUtils.shortTipInSnack(view,"已复制: "+allLabel);
            return true;
        });
        //长按引用日记
        viewHolder.quoteDiaryArea.setOnLongClickListener(v -> {
            if(viewHolder.isBlog){
                Intent intent = new Intent(context, BlogDetailActivity.class);
                intent.putExtra(BlogDetailActivity.INTENT_BLOG_ID,viewHolder.diaryId);
                context.startActivity(intent);
            }else{
                Intent intent = new Intent(context,DiaryDetailActivity.class);
                intent.putExtra("uuid",viewHolder.quoteDiaryUuid);
                context.startActivity(intent);
            }
            return true;
        });
        //短按引用日记的提示
        viewHolder.quoteDiaryArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseUtils.shortTipInSnack(viewHolder.quoteDiaryArea,"长按跳转到引用日记");
            }
        });
        return viewHolder;
    }

    /**
     * 将View保存为图片
     * @param view
     */
    private void saveViewAsImage(View view) {
        RecyclerView parent = (RecyclerView) view.getParent();
//        view.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
////        view.setDrawingCacheEnabled(true);
////        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        FileOutputStream fos;
        String imagePath = "";
        try {
            // 判断手机设备是否有SD卡
            boolean isHasSDCard = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                // SD卡根目录
                //File sdRoot = Environment.getExternalStorageDirectory();
                String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/消消乐/";
                File sdRoot = new File(absolutePath);
                if(!sdRoot.exists()){
                    sdRoot.mkdirs();
                }
                File file = new File(sdRoot, Calendar.getInstance().getTimeInMillis()+".png");
                fos = new FileOutputStream(file);
                imagePath = file.getAbsolutePath();
            } else
                throw new Exception("创建文件失败!");

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);

            fos.flush();
            fos.close();
            BaseUtils.alertDialogToShow(context,"提示","保存成功，存储地址为：相机目录\\消消乐");
        } catch (Exception e) {
            e.printStackTrace();
            BaseUtils.shortTipInCoast(context,"保存失败");
        }
//        view.destroyDrawingCache();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        newHolder.isBlog = DiaryVo.BLOG_FLAG.equals(diaryVo.getQuoteDiaryUuid());  //已约定QuoteDiaryUuid作为是否是Blog的标识
        newHolder.diaryId = diaryVo.getId();
        newHolder.quoteDiaryUuid = diaryVo.getQuoteDiaryUuid();
        newHolder.myUuid = diaryVo.getMyUuid();
        newHolder.modifyDate.setText(diaryVo.getModifiedDate());
        newHolder.weather.setText(diaryVo.getWeatherStr());
        newHolder.location.setText(diaryVo.getLocationStr());
        if("".equals(diaryVo.getLabelStr())){
            newHolder.label.setVisibility(View.GONE);
        }else{
            newHolder.label.setText(diaryVo.getLabelStr());
            newHolder.label.setVisibility(View.VISIBLE);
        }
        newHolder.content.setText(diaryVo.getContent());
        if(!newHolder.isBlog){  //非Blog（也就是日记）才需要处理图片、视频、评论区的展示
            //处理图片展示
            RecyclerView imgRecyclerView = newHolder.imageView;
            DiaryImageAdapter imgAdapter = new DiaryImageAdapter((ArrayList<String>) diaryVo.getPicSrcList());
            GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
            imgRecyclerView.setAdapter(imgAdapter);
            imgRecyclerView.setLayoutManager(layoutManager);
            //处理视频展示
            RecyclerView videoRV = newHolder.videoView;
            DiaryVideoAdapter videoAdapter = new DiaryVideoAdapter(diaryVo.getVideoSrcList());
            GridLayoutManager videoLayoutManager = new GridLayoutManager(context, 2);
            videoRV.setAdapter(videoAdapter);
            videoRV.setLayoutManager(videoLayoutManager);
            //处理评论区
            RecyclerView commentRecyclerView = newHolder.commentView;
            DiaryCommentAdapter commentAdapter = new DiaryCommentAdapter(diaryVo.getCommentList());
            GridLayoutManager layoutManager2 = new GridLayoutManager(context, 1);
            commentRecyclerView.setAdapter(commentAdapter);
            commentRecyclerView.setLayoutManager(layoutManager2);
            androidx.cardview.widget.CardView commentAreaParent=newHolder.diaryView.findViewById(R.id.item_comment_area_parent);
            if (context.getResources().getConfiguration().uiMode == 0x11) {
                commentAreaParent.setCardBackgroundColor(Color.parseColor("#EFEAEB"));
            }else{
                commentAreaParent.setCardBackgroundColor(Color.parseColor("#525050"));
            }
            if(!diaryVo.getCommentList().isEmpty()){
                newHolder.comment.setText("评论("+diaryVo.getCommentList().size()+")");
                //改为可见
                if(MyConfiguration.getInstance().isAutoOpenComment()){
                    commentAreaParent.setVisibility(View.VISIBLE);
                    newHolder.visible = true;
                }
            }else{
                newHolder.comment.setText("评论");
                //改为不可见
                commentAreaParent.setVisibility(View.GONE);
                newHolder.visible = false;
            }
        }
        //处理是Blog的情况
        if(null!=newHolder.quoteDiaryUuid && DiaryVo.BLOG_FLAG.equals(newHolder.quoteDiaryUuid)){
            //禁用部分View
            newHolder.comment.setVisibility(View.GONE);
            newHolder.commentView.setVisibility(View.GONE);
            newHolder.imageView.setVisibility(View.GONE);
            newHolder.content.setVisibility(View.GONE);
            //更换某些View作为提示
            newHolder.username.setText("Blog");
            newHolder.headImg.setImageResource(R.drawable.icon_blog);
            newHolder.location.setText("提示：长按“引用”即可跳转Blog详情");
            newHolder.quoteDiaryStr.setText(diaryVo.getContent());
            if (context.getResources().getConfiguration().uiMode == 0x11) {
                newHolder.quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#EFEAEB"));
            }else{
                newHolder.quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#525050"));
            }
            newHolder.quoteDiaryArea.setVisibility(View.VISIBLE);
        }else if(null!=newHolder.quoteDiaryUuid && !"".equals(newHolder.quoteDiaryUuid)){
            //处理引用日记
            if("del".equals(newHolder.quoteDiaryUuid)){
                newHolder.quoteDiaryStr.setText("[提示:引用的日记已被删除]");
            }else{
                newHolder.quoteDiaryStr.setText(diaryVo.getQuoteDiaryStr());
            }
            if (context.getResources().getConfiguration().uiMode == 0x11) {
                newHolder.quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#EFEAEB"));
            }else{
                newHolder.quoteDiaryArea.setCardBackgroundColor(Color.parseColor("#525050"));
            }
            newHolder.quoteDiaryArea.setVisibility(View.VISIBLE);
        }else{
            newHolder.quoteDiaryArea.setVisibility(View.GONE);
        }
        holder = newHolder;
    }

    @Override
    public int getItemCount() {
        return diaryVoList.size();
    }

}
