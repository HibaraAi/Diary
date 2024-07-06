package cn.snowt.diary.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryDetailActivity;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVo;
import cn.snowt.mine.MineGameActivity;

/**
 * 特地为帮助&关于、及彩蛋加的RecyclerView.Adapter。DiaryVo内容匹配的View也变量，对应如下：
 *         newHolder.headImg.setImageResource(diaryVo.getId());
 *         newHolder.username.setText(diaryVo.getMyUuid());
 *         newHolder.modifyDate.setText(diaryVo.getModifiedDate());
 *         newHolder.weather.setText(diaryVo.getWeatherStr());
 *         newHolder.location.setText(diaryVo.getLocationStr());
 *         newHolder.label.setText(diaryVo.getLabelStr());
 *         newHolder.label.setVisibility(View.VISIBLE);
 *         newHolder.content.setText(diaryVo.getContent());
 *         newHolder.comment.setText(diaryVo.getQuoteDiaryStr());
 */
public class HelpAdapter extends RecyclerView.Adapter{
    private List<DiaryVo> diaryVoList;
    private Context context;

    public HelpAdapter(List<DiaryVo> diaryVoList) {
        this.diaryVoList = diaryVoList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(null==context){
            context = parent.getContext();
        }
        //创建View并绑定资源
        View view = LayoutInflater.from(context).inflate(R.layout.diary_item,parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.headImg = view.findViewById(R.id.item_head);
        viewHolder.username = view.findViewById(R.id.item_username);
        viewHolder.modifyDate = view.findViewById(R.id.item_modifyDate);
        viewHolder.weather = view.findViewById(R.id.item_weather);
        viewHolder.location = view.findViewById(R.id.item_location);
        viewHolder.label = view.findViewById(R.id.item_label);
        viewHolder.content = view.findViewById(R.id.item_content);
        viewHolder.comment = view.findViewById(R.id.item_btn_comment);
//        viewHolder.comment.setOnClickListener(v->{
//            BaseUtils.gotoActivity((Activity) context, MineGameActivity.class);
//        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        DiaryVo diaryVo = diaryVoList.get(position);
        newHolder.headImg.setImageResource(diaryVo.getId());
        newHolder.username.setText(diaryVo.getMyUuid());
        newHolder.modifyDate.setText(diaryVo.getModifiedDate());
        newHolder.weather.setText(diaryVo.getWeatherStr());
        newHolder.location.setText(diaryVo.getLocationStr());
        newHolder.label.setText(diaryVo.getLabelStr());
        newHolder.label.setVisibility(View.VISIBLE);
        newHolder.content.setText(diaryVo.getContent());
        if(null!=diaryVo.getQuoteDiaryStr() && !"".equals(diaryVo.getQuoteDiaryStr())){
            newHolder.comment.setText(diaryVo.getQuoteDiaryStr());
            newHolder.comment.setVisibility(View.VISIBLE);
            if("去玩扫雷".equals(diaryVo.getQuoteDiaryStr())){
                newHolder.comment.setOnClickListener(v-> BaseUtils.gotoActivity((Activity) context, MineGameActivity.class));
            }else if ("复制地址".equals(diaryVo.getQuoteDiaryStr())){
                newHolder.comment.setOnClickListener(v -> {
                    if (BaseUtils.copyInClipboard(context,"https://github.com/HibaraAi/Diary")) {
//                        BaseUtils.shortTipInCoast(context,"开源代码的网址已复制");
                        BaseUtils.shortTipInSnack(newHolder.headImg,"开源代码的网址已复制");
                    }
                });
                newHolder.comment.setOnLongClickListener(v -> {
                    if (BaseUtils.copyInClipboard(context,"https://gitee.com/HibaraAi/Diary")) {
//                        BaseUtils.shortTipInCoast(context,"镜像代码的网址已复制");
                        BaseUtils.shortTipInSnack(newHolder.headImg,"镜像代码的网址已复制");
                    }
                    return true;
                });
            }

        }else{
            newHolder.comment.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return diaryVoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View diaryView;
        ImageView headImg;
        TextView username;
        TextView modifyDate;
        TextView weather;
        TextView location;
        TextView label;
        TextView content;
        Button comment;

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
            diaryView = itemView;
        }
    }
}
