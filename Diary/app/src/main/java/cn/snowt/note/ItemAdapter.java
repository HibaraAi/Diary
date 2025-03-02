package cn.snowt.note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.R;

public class ItemAdapter extends RecyclerView.Adapter{
    private List<Item> itemList;
    private Context context;
    private ItemDao itemDao = new ItemDao();

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView textView;
        TextView dateView;
        ImageView delView;
        Integer itemId;
        String fullText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view =itemView;
            dateView = itemView.findViewById(R.id.item_date);
            textView = itemView.findViewById(R.id.item_text);
            delView =itemView.findViewById(R.id.item_del);
        }
    }

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_item, parent, false);
        if(null==context){
            context = parent.getContext();
        }
        ViewHolder viewHolder = new ViewHolder(view);
        if (parent.getId()==R.id.at_list){
            viewHolder.delView.setImageResource(R.drawable.bar_finish);
            viewHolder.delView.setOnClickListener(view1 -> finishItem(viewHolder.itemId,viewHolder.textView.getText().toString()));
        }else{
            viewHolder.delView.setOnClickListener(view1 -> delItem(viewHolder.itemId,viewHolder.textView.getText().toString()));
        }
        viewHolder.textView.setOnClickListener(view1 -> showItem(viewHolder.fullText));
        viewHolder.textView.setOnLongClickListener(v -> {
            updateItem(viewHolder.itemId,viewHolder.fullText);
            return true;
        });
        return viewHolder;
    }

    private void finishItem(Integer itemId, String toString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("已完成？");
        builder.setMessage(toString);
        builder.setPositiveButton("已完成", (dialog, which) -> {
            itemDao.finishOneById(itemId);
            ItemAdapter.this.itemList = itemDao.getAllUnfinished();
            ItemAdapter.this.notifyDataSetChanged();
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    private void showItem(String fullText) {
        if (fullText.length()<25) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("详情");
        builder.setMessage(fullText);
        builder.setNegativeButton("OK",null);
        builder.setCancelable(false);
        builder.show();
    }

    private void delItem(Integer itemId, String toString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("是否删除？");
        builder.setMessage(toString);
        builder.setPositiveButton("删除", (dialog, which) -> {
            itemDao.delOneById(itemId);
            ItemAdapter.this.itemList = itemDao.getAllFinishDescCreate();
            ItemAdapter.this.notifyDataSetChanged();
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateItem(Integer itemId, String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("修改");
        EditText editText = new EditText(context);
        editText.setLines(10);
        editText.setGravity(Gravity.TOP);
        editText.setBackgroundResource(R.drawable.edge);
        editText.setHint("输入......");
        editText.setText(s);
        editText.setPadding(5,5,5,5);
        builder.setView(editText);
        builder.setPositiveButton("修改", (dialog, which) -> {
            String text = editText.getText().toString();
            if(text.isEmpty()){
                Toast.makeText(context,"不能为空!",Toast.LENGTH_SHORT).show();
            }else{
                itemDao.updateById(itemId,text);
                ItemAdapter.this.itemList = itemDao.getAllUnfinished();
                ItemAdapter.this.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder newHolder = (ViewHolder) holder;
        Item item = itemList.get(position);
        String content = item.getContent();
        newHolder.textView.setText(content.length()<25? content :(content.substring(0,22)+"..."));
        String timeText = "创建："+dateToString(item.getCreateDate());
        if(null!=item.getFinishDate()){
            timeText = timeText+"    完成："+dateToString(item.getFinishDate());
        }
        newHolder.dateView.setText(timeText);
        newHolder.itemId = item.getId();
        newHolder.fullText = item.getContent();
    }

    private String dateToString(Date date) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String format = sdf.format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
            format += " 周日";
        } else if (weekday == 2) {
            format += " 周一";
        } else if (weekday == 3) {
            format += " 周二";
        } else if (weekday == 4) {
            format += " 周三";
        } else if (weekday == 5) {
            format += " 周四";
        } else if (weekday == 6) {
            format += " 周五";
        } else if (weekday == 7) {
            format += " 周六";
        }
        return format;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
