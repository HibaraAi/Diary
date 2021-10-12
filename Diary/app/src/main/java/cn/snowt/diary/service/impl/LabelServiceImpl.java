package cn.snowt.diary.service.impl;

import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.service.LabelService;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-05 18:07
 * @Description: 前期实体类不好好想，现在又想加这么多功能？？？？
 * 和标签有关的方法都和猪一样sb，
 * 下次再加功能直接新建一个新项目好吧，*****
 * 2021-10-09 18点39分
 */
public class LabelServiceImpl implements LabelService {
    @Override
    public List<String> parseLabelFromStr(String labelStr) {
        if(null==labelStr || "".equals(labelStr)){
            return null;
        }
        final String[] items = labelStr.split("##");
        if(items.length>1){
            items[0] = items[0]+"#";
            items[items.length-1] = "#"+items[items.length-1];
        }
        if(items.length>=3){
            for(int i=1;i<=items.length-2;i++){
                items[i] = "#"+items[i]+"#";
            }
        }
        return new ArrayList<String>(Arrays.asList(items));
    }

    @Override
    public SimpleResult addSameLabel(String sameLabel) {
        sameLabel = sameLabel.trim();
        List<String> list = parseLabelFromStr(sameLabel);
        int num = 0;
        for (char c : sameLabel.toCharArray()) {
            if(c == '#'){
                num++;
            }
        }
        boolean flag = (num%2!=0 || num==0);
        if(null==list || list.isEmpty() || flag){
            return SimpleResult.error().msg("标签解析失败，请严格按照#AA##BB#形式添加");
        }
        if (list.size()==1){
            return SimpleResult.error().msg("才一个标签,何来的相同？已禁止添加");
        }
        Map<Integer,String> existingLabel = getAllSameLabel();
        Map<Integer,String> newMap = new HashMap<>();
        existingLabel.forEach((integer, s) -> newMap.put(newMap.size()+1, s));
        //循环结束标记
        AtomicInteger stopFlag = new AtomicInteger();
        stopFlag.set(-1);
        //因为哪个标签结束的循环
        AtomicReference<String> hadLabel = new AtomicReference<>();
        list.forEach(label->{
            if(stopFlag.get()==-1){
                newMap.forEach((integer, s) -> {
                    if(s.contains(label)){
                        stopFlag.set(integer);
                        hadLabel.set(label);
                        return;
                    }
                });
            }
        });
        if(stopFlag.get()!=-1){
            //存在已有标签
            String labelInMap = newMap.get(stopFlag.get());
            if(labelInMap.equals(sameLabel)){
                return SimpleResult.error().msg("已经有完全一样的同名标签了，不用重复添加");
            }
            List<String> list1 = parseLabelFromStr(labelInMap);
            Set<String> all = new HashSet<>();
            all.addAll(list1);all.addAll(list);
            StringBuilder builder = new StringBuilder();
            all.forEach(builder::append);
            newMap.put(stopFlag.get(),builder.toString());
            String jsonString = JSON.toJSONString(newMap);
            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
            edit.putString("sameLabel",jsonString);
            edit.apply();
            return SimpleResult.ok().msg("由于新添加的标签"+hadLabel.get()+"已存在同名标签库中，因此此次添加的同名标签以追加的方式加在已有同名标签后面。");
        }else{
            //全新的同名标签
            newMap.put(newMap.size()+1, sameLabel);
            String jsonString = JSON.toJSONString(newMap);
            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
            edit.putString("sameLabel",jsonString);
            edit.apply();
            return SimpleResult.ok().msg("全新的同名标签已添加");
        }
    }

    @Override
    public Map<Integer,String> getAllSameLabel() {
        String sameLabel = BaseUtils.getSharedPreference().getString("sameLabel", "");
        Map<Integer,String> map = JSON.parseObject(sameLabel, Map.class);
        if(map==null){
            map = new HashMap<>();
        }
        return map;
    }

    @Override
    public SimpleResult updateSameLabel(Integer id,String newSameLabel) {
        newSameLabel = newSameLabel.trim();
        Map<Integer,String> existingLabel = getAllSameLabel();
        if("".equals(newSameLabel)){
            existingLabel.remove(id);
            Map<Integer,String> newMap = new HashMap<>(existingLabel.size());
            existingLabel.forEach((integer, s) -> newMap.put(newMap.size()+1,s));
            String jsonString = JSON.toJSONString(newMap);
            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
            edit.putString("sameLabel",jsonString);
            edit.apply();
            return SimpleResult.ok().msg("标签已删除");
        }
        List<String> list = parseLabelFromStr(newSameLabel);
        int num = 0;
        for (char c : newSameLabel.toCharArray()) {
            if(c == '#'){
                num++;
            }
        }
        boolean flag = (num%2!=0 || num==0);
        if(null==list || list.isEmpty() || flag){
            return SimpleResult.error().msg("标签解析失败，请严格按照#AA##BB#形式添加");
        }else if (list.size()==1){
            return SimpleResult.error().msg("才一个标签,何来的相同？已禁止修改");
        }
        //新加的标签已在其他同名标签
        AtomicReference<String> hadFlag = new AtomicReference<>("");
        Map<Integer,String> existingLabel2 = getAllSameLabel();
        existingLabel2.remove((Integer)id);
        list.forEach(l->{
            if("".equals(hadFlag.get())){
                existingLabel2.forEach((integer, s) -> {
                    if(s.contains(l)){
                        hadFlag.set(l);
                        return;
                    }
                });
            }else{
                return;
            }
        });
        if(!"".equals(hadFlag.get())){
            return SimpleResult.error().msg(hadFlag.get()+"在其他同名标签中出现过，请在该标签中修改，如果想合并两个已有的同名标签，请先删除一个再手动增加");
        }
        existingLabel.put(id,newSameLabel);
        String jsonString = JSON.toJSONString(existingLabel);
        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
        edit.putString("sameLabel",jsonString);
        edit.apply();
        return SimpleResult.ok().msg("同名标签已更新");
    }

    @Override
    public List<String> getSameLabelsByOne(String label) {
        Map<Integer,String> map = getAllSameLabel();
        AtomicReference<Integer> flag = new AtomicReference<>();
        flag.set(-1);
        map.forEach((integer, s) -> {
            if(s.contains(label)){
                flag.set(integer);
                return;
            }
        });
        List<String> list;
        if(flag.get()==-1){
            list = new ArrayList<>();
            list.add(label);
        }else{
            list = parseLabelFromStr(map.get(flag.get()));
        }
        return list;
    }
}
