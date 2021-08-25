package cn.snowt.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;


/**
 * @Author: HibaraAi
 * @Date: 2021-08-19 17:33
 * @Description: 扫雷游戏
 */
public class MineGameActivity extends AppCompatActivity {
    public static List<Block> blockList;
    private static BlockAdapter adapter;
    private static GameService gameService;
    private static RecyclerView recyclerView;
    private static Context context;
    private static TextView mineSignSurplus;
    private static TextView bestTimeUsed;
    private static TextView haveUsedTime;
    public static double bestTimeInXml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_game);
        bestTimeUsed = findViewById(R.id.mine_best_time);
        initMine();
        initInfo();
        context = MineGameActivity.this;
        mineSignSurplus = findViewById(R.id.mine_sign_surplus);
        haveUsedTime = findViewById(R.id.mine_now_used_time);
        gameService = new GameService(blockList);
        recyclerView = findViewById(R.id.block_recyclerview);
        findViewById(R.id.mine_btn_play_again).setOnClickListener(v->{
            gameService.stopGame();
            initMine();
            initInfo();
            gameService = new GameService(blockList);
            GridLayoutManager layoutManager = new GridLayoutManager(context, 10);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new BlockAdapter(blockList, gameService);
            recyclerView.setAdapter(adapter);
            haveUsedTime.setText("当前用时(秒):0");
            mineSignSurplus.setText("剩余标记数(个):12");
        });
        GridLayoutManager layoutManager = new GridLayoutManager(context, 10);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BlockAdapter(blockList, gameService);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 初始化信息展示区
     */
    private void initInfo() {
        SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
        float mineBestTime = sharedPreference.getFloat("mineBestTime", 3600);
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(mineBestTime));
        bestTimeInXml = bigDecimal.doubleValue();
        flushGameInfo(0.0,12,bestTimeInXml);
    }

    /**
     * 初始化扫雷方块区
     */
    private void initMine(){
        blockList = new ArrayList<>(100);
        Random rand = new Random();
        int[][] tempMine = new int[10][10];
        //已经生成的雷的个数。需要12个
        int hadSetMineNum = 0;
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                //生成完毕，跳出最大的循环
                if(12==hadSetMineNum){
                    i = 11;
                    break;
                }
                //指定数值为1的为雷
                if(1!=tempMine[i][j]){
                    tempMine[i][j] = rand.nextInt(10*10/12+1);
                    if(1==tempMine[i][j]){
                        hadSetMineNum++;
                    }
                }
                //若到了最后一个方块，雷还不够，从头继续赋值
                if(i==9 && j==9 && 12!=hadSetMineNum) {
                    i=0;j=0;
                }
            }
        }
        //地雷放置完毕,计算各个方块周围雷的个数
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                if(tempMine[i][j]==1){
                    Block block = new Block(i,j,true);
                    blockList.add(block);
                }else{
                    Block block = new Block(i,j,false);
                    blockList.add(block);
                }
            }
        }
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                int numOfMineNearby = 0;
                Block block1 = getBlockByLocation(i - 1, j - 1);
                Block block2 = getBlockByLocation(i - 1, j );
                Block block3 = getBlockByLocation(i - 1, j + 1);
                Block block4 = getBlockByLocation(i , j - 1);
                Block block6 = getBlockByLocation(i , j + 1);
                Block block7 = getBlockByLocation(i + 1, j - 1);
                Block block8 = getBlockByLocation(i + 1, j );
                Block block9 = getBlockByLocation(i + 1, j + 1);
                if (block1 != null && block1.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block2 != null && block2.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block3 != null && block3.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block4 != null && block4.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block6 != null && block6.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block7 != null && block7.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block8 != null && block8.getIsMine()) {
                    numOfMineNearby++;
                }
                if (block9 != null && block9.getIsMine()) {
                    numOfMineNearby++;
                }
                Block block = getBlockByLocation(i, j);
                if (block != null) {
                    block.setNumOfMineNearby(numOfMineNearby);
                }
                blockList.set(i*10+j,block);
            }
        }
    }

    /**
     * 根据坐标获取方块实体
     * @param x x坐标
     * @param y y坐标
     * @return 如果坐标越界了，返回null
     */
    private Block getBlockByLocation(int x,int y){
        if(x<0 || x>=10 || y<0 || y>=10){
            return null;
        }
        return blockList.get(x * 10 + y);
    }

    /**
     * 刷新方块矩阵
     */
    @SuppressLint("NotifyDataSetChanged")
    public static void flushMineUi(){
        adapter.notifyDataSetChanged();
    }

    /**
     * 刷新游戏信息
     * @param usedTime 已用时
     * @param surplusSign 剩余标记数
     * @param bestTime 最佳用时
     */
    public static void flushGameInfo(double usedTime,int surplusSign,double bestTime){
        if(null!=mineSignSurplus){
            mineSignSurplus.setText("剩余标记数(个):"+surplusSign);
        }
        if(null!=bestTimeUsed){
            bestTimeUsed.setText("最佳用时(秒):"+bestTime);
        }
        if(null!=haveUsedTime){
            haveUsedTime.setText("当前用时(秒):"+usedTime);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameService.stopGame();
    }
}