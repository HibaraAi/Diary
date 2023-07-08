package cn.snowt.note;

import com.alibaba.fastjson.JSON;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemDao {
    /**
     * 所有未完成的便签
     * @return
     */
    List<Item> getAllUnfinished(){
        List<Item> list = new ArrayList<>();
        List<Item> items = LitePal.order("createDate desc").find(Item.class);
        for(int i=0;i<items.size();i++){
            if(!Item.STATE_FINISH.equals(items.get(i).getState())){
                list.add(items.get(i));
            }
        }
        return list;
    }

    Item getOneById(Integer id){
        return LitePal.find(Item.class, id);
    }

    void addOne(Item item){
        item.save();
    }

    void delOneById(Integer id){
        Item oneById = getOneById(id);
        if(null!=oneById){
            oneById.delete();
        }
    }

    void updateById(Integer id,String text){
        Item oneById = getOneById(id);
        oneById.setContent(text);
        oneById.update(id);
    }

    public void finishOneById(Integer itemId) {
        Item oneById = getOneById(itemId);
        oneById.setState(Item.STATE_FINISH);
        oneById.setFinishDate(new Date());
        oneById.save();
    }

    public List<Item> getAllFinishDescCreate() {
        List<Item> list = LitePal.where("state == "+Item.STATE_FINISH).order("createDate desc").find(Item.class);
        if (null==list){
            list = new ArrayList<>();
        }
        return list;
    }

    public List<Item> getAllFinishDescFinish() {
        List<Item> list = LitePal.where("state == "+Item.STATE_FINISH).order("finishDate desc").find(Item.class);
        if (null==list){
            list = new ArrayList<>();
        }
        return list;
    }

    public String getAllDataToJSON(){
        List<Item> all = LitePal.findAll(Item.class);
        if(all.size()==0){
            return "";
        }else{
            return JSON.toJSONString(all);
        }
    }

    public void addFromJson(String tipJson) {
        if(null==tipJson || "".equals(tipJson)){
            return;
        }
        List<Item> item = JSON.parseArray(tipJson, Item.class);
        if(null!=item){
            item.forEach(item1 -> {
                item1.assignBaseObjId(0);
                item1.save();
            });
        }
    }
}
