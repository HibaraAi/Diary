package cn.snowt.mine;

import android.content.SharedPreferences;

import org.litepal.LitePalApplication;

import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.util.BaseUtils;


/**
 * @Author: HibaraAi
 * @Date: 2021-08-19 20:45
 * @Description:
 */
public class GameService {

    private List<Block> blockList;
    private int maxX;
    private int maxY;
    private int totalMineNum;
    private int totalBlockNum;
    private int openedBlockNum;
    private int surplusSign;

    private boolean gameHaveStarted;
    private Long startTime;
    private double bestTime;
    private Thread timer;
    private double usedTime2;

    public GameService(List<Block> blockList) {
        this.blockList = blockList;
        //默认方块矩阵的长和宽都是10
        this.maxX = 10;
        this.maxY = 10;
        this.totalMineNum = 12;
        this.totalBlockNum = 100;
        this.openedBlockNum = 0;
        this.gameHaveStarted = false;
        this.surplusSign = this.totalMineNum;
        this.bestTime = MineGameActivity.bestTimeInXml;
    }

    /**
     * 打开一个方块
     * @return true——游戏没输,false——游戏输了
     */
    public void open(int x,int y){
        Block centerBlock = getBlockByLocation(x,y);
        if(canBeOpen(centerBlock)){
            //启动计时器
            if(!gameHaveStarted){
                gameHaveStarted = true;
                startTime = System.currentTimeMillis();
                if(null==timer){
                    timer = new Thread(() -> {
                        while(gameHaveStarted){timer();}
                    });
                }
                timer.start();//线程启动
            }
            if(blockIsMine(centerBlock)){
               lost(x,y);
            }else{
                //点开的不是雷,递归打开附近方块
                openNearby(x,y);
                //如果"翻开数量=总方块数-总雷数"判为胜利
                if(this.openedBlockNum==this.totalBlockNum-this.totalMineNum){
                    win();
                }
            }
            MineGameActivity.flushMineUi();
        }
    }

    /**
     * 计时器
     */
    private void timer() {
        try {
            //既然看到这里了，就说一个bug给你吧，游戏结束时，这里会报一个
            //java.lang.InterruptedException异常，原因是我使用了
            //timer.interrupt();来结束这个进程
            Thread.sleep(100);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();
        //当前的游戏用时
        usedTime2 = (double)(now-startTime)/1000;
        //换成1位小数的数
        usedTime2 = (double)Math.round(usedTime2*10)/10;
        MineGameActivity.flushGameInfo(usedTime2,surplusSign,bestTime);
    }

    /**
     * 根据坐标获取方块实体
     * @param x x坐标
     * @param y y坐标
     * @return 如果坐标越界了，返回null
     */
    public Block getBlockByLocation(int x,int y){
        if(x<0 || x>=maxX || y<0 || y>=maxY){
            return null;
        }
        return blockList.get(x * 10 + y);
    }

    /**
     * 递归打开不是雷方块的附近非雷方块
     * @param x x
     * @param y y
     */
    public void openNearby(int x,int y) {
        Block centerBlock = getBlockByLocation(x,y);
        //x，y数组越界，退出递归
        if(null==centerBlock){
            return;
        }
        //当前方块被标记了或者已被打开，退出递归
        if(centerBlock.getState().equals(Block.STATE_SIGN) || centerBlock.getState().equals(Block.STATE_OPEN)){
            return;
        }
        //当前方块的周围雷数不是0,打开这个方块并退出递归
        if(!centerBlock.getNumOfMineNearby().equals(0)){
            centerBlock.setState(Block.STATE_OPEN);
            openNotMineAndShowNearbyNum(centerBlock);
            openedBlockNum++;
            return;
        }else{
            centerBlock.setState(Block.STATE_OPEN);
            openedBlockNum++;
            openNotMineAndShowNearbyNum(centerBlock);
            openNearby(x-1,y-1);
            openNearby(x-1,y);
            openNearby(x-1,y+1);
            openNearby(x,y-1);
            openNearby(x,y+1);
            openNearby(x+1,y-1);
            openNearby(x+1,y);
            openNearby(x+1,y+1);
        }
    }

    /**
     * 游戏输了，传入x和y是为了记录那个地方输了
     * @param x
     * @param y
     */
    public void lost(int x,int y) {
        blockList.forEach(block -> {
            if(block.getX()==x && block.getY()==y){
                //游戏结束的地方
                block.setImgId(R.drawable.mine_die);
            }else{
                //还没打开过的
                if(block.getState().equals(Block.STATE_DEFAULT)){
                    //没打开过的雷
                    if(block.getIsMine()){
                        block.setImgId(R.drawable.mine_default);
                    }else{
                        openNotMineAndShowNearbyNum(block);
                    }
                }
                //被标记的
                if(block.getState().equals(Block.STATE_SIGN)){
                    //标记对了
                    if(block.getIsMine()){
                        block.setImgId(R.drawable.mine_sign_true);
                    }else{
                        block.setImgId(R.drawable.mine_sign_false);
                    }
                }
            }
            block.setState(Block.STATE_OPEN);
        });
        gameHaveStarted = false;
        timer.interrupt();
        BaseUtils.longTipInCoast(LitePalApplication.getContext(),"你输了, 退出后重新加载游戏");
    }

    /**
     * 游戏胜利
     */
    private void win(){
        if(usedTime2<bestTime){
            BaseUtils.longTipInCoast(LitePalApplication.getContext(),"你赢了, 用时(秒)："+usedTime2+"。恭喜你打破本程序的最佳纪录!");
            SharedPreferences sharedPreference = BaseUtils.getSharedPreference();
            SharedPreferences.Editor edit = sharedPreference.edit();
            edit.putFloat("mineBestTime", (float) usedTime2);
            edit.apply();
        }else{
            BaseUtils.longTipInCoast(LitePalApplication.getContext(),"你赢了, 用时(秒)："+usedTime2);
        }
        gameHaveStarted = false;
        timer.interrupt();
    }

    /**
     * 可以被打开吗
     * @param block
     * @return
     */
    private Boolean canBeOpen(Block block){
        return block.getState().equals(Block.STATE_DEFAULT);
    }

    /**
     * 方块是雷吗
     * @param block
     * @return
     */
    private Boolean blockIsMine(Block block){
        return block.getIsMine();
    }

    /**
     * 打开的不是地雷方块，更改方块的状态并展示周围雷数
     * @param centerBlock
     */
    private void openNotMineAndShowNearbyNum(Block centerBlock){
        centerBlock.setState(Block.STATE_OPEN);
        switch (centerBlock.getNumOfMineNearby()){
            case 0:{
                centerBlock.setImgId(R.drawable.mine_0);
                break;
            }
            case 1:{
                centerBlock.setImgId(R.drawable.mine_1);
                break;
            }
            case 2:{
                centerBlock.setImgId(R.drawable.mine_2);
                break;
            }
            case 3:{
                centerBlock.setImgId(R.drawable.mine_3);
                break;
            }
            case 4:{
                centerBlock.setImgId(R.drawable.mine_4);
                break;
            }
            case 5:{
                centerBlock.setImgId(R.drawable.mine_5);
                break;
            }
            case 6:{
                centerBlock.setImgId(R.drawable.mine_6);
                break;
            }
            case 7:{
                centerBlock.setImgId(R.drawable.mine_7);
                break;
            }
            case 8:{
                centerBlock.setImgId(R.drawable.mine_8);
                break;
            }
            default:break;
        }
    }

    /**
     * 更换标记状态
     * @param x
     * @param y
     */
    public void changeSignState(int x, int y) {
        Block centerBlock = getBlockByLocation(x, y);
        //没打开的方块才可以没长按
        if(!centerBlock.getState().equals(Block.STATE_OPEN)){
            if(centerBlock.getState().equals(Block.STATE_SIGN)){
                centerBlock.setState(Block.STATE_DEFAULT);
                centerBlock.setImgId(R.drawable.mine_blackground);
                surplusSign++;
            }else{
                if(surplusSign<=0){
                    BaseUtils.shortTipInCoast(LitePalApplication.getContext(),"标记数已用完");
                }else{
                    centerBlock.setState(Block.STATE_SIGN);
                    centerBlock.setImgId(R.drawable.mine_sign);
                    surplusSign--;
                }
            }
            BaseUtils.createOneShotByVibrator();
        }
        MineGameActivity.flushMineUi();
    }

    /**
     * 结束计时器线程
     */
    public void stopGame(){
        if(null!=timer){
            timer.interrupt();
            timer = null;
        }
        gameHaveStarted = false;
    }
}
