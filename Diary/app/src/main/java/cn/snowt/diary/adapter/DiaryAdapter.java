package cn.snowt.diary.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryListActivity;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.CommentServiceImpl;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

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
        RecyclerView commentView;
        Button comment;
        Button del;
        Button submitCommentBtn;
        EditText commentInput;
        Boolean visible;

        Integer diaryId;


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
            del = itemView.findViewById(R.id.item_btn_del);
            imageView = itemView.findViewById(R.id.item_pic_area);
            commentView = itemView.findViewById(R.id.item_comment_area);
            diaryView = itemView;
            submitCommentBtn = itemView.findViewById(R.id.item_comment_input_btn);
            commentInput = itemView.findViewById(R.id.item_comment_input);

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
        //读取输入的评论
        viewHolder.submitCommentBtn.setOnClickListener(v->{
            String commentInputStr = viewHolder.commentInput.getText().toString();
            if(!"".equals(commentInputStr)){
                SimpleResult result = commentService.addOneByArgs(commentInputStr, viewHolder.diaryId);
                if(result.getSuccess()){
                    BaseUtils.shortTipInSnack(viewHolder.diaryView,"评论成功，请手动刷新");
                    viewHolder.commentInput.setText("");
                }else{
                    BaseUtils.longTipInSnack(viewHolder.diaryView,result.getMsg());
                }
            }
        });
        viewHolder.del.setOnClickListener(v->{
            BaseUtils.shortTipInSnack(viewHolder.diaryView,"长按删除日记");
        });
        viewHolder.del.setOnLongClickListener(v->{
            new AlertDialog.Builder(context)
                    .setTitle("确定要删除这条日记吗?")
                    .setPositiveButton("确认删除", (dialog, which) -> {
                        SimpleResult result = diaryService.deleteById(viewHolder.diaryId);
                        if(result.getSuccess()){
                            BaseUtils.shortTipInSnack(viewHolder.diaryView,"删除成功，刷新后将正常展示");
                        }else{
                            BaseUtils.shortTipInSnack(viewHolder.diaryView,result.getMsg());
                        }
                    })
                    .setNegativeButton("刚刚点错了",null)
                    .show();
            return true;
        });
        viewHolder.content.setOnLongClickListener(v->{
            BaseUtils.copyInClipboard(context,viewHolder.content.getText().toString());
            BaseUtils.shortTipInSnack(viewHolder.content,"日记已复制");
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
            builder.setCancelable(false);
            builder.show();
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        newHolder.diaryId = diaryVo.getId();
        if(null== MyConfiguration.getInstance().getHeadImg()){
            newHolder.headImg.setImageResource(R.drawable.nav_icon);
        }else{
            Glide.with(newHolder.diaryView).load(MyConfiguration.getInstance().getHeadImg()).into(newHolder.headImg);
        }
        newHolder.username.setText(MyConfiguration.getInstance().getUsername());
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
        //处理图片展示
        RecyclerView imgRecyclerView = newHolder.imageView;
        DiaryImageAdapter imgAdapter = new DiaryImageAdapter(diaryVo.getPicSrcList());
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        imgRecyclerView.setAdapter(imgAdapter);
        imgRecyclerView.setLayoutManager(layoutManager);
        //处理评论区
        RecyclerView commentRecyclerView = newHolder.commentView;
        DiaryCommentAdapter commentAdapter = new DiaryCommentAdapter(diaryVo.getCommentList());
        GridLayoutManager layoutManager2 = new GridLayoutManager(context, 1);
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(layoutManager2);
        holder = newHolder;
    }

    @Override
    public int getItemCount() {
        return diaryVoList.size();
    }

}
