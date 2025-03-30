package cn.snowt.blog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DiaryListActivity;
import cn.snowt.diary.util.BaseUtils;

/**
 * @Author : HibaraAi github.com/HibaraAi
 * @Date : on 2025-02-15 11:26.
 * @Description :
 */
public class BlogAdapter extends  RecyclerView.Adapter{

    private List<BlogSimpleVo> voList;
    private Context context;
    private BlogService blogService;

    public BlogAdapter(List<BlogSimpleVo> voList, Context context) {
        this.voList = voList;
        this.context = context;
        this.blogService = new BlogService();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;  //整个View
        ImageView imageView;
        TextView dateView;  //创建时间的View
        TextView labelView;  //标签的View
        TextView titleView;  //标题的View
        TextView contentView;  //正文的View
        Integer blogId;  //BlogId

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view =itemView;
            imageView = itemView.findViewById(R.id.blog_item_img);
            dateView = itemView.findViewById(R.id.blog_item_date);
            labelView = itemView.findViewById(R.id.blog_item_label);
            titleView =itemView.findViewById(R.id.blog_item_title);
            contentView =itemView.findViewById(R.id.blog_item_content);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rw_item_blog, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.view.setOnClickListener(v->{
            Intent intent = new Intent(context, BlogDetailActivity.class);
            intent.putExtra(BlogDetailActivity.INTENT_BLOG_ID,viewHolder.blogId);
            context.startActivity(intent);
        });
        //点击标签
        viewHolder.labelView.setOnClickListener(v-> responseToClickLabel(viewHolder.labelView.getText().toString()));
        viewHolder.labelView.setOnLongClickListener(v -> {
            String allLabel = viewHolder.labelView.getText().toString();
            BaseUtils.copyInClipboard(context,allLabel);
            BaseUtils.shortTipInSnack(view,"已复制: "+allLabel);
            return true;
        });
        return viewHolder;
    }

    /**
     * 相应被标签被点击
     * @param allLabel 需要提供被点击标签的值
     */
    private void responseToClickLabel(String allLabel){
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
        builder.setTitle("请选择一个标签,将展示同标签的所有日记和Blog");
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
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        BlogSimpleVo vo = voList.get(position);
        newHolder.blogId = vo.getId();
        newHolder.titleView.setText(vo.getTitle());
        newHolder.contentView.setText(vo.getSimpleContent());
        newHolder.dateView.setText(vo.getDate());
        if(null!=vo.getMediaSrc() && !"".equals(vo.getMediaSrc())){
            RequestOptions options = RequestOptions
                    .bitmapTransform(new RoundedCorners(10))
                    .placeholder(R.drawable.loading)
                    .fallback( R.drawable.img_load_fail)
                    .error(R.drawable.img_load_fail);
            Glide.with(context)
                    .load(vo.getMediaSrc())
                    .apply(options)
                    .into(newHolder.imageView);
        }
        newHolder.labelView.setText(vo.getLabelStr());
    }

    @Override
    public int getItemCount() {
        return voList.size();
    }
}
