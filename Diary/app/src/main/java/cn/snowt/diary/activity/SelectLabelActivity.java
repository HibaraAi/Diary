package cn.snowt.diary.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.snowt.blog.BlogService;
import cn.snowt.diary.R;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.ChineseCharUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2025-05-11 15:55
 * @Description: 选择已有标签
 */
public class SelectLabelActivity extends AppCompatActivity {
    private final DiaryService diaryService = new DiaryServiceImpl();
    private final BlogService blogService = new BlogService();

    private RecyclerView recyclerView;
    private static TextView tipView;
    private Toolbar toolbar;
    private SearchView searchView;

    private List<DiaryVo> diaryList;
    private DiaryAxisAdapterForSelectLabel adapter;
    private Integer openType;

    private List<String> searchHelp = new ArrayList<>();
    private List<DiaryVo> diaryListBackup;

    static List<Integer> beSelectedToDelID = new ArrayList<>();

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗

    public static String beSelectLabelStr = "";

    private static Set<String> beSelectLabelSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_axis);
        bindViewAndSetListener();
        searchView.setVisibility(View.VISIBLE);
        showAllLabel();
    }


    private void bindViewAndSetListener() {
        beSelectLabelStr = "";
        beSelectLabelSet = new HashSet<>();
        recyclerView = findViewById(R.id.axis_recyclerview);
        View parent = (View) recyclerView.getParent();
        if(this.getResources().getConfiguration().uiMode == 0x11){
            //parent.setBackgroundResource(R.drawable.day_detail_bg);
            parent.setBackgroundColor(Color.parseColor("#eeeeee"));
        }else{
//            parent.setBackgroundResource(R.drawable.night_bg);
            parent.setBackgroundColor(Color.parseColor("#212b2e"));
        }
        tipView = findViewById(R.id.axis_tip);
        toolbar = findViewById(R.id.axis_toolbar);
        searchView = findViewById(R.id.axis_search);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if(null!=supportActionBar){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                if("".equals(newText)){
                    diaryList.clear();
                    diaryList.addAll(diaryListBackup);
                }else{
                    diaryList.clear();
                    searchHelp.forEach(s->{
                        if(s.contains(newText)){
                            int index = searchHelp.indexOf(s);
                            diaryList.add(diaryListBackup.get(index));
                        }
                    });
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }


    /**
     * 展示所有标签
     */
    private void showAllLabel(){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("选择已有标签");
        Set<String> labelsUnsplit = diaryService.getAllLabelsUnsplit();
        if(labelsUnsplit.isEmpty()){
            tipView.setText("系统中还没有日记加过标签");
        }else{
            int sizeUnsplit = labelsUnsplit.size();
            Set<String> labels1 = diaryService.getAllLabels();
            int sizeSplit = labels1.size();
            labelsUnsplit.addAll(labels1);
            tipView.setText("在所有日记中，共统计到["+sizeUnsplit+"]个完整标签、["+sizeSplit+"]个分解标签。\n搜索只支持输入小写字母。");
            List<DiaryVo> vos = new ArrayList<>();
            labelsUnsplit.forEach(label -> {
                DiaryVo vo = new DiaryVo();
                vo.setId(-1);
                vo.setContent(label);
                vo.setModifiedDate("");
                vo.setPicSrcList(new ArrayList<>());
                vos.add(vo);
            });
            diaryList = vos;
            adapter = new DiaryAxisAdapterForSelectLabel(diaryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            diaryListBackup = new ArrayList<>(diaryList.size());
            diaryList.forEach(diaryVo -> {
                searchHelp.add(ChineseCharUtils.getAllFirstLetter(diaryVo.getContent()).toLowerCase());
                diaryListBackup.add(diaryVo);
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_label_finish:{
                Intent intent = new Intent();
                intent.putExtra("beSelectLabelStr",beSelectLabelStr);
                setResult(RESULT_OK,intent);
                finish();
            }
            default:break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_finish,menu);
        return true;
    }

    /**
     * 需要更换点击事件，所以换了一个Adapter
     */
    private static class DiaryAxisAdapterForSelectLabel extends RecyclerView.Adapter{
        private List<DiaryVo> diaryVoList;
        private Context context;


        static class ViewHolder extends RecyclerView.ViewHolder{
            View view;
            TextView dateView;
            TextView diaryCutView;
            int diaryId;
            boolean beSelected =false;
            String time;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;
                dateView = itemView.findViewById(R.id.axis_item_date);
                diaryCutView = itemView.findViewById(R.id.axis_item_diary_cut);
            }
        }

        public DiaryAxisAdapterForSelectLabel(List<DiaryVo> diaryVoList) {
            this.diaryVoList = diaryVoList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.diary_axis_item, parent, false);
            if(null==context){
                context = parent.getContext();
            }
            final ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.dateView.setBackground(null);
            viewHolder.view.setOnClickListener(v->{
                Set<String> labelSet = strToSet(viewHolder.diaryCutView.getText().toString());
                labelSet.forEach(s -> {
                    if(beSelectLabelSet.contains(s)){
                        beSelectLabelSet.remove(s);
                    }else{
                        beSelectLabelSet.add(s);
                    }
                });
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : beSelectLabelSet) {
                    stringBuilder.append(s);
                }
                beSelectLabelStr = stringBuilder.toString();
//                if(!viewHolder.beSelected){
//                    //改为被选中
//                    viewHolder.beSelected = true;
//                    viewHolder.dateView.setText("被选中");
//                    viewHolder.dateView.setBackgroundResource(R.drawable.axis_time4);
//                    Set<String> labelSet = strToSet(viewHolder.diaryCutView.getText().toString());
//                    //beSelectLabelStr = beSelectLabelStr +viewHolder.diaryCutView.getText().toString();
//                    beSelectLabelSet.addAll(labelSet);
//                }else{
//                    //改为不选中
//                    viewHolder.beSelected = false;
//                    viewHolder.dateView.setText(viewHolder.time);
//                    viewHolder.dateView.setBackgroundResource(R.drawable.axis_time2);
//                    Set<String> labelSet = strToSet(viewHolder.diaryCutView.getText().toString());
//                    beSelectLabelSet.removeAll(labelSet);
//                    //beSelectLabelStr = beSelectLabelStr.replaceFirst(viewHolder.diaryCutView.getText().toString(),"");
//                }
                BaseUtils.shortTipInSnack(view, beSelectLabelStr);
                tipView.setText(beSelectLabelStr);
            });
            return viewHolder;
        }

        /**
         * 将字符串标签解析为单个标签的Set
         * @param labelStr 字符串标签
         * @return Set，解析失败就返回空集合
         */
        private Set<String> strToSet(String labelStr) {
            HashSet<String> hashSet = new HashSet<>();
            if(null==labelStr || labelStr.isEmpty()){
                return hashSet;
            }else{
                String[] items = labelStr.split("##");
                if(items.length>1){
                    items[0] = items[0]+"#";
                    items[items.length-1] = "#"+items[items.length-1];
                }
                if(items.length>=3){
                    for(int i=1;i<=items.length-2;i++){
                        items[i] = "#"+items[i]+"#";
                    }
                }
                hashSet.addAll(Arrays.asList(items));
                return hashSet;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ViewHolder newHolder = (ViewHolder) holder;
            DiaryVo diaryVo = diaryVoList.get(position);
            newHolder.diaryId = diaryVo.getId();
            newHolder.time = diaryVo.getModifiedDate();
            newHolder.diaryCutView.setText(diaryVo.getContent());
//            if(beSelectedToDelID.contains(newHolder.diaryId)){
//                newHolder.beSelected = true;
//                newHolder.dateView.setText("被选中");
//                newHolder.dateView.setBackgroundResource(R.drawable.axis_time4);
//            }else{
//                //改为不选中
//                newHolder.dateView.setText(newHolder.time);
//                newHolder.dateView.setBackgroundResource(R.drawable.axis_time2);
//                newHolder.beSelected = false;
//            }
        }

        @Override
        public int getItemCount() {
            return diaryVoList.size();
        }
    }
}