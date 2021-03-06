package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.activity.DiaryListActivity;
import cn.snowt.diary.activity.KeepDiaryActivity;
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
            //?????????????????????
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
        //???????????????????????????item????????????????????????????????????
        if(null== MyConfiguration.getInstance().getHeadImg()){
            viewHolder.headImg.setImageResource(R.drawable.nav_icon);
        }else{
            Glide.with(viewHolder.diaryView).load(MyConfiguration.getInstance().getHeadImg()).into(viewHolder.headImg);
        }
        viewHolder.username.setText(MyConfiguration.getInstance().getUsername());
        //????????????????????????
        viewHolder.comment.setOnClickListener(v->{
            if(viewHolder.visible){
                //???????????????
                viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.GONE);
                viewHolder.visible = false;
            }else{
                viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
                viewHolder.visible = true;
            }
        });
        //????????????????????????????????????????????????????????????
        if(parent.getId()==R.id.detail_recyclerview){
            viewHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
            viewHolder.visible = true;
        }
        //?????????????????????
        viewHolder.submitCommentBtn.setOnClickListener(v->{
            String commentInputStr = viewHolder.commentInput.getText().toString();
            if(!"".equals(commentInputStr)){
                SimpleResult result = commentService.addOneByArgs(commentInputStr, viewHolder.diaryId);
                if(result.getSuccess()){
                    BaseUtils.shortTipInSnack(viewHolder.diaryView,"?????????????????????????????? OvO");
                    viewHolder.commentInput.setText("");
                }else{
                    BaseUtils.longTipInSnack(viewHolder.diaryView,result.getMsg());
                }
            }
        });
        //??????????????????
        float fontSize = MyConfiguration.getInstance().getFontSize();
        if(fontSize!=-1){
            viewHolder.content.setTextSize(fontSize);
        }
        //??????????????????
        viewHolder.content.setOnLongClickListener(v->{
            AtomicReference<String> select = new AtomicReference<>();
            final String[] items = {"????????????","????????????","????????????","????????????","????????????","??????"};
            select.set(items[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("????????????");
            builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> {
                select.set(items[i]);
            });
            builder.setPositiveButton("??????", (dialog, which) -> {
                switch (select.get()) {
                    case "????????????":{
                        Intent intent = new Intent(context,KeepDiaryActivity.class);
                        intent.putExtra(KeepDiaryActivity.OPEN_FROM_TYPE,KeepDiaryActivity.OPEN_FROM_QUOTE_ADD);
                        intent.putExtra("uuid",viewHolder.myUuid);
                        intent.putExtra("str",viewHolder.content.getText());
                        context.startActivity(intent);
                        break;
                    }
                    case "????????????":{
                        BaseUtils.copyInClipboard(context,viewHolder.content.getText().toString());
                        BaseUtils.shortTipInSnack(viewHolder.content,"??????????????? OvO");
                        break;
                    }
                    case "??????":{
                        String s = viewHolder.content.getText().toString().replaceAll("\n","");
                        String tip = s.length()>20 ? s.substring(0,20)+"..." : s;
                        new AlertDialog.Builder(context)
                                .setTitle("???????????????????????????????")
                                .setMessage("\""+tip+"\"")
                                .setPositiveButton("????????????", (dialog1, which1) -> {
                                    SimpleResult result = diaryService.deleteById(viewHolder.diaryId);
                                    if(result.getSuccess()){
                                        BaseUtils.shortTipInSnack(viewHolder.diaryView,"??????????????????????????????????????? OvO");
                                    }else{
                                        BaseUtils.shortTipInSnack(viewHolder.diaryView,result.getMsg());
                                    }
                                })
                                .setNegativeButton("???????????????",null)
                                .show();
                        break;
                    }
                    case "????????????":{
                        Intent intent = new Intent(context, DiaryDetailActivity.class);
                        intent.putExtra("id",viewHolder.diaryId);
                        context.startActivity(intent);
                        break;
                    }
                    case "????????????":{
                        Intent intent = new Intent(context, KeepDiaryActivity.class);
                        intent.putExtra(KeepDiaryActivity.OPEN_FROM_TYPE,KeepDiaryActivity.OPEN_FROM_UPDATE_DIARY);
                        intent.putExtra("id",viewHolder.diaryId);
                        context.startActivity(intent);
                        break;
                    }
                    case "????????????":{
                        new AlertDialog.Builder(context)
                                .setTitle("??????")
                                .setMessage("????????????????????????(???????????????)\n???????????????????????????????????????????????????????????????????????????\n?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                .setPositiveButton("??????", (dialog1, which1) -> {
                                    int topDiaryId = BaseUtils.getSharedPreference().getInt("topDiary", -1);
                                    SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
                                    if(viewHolder.diaryId==topDiaryId){
                                        //??????????????????????????????
                                        edit.putInt("topDiary",-1);
                                    }else{
                                        //???????????????
                                        edit.putInt("topDiary",viewHolder.diaryId);
                                    }
                                    edit.apply();
                                    BaseUtils.shortTipInSnack(viewHolder.diaryView,"?????????????????????????????????????????????OvO");
                                })
                                .setNegativeButton("??????",null)
                                .show();
                        break;
                    }
                    default:break;
                }
            });
            builder.setNegativeButton("??????",null);
            builder.setCancelable(false);
            builder.show();
            return true;
        });
        //????????????
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
            builder.setTitle("?????????????????????");
            builder.setTitle("?????????????????????,?????????????????????????????????");
            builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> selectLabel.set(items[i]));
            builder.setPositiveButton("??????", (dialog, which) -> {
                Intent intent = new Intent(context, DiaryListActivity.class);
                intent.putExtra(DiaryListActivity.OPEN_FROM_TYPE,DiaryListActivity.OPEN_FROM_SEARCH_LABEL);
                intent.putExtra("label",selectLabel.get());
                context.startActivity(intent);
            });
            builder.setNegativeButton("??????",null);
            builder.setCancelable(false);
            builder.show();
        });
        viewHolder.label.setOnLongClickListener(v -> {
            String allLabel = viewHolder.label.getText().toString();
            BaseUtils.copyInClipboard(context,allLabel);
            BaseUtils.shortTipInCoast(context,"?????????: "+allLabel);
            return true;
        });
        //??????????????????
        viewHolder.quoteDiaryArea.setOnLongClickListener(v -> {
            Intent intent = new Intent(context,DiaryDetailActivity.class);
            intent.putExtra("uuid",viewHolder.quoteDiaryUuid);
            context.startActivity(intent);
            return true;
        });
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder)holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        newHolder.diaryId = diaryVo.getId();
        newHolder.quoteDiaryUuid = diaryVo.getQuoteDiaryUuid();
        newHolder.myUuid = diaryVo.getMyUuid();
//        if(null== MyConfiguration.getInstance().getHeadImg()){
//            newHolder.headImg.setImageResource(R.drawable.nav_icon);
//        }else{
//            Glide.with(newHolder.diaryView).load(MyConfiguration.getInstance().getHeadImg()).into(newHolder.headImg);
//        }
//        newHolder.username.setText(MyConfiguration.getInstance().getUsername());
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
        //??????????????????
        RecyclerView imgRecyclerView = newHolder.imageView;
        DiaryImageAdapter imgAdapter = new DiaryImageAdapter((ArrayList<String>) diaryVo.getPicSrcList());
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        imgRecyclerView.setAdapter(imgAdapter);
        imgRecyclerView.setLayoutManager(layoutManager);
        //??????????????????
        RecyclerView videoRV = newHolder.videoView;
        DiaryVideoAdapter videoAdapter = new DiaryVideoAdapter(diaryVo.getVideoSrcList());
        GridLayoutManager videoLayoutManager = new GridLayoutManager(context, 2);
        videoRV.setAdapter(videoAdapter);
        videoRV.setLayoutManager(videoLayoutManager);
        //???????????????
        RecyclerView commentRecyclerView = newHolder.commentView;
        DiaryCommentAdapter commentAdapter = new DiaryCommentAdapter(diaryVo.getCommentList());
        GridLayoutManager layoutManager2 = new GridLayoutManager(context, 1);
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(layoutManager2);
        if(!diaryVo.getCommentList().isEmpty()){
            newHolder.comment.setText("??????("+diaryVo.getCommentList().size()+")");
            //????????????
            if(MyConfiguration.getInstance().isAutoOpenComment()){
                newHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.VISIBLE);
                newHolder.visible = true;
            }
        }else{
            newHolder.comment.setText("??????");
            //???????????????
            newHolder.diaryView.findViewById(R.id.item_comment_area_parent).setVisibility(View.GONE);
            newHolder.visible = false;
        }
        //??????????????????
        if(null!=newHolder.quoteDiaryUuid && !"".equals(newHolder.quoteDiaryUuid)){
            if("del".equals(newHolder.quoteDiaryUuid)){
                newHolder.quoteDiaryStr.setText("[??????:???????????????????????????]");
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
