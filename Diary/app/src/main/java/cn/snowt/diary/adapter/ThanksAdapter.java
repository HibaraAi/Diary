package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
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

import cn.snowt.diary.R;
import cn.snowt.diary.vo.ThanksVo;

/**
 * @Author: HibaraAi
 * @Date: 2022-04-22 19:09:13
 * @Description: 鸣谢RecyclerView的适配器
 */
public class ThanksAdapter  extends RecyclerView.Adapter{
    private List<ThanksVo> voList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        ImageView headImg;
        TextView name;
        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            headImg = itemView.findViewById(R.id.thanks_item_img);
            name = itemView.findViewById(R.id.thanks_item_name);
            text = itemView.findViewById(R.id.thanks_item_text);
        }
    }

    public ThanksAdapter(List<ThanksVo> voList) {
        this.voList = voList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.thanks_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        ThanksVo thanksVo = voList.get(position);
        newHolder.text.setText(thanksVo.getText());
        newHolder.name.setText(thanksVo.getName());
        new RequestOptions()
                .placeholder(R.drawable.load_image)
                .fallback(R.drawable.bad_image)
                .error(R.drawable.bad_image);
        RequestOptions options = RequestOptions
                .bitmapTransform(new RoundedCorners(30));
        Glide.with(context)
                .load(thanksVo.getHeadImgId())
                .apply(options)
                .into(newHolder.headImg);
    }

    @Override
    public int getItemCount() {
        return voList.size();
    }

//    private List<ThanksVo> thanksVoList;
//    private Context context;
//
//    static class ViewHolder extends RecyclerView.ViewHolder{
//        View view;
//        TextView nameView;
//        TextView textView;
//        ImageView headView;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            view = itemView;
//            nameView = itemView.findViewById(R.id.thank_item_name);
//            textView = itemView.findViewById(R.id.thank_item_text);
//            headView = itemView.findViewById(R.id.thank_item_head);
//        }
//    }
//
//    public ThanksAdapter(List<ThanksVo> thanksVoList) {
//        this.thanksVoList = thanksVoList;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.thanks_item, parent, false);
//        if(null==context){
//            context = parent.getContext();
//        }
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        ViewHolder newHolder = (ViewHolder) holder;
//        ThanksVo thanksVo = thanksVoList.get(position);
//        newHolder.textView.setText(thanksVo.getText());
//        newHolder.nameView.setText(thanksVo.getName());
//        Glide.with(context)
//                .load(thanksVo.getHeadImgId())
//                .into(newHolder.headView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return thanksVoList.size();
//    }


}
