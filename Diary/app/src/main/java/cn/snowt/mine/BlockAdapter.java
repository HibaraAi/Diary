package cn.snowt.mine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.snowt.diary.R;


/**
 * @Author: HibaraAi
 * @Date: 2021-08-19 18:28
 * @Description:
 */
public class BlockAdapter extends RecyclerView.Adapter{
    private Context context;
    private List<Block> blockList;
    private GameService gameService;

    static class ViewHolder extends RecyclerView.ViewHolder{
        int x;
        int y;
        CardView cardView;
        ImageView blockImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            blockImage = itemView.findViewById(R.id.block_image);
        }
    }

    public BlockAdapter(List<Block> blockList, GameService gameService) {
        this.blockList = blockList;
        this.gameService = gameService;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(null==context){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.block_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.blockImage.setOnClickListener(v-> {
            gameService.open(viewHolder.x,viewHolder.y);
        });
        viewHolder.blockImage.setOnLongClickListener(v -> {
            gameService.changeSignState(viewHolder.x,viewHolder.y);
            return true;
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Block block = blockList.get(position);
        ViewHolder newHolder = (ViewHolder) holder;
        newHolder.x = block.getX();
        newHolder.y = block.getY();
        Glide.with(context).load(block.getImgId()).into(newHolder.blockImage);
    }

    @Override
    public int getItemCount() {
        return blockList.size();
    }
}
