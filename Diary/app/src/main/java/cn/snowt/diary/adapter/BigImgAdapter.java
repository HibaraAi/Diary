package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.ZoomImageActivity;
import cn.snowt.diary.view.ZoomImageView;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-19 09:48:47
 * @Description:
 */
public class BigImgAdapter extends RecyclerView.Adapter {

    private final List<String> imgSrcList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView tip;
        String imgSrc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img_item_img);
            tip = itemView.findViewById(R.id.img_item_tip);
        }
    }

    public BigImgAdapter(List<String> imgSrc) {
        this.imgSrcList = imgSrc;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_image_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        ViewHolder viewHolder = new ViewHolder(view);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.load_image)//图片加载出来前，显示的图片
                .fallback( R.drawable.bad_image) //url为空的时候,显示的图片
                .error(R.drawable.bad_image);//图片加载失败后，显示的图片
        Glide.with(context).load("/dsfdsfdsfdsdfsdfsdfdsfds").apply(options).into(viewHolder.imageView);
        viewHolder.tip.setOnClickListener(v->{
            Intent intent = new Intent(context, ZoomImageActivity.class);
            intent.putExtra(ZoomImageActivity.EXTRA_IMAGE_SRC,viewHolder.imgSrc);
            context.startActivity(intent);
        });
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        String imgSrc = imgSrcList.get(position);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.load_image)//图片加载出来前，显示的图片
                .fallback( R.drawable.bad_image) //url为空的时候,显示的图片
                .error(R.drawable.bad_image);//图片加载失败后，显示的图片
        Glide.with(context).load(imgSrc).apply(options).into(newHolder.imageView);
        newHolder.tip.setText((position+1)+"/"+imgSrcList.size()+"  (点我?)");
        newHolder.imgSrc = imgSrc;
    }

    @Override
    public int getItemCount() {
        return imgSrcList.size();
    }
}
