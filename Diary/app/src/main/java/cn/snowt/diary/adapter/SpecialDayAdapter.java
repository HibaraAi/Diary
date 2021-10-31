package cn.snowt.diary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.activity.DayDetailActivity;
import cn.snowt.diary.vo.SpecialDayVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-09 22:23:58
 * @Description:
 */
public class SpecialDayAdapter extends RecyclerView.Adapter{
    private List<SpecialDayVo> voList;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        Integer id;
        Boolean isStop;
        ImageView image;
        TextView title;
        TextView startDay;
        TextView dayNum;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            image = itemView.findViewById(R.id.day_item_img);
            title = itemView.findViewById(R.id.day_item_title);
            startDay = itemView.findViewById(R.id.day_item_date);
            dayNum = itemView.findViewById(R.id.day_item_days);
        }
    }

    public SpecialDayAdapter(List<SpecialDayVo> voList) {
        this.voList = voList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false);
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
        SpecialDayVo vo = voList.get(position);
        if(vo.getStop()){
            CardView view = (CardView) newHolder.view;
            view.setCardBackgroundColor(Color.parseColor("#C1C0BE"));
        }
        newHolder.isStop = vo.getStop();
        newHolder.dayNum.setText(vo.getSumDay()+"");
        newHolder.id = vo.getId();
        newHolder.startDay.setText(vo.getStartDate());
        newHolder.title.setText(vo.getTitle());
        new RequestOptions()
                .placeholder(R.drawable.load_image)
                .fallback( R.drawable.bad_image)
                .error(R.drawable.bad_image);
        RequestOptions options = RequestOptions
                .bitmapTransform(new RoundedCorners(30));
        Glide.with(context)
                .load(vo.getImageSrc())
                .apply(options)
                .into(newHolder.image);
        newHolder.view.setOnClickListener(v -> {
            Intent intent = new Intent(context, DayDetailActivity.class);
            intent.putExtra(DayDetailActivity.OPEN_TYPE,DayDetailActivity.OPEN_TYPE_DETAIL);
            intent.putExtra("dayVo",vo);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return voList.size();
    }
}
