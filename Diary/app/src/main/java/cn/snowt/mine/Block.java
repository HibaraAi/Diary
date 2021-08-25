package cn.snowt.mine;


import cn.snowt.diary.R;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-19 17:24
 * @Description: 方块
 */
public class Block {
    /**
     * 已被打开
     */
    public final static int STATE_OPEN = 1;

    /**
     * 被玩家标记成地雷
     */
    public final static int STATE_SIGN = 2;

    /**
     * 初始的默认状态
     */
    public final static int STATE_DEFAULT = 3;

    /**
     * x坐标
     */
    private Integer x;

    /**
     * y坐标
     */
    private Integer y;

    /**
     * 是否是地雷方块
     */
    private Boolean isMine;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 附近的雷的数量
     */
    private Integer numOfMineNearby;

    /**
     * 当前图片的Id
     */
    private Integer imgId;

    public Block(Integer x, Integer y, Boolean isMine) {
        this.x = x;
        this.y = y;
        this.isMine = isMine;
        this.state = STATE_DEFAULT;
        this.imgId = R.drawable.mine_blackground;
        this.numOfMineNearby = -1;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Boolean getIsMine() {
        return isMine;
    }

    public void setIsMine(Boolean isMine) {
        this.isMine = isMine;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getNumOfMineNearby() {
        return numOfMineNearby;
    }

    public void setNumOfMineNearby(Integer numOfMineNearby) {
        this.numOfMineNearby = numOfMineNearby;
    }

    public Integer getImgId() {
        return imgId;
    }

    public void setImgId(Integer imgId) {
        this.imgId = imgId;
    }


}
