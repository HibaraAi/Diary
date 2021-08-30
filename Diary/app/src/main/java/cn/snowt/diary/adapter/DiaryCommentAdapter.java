package cn.snowt.diary.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.entity.Comment;
import cn.snowt.diary.service.CommentService;
import cn.snowt.diary.service.impl.CommentServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.MyConfiguration;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-29 08:45
 * @Description:
 */
public class DiaryCommentAdapter extends RecyclerView.Adapter{
    private Context context;
    private List<Comment> commentList;
    private static CommentService commentService = new CommentServiceImpl();

    public DiaryCommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(null==context){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.diary_comment_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.commentArea.setOnClickListener(v-> BaseUtils.shortTipInSnack(viewHolder.commentArea,"长按评论删除"));
        viewHolder.commentArea.setOnLongClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("确认删除此条评论吗?");
            builder.setNegativeButton("刚刚点错了",null);
            builder.setPositiveButton("确认删除", (dialog, which) -> {
                SimpleResult result = commentService.deleteById(viewHolder.commentId);
                if(result.getSuccess()){
                    BaseUtils.shortTipInSnack(viewHolder.commentArea,"删除成功，刷新后即可正常展示");
                }else{
                    BaseUtils.shortTipInSnack(viewHolder.commentArea,result.getMsg());
                }
            });
            builder.show();
            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        Comment comment = commentList.get(position);
        newHolder.username.setText(MyConfiguration.getInstance().getUsername());
        newHolder.modifyDate.setText(BaseUtils.dateToString(comment.getModifiedDate()));
        newHolder.content.setText(comment.getContent());
        newHolder.commentId = comment.getId();
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View commentArea;
        TextView username;
        TextView modifyDate;
        TextView content;
        int commentId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.commentArea = itemView;
            this.username = itemView.findViewById(R.id.comment_item_username);
            this.modifyDate = itemView.findViewById(R.id.comment_item_modifiedTime);
            this.content = itemView.findViewById(R.id.comment_item_content);
        }
    }
}
