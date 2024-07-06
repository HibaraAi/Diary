package cn.snowt.diary.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.litepal.LitePal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.DiaryAxisAdapter;
import cn.snowt.diary.async.MyAsyncTask;
import cn.snowt.diary.async.GetSimpleDiaryByDateTask;
import cn.snowt.diary.entity.TempDiary;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.ChineseCharUtils;
import cn.snowt.diary.util.SimpleResult;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-09-01 14:16
 * @Description: 简易日记的RecyclerView展示UI，class旧名字叫TimeAxisActivity
 */
public class DiaryListActivity extends AppCompatActivity {
    public static final String TAG = "DiaryListActivity";
    public static final String OPEN_FROM_TYPE = "openType";

    public static final String DATE_ONE = "date1";
    public static final String DATE_TWO = "date2";

    public static final int OPEN_FROM_TIME_AXIS = 1;
    public static final int OPEN_FROM_TEMP_DIARY = 2;
    public static final int OPEN_FROM_SEARCH_DIARY = 3;
    public static final int OPEN_FROM_SEARCH_LABEL = 4;
    public static final int OPEN_FROM_LABEL_LIST = 5;
    public static final int OPEN_FROM_FULL_SEARCH = 6;
    public static final int OPEN_FROM_DELETE_LIST = 7;
    public static final int OPEN_FROM_ASYNC_SEARCH = 8;

    private final DiaryService diaryService = new DiaryServiceImpl();

    private RecyclerView recyclerView;
    private TextView tipView;
    private Toolbar toolbar;
    private SearchView searchView;

    private List<DiaryVo> diaryList;
    private DiaryAxisAdapter adapter;
    private Integer openType;

    private List<String> searchHelp = new ArrayList<>();
    private List<DiaryVo> diaryListBackup;

    static List<Integer> beSelectedToDelID = new ArrayList<>();

    private androidx.appcompat.app.AlertDialog ProgressADL;  //进度条的弹窗
    Handler handler = new Handler(new Handler.Callback() {  //处理异步回调
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MyAsyncTask.FINISH_TASK:{
                    ProgressADL.cancel();
                    SimpleResult result = (SimpleResult) msg.obj;
                    if(!result.getSuccess()){
                        tipView.setText("【BUG】由于神秘力量，查询异常...");
                    }else{
                        switch (openType) {
                            case OPEN_FROM_DELETE_LIST:{
                                diaryList = (List<DiaryVo>) result.getData();
                                afterShowDeleteList2();
                                break;
                            }
                            case OPEN_FROM_TIME_AXIS:{
                                diaryList = (List<DiaryVo>) result.getData();
                                String[] strings = result.getMsg().split("#");
                                afterShowSimpleDiary2(strings[0],strings[1]);
                                break;
                            }
                            default:{
                                tipView.setText("【BUG】程序异常......");
                                break;
                            }
                        }

                    }
                    break;
                }
                case MyAsyncTask.START_TASK:{
                    showProgressAlertDialog();
                    break;
                }
                default:break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //禁止截屏设置
        boolean notAllowScreenshot = BaseUtils.getDefaultSharedPreferences().getBoolean("notAllowScreenshot", true);
        if(notAllowScreenshot){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_time_axis);
        bindViewAndSetListener();
        Intent intent = getIntent();
        openType = intent.getIntExtra(OPEN_FROM_TYPE, -1);
        switch (openType) {
            case OPEN_FROM_TIME_AXIS:{
//                showSimpleDiary(intent.getStringExtra(DATE_ONE),intent.getStringExtra(DATE_TWO));
                showSimpleDiary2(intent.getStringExtra(DATE_ONE),intent.getStringExtra(DATE_TWO));
                break;
            }
            case OPEN_FROM_TEMP_DIARY:{
                showSimpleDiary();
                break;
            }
            case OPEN_FROM_SEARCH_DIARY:{
                showSimpleDiary(intent.getIntegerArrayListExtra("ids"),intent.getStringExtra("searchValue"));
                break;
            }
            case OPEN_FROM_SEARCH_LABEL:{
                showSimpleDiary(intent.getStringExtra("label"));
                break;
            }
            case OPEN_FROM_LABEL_LIST:{
                searchView.setVisibility(View.VISIBLE);
                showAllLabel();
                break;
            }
            case OPEN_FROM_FULL_SEARCH:{
                List<DiaryVo> diaryVos = (List<DiaryVo>) intent.getSerializableExtra("diaryVos");
                diaryList = diaryVos;
                showFullSearchResult();
                break;
            }
            case OPEN_FROM_DELETE_LIST:{
//                showDeleteList();
                showDeleteList2();
                break;
            }
            case OPEN_FROM_ASYNC_SEARCH:{
                List<DiaryVo> diaryVos = (List<DiaryVo>) intent.getSerializableExtra("diaryVos");
                diaryList = diaryVos;
                showAsyncSearchResult();
                break;
            }
            default:break;
        }

    }

    /**
     * 处理批量删除的界面
     */
    private void showDeleteList() {
        //标题及提示
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("批量删除");
        tipView.setText("被选中日记的时间会被成蓝紫色，点击删除按钮预览确认被选中日记。");
        //处理信息展示
        SimpleResult result = diaryService.getSimpleDiaryByDate(BaseUtils.stringToDate("1024-06-16 00:00:00"),new Date());
        diaryList = (List<DiaryVo>) result.getData();
        DiaryAxisAdapterForDeleteList adapter = new DiaryAxisAdapterForDeleteList(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //响应条目点击，展示被点击条目及记录被点击日记的ID

    }

    /**
     * 处理批量删除的界面，异步加载，提供加载动画
     */
    private void showDeleteList2() {
        //标题及提示
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("批量删除");
        tipView.setText("被选中日记的时间会被成蓝紫色，点击删除按钮预览确认被选中日记。");
        //处理信息展示
        GetSimpleDiaryByDateTask task = new GetSimpleDiaryByDateTask(handler);
        task.getSimpleDiaryByDate("1024-06-16",BaseUtils.dateToString(new Date()).substring(0,10));


    }

    private void afterShowDeleteList2(){
        DiaryAxisAdapterForDeleteList adapter = new DiaryAxisAdapterForDeleteList(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示慢搜索的结果
     */
    private void showFullSearchResult() {
        if(diaryList==null || diaryList.isEmpty()){
            tipView.setText("搜索出错");
            return;
        }
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("慢速搜索结果");
        tipView.setText("搜索到的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示异步搜索的结果
     */
    private void showAsyncSearchResult() {
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("搜索结果");
        String searchValue = getIntent().getStringExtra("searchValue");
        tipView.setText("搜索到包含【"+searchValue+"】的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void bindViewAndSetListener(){
        recyclerView = findViewById(R.id.axis_recyclerview);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //根据打开类型判断是否需要展示信息流按钮
        switch (openType) {
            case OPEN_FROM_DELETE_LIST:{
                //只需要删除按钮，共用toolbar_day_detail
                getMenuInflater().inflate(R.menu.toolbar_day_detail,menu);
                break;
            }
            case OPEN_FROM_FULL_SEARCH:
            case OPEN_FROM_TIME_AXIS:
            case OPEN_FROM_SEARCH_DIARY:
            case OPEN_FROM_SEARCH_LABEL:{
                if(null!=diaryList && diaryList.size()>=2){
                    getMenuInflater().inflate(R.menu.toolbar_diary_lits,menu);
                    break;
                }
            }
            default:break;
        }
        return true;
    }

    /**
     * 展示从草稿箱来的数据
     */
    private void showSimpleDiary(){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("草稿箱");
        List<TempDiary> tempDiaries = LitePal.order("id desc").find(TempDiary.class);
        if(tempDiaries.size()>0){
            tipView.setText("草稿箱共有"+tempDiaries.size()+"条记录");
            List<DiaryVo> vos = new ArrayList<>();
            tempDiaries.forEach(tempDiary -> {
                DiaryVo vo = new DiaryVo();
                vo.setId(tempDiary.getId());
                String subDiary = tempDiary.getContent().length()>30?
                        (tempDiary.getContent().substring(0,30))+"...":tempDiary.getContent();
                vo.setContent(subDiary);
                vo.setModifiedDate("");
                vo.setPicSrcList(new ArrayList<>());
                vos.add(vo);
            });
            diaryList = vos;
            adapter = new DiaryAxisAdapter(diaryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }else{
            tipView.setText("草稿箱暂时还没有内容");
        }
    }

    /**
     * 展示从时间轴来的数据
     * @param dateOneStr
     * @param dateTwoStr
     */
    private void showSimpleDiary(String dateOneStr,String dateTwoStr){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("时间轴");;
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = dateFormat.parse(dateOneStr);
            date2 = dateFormat.parse(dateTwoStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //date1在前
        assert date2 != null;
        if(date2.before(date1)){
            Date temp = date1;
            date1 = date2;
            date2 = temp;
        }
        SimpleResult result = diaryService.getSimpleDiaryByDate(date1,date2);
        diaryList = (List<DiaryVo>) result.getData();
        tipView.setText("在"+dateOneStr+"到"+dateTwoStr+"的时间段内，共有"+diaryList.size()+"条日记");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 继续造屎山
     *
     *
     * 展示从时间轴来的数据，异步加载，提供动画
     * @param dateOneStr
     * @param dateTwoStr
     */
    private void showSimpleDiary2(String dateOneStr,String dateTwoStr){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("时间轴");
        GetSimpleDiaryByDateTask task = new GetSimpleDiaryByDateTask(handler);
        task.getSimpleDiaryByDate(dateOneStr,dateTwoStr);
        //这里加两个是为了显示右上角的信息流按钮。。。。
        diaryList = new ArrayList<>();
        diaryList.add(new DiaryVo());
        diaryList.add(new DiaryVo());
    }

    private void afterShowSimpleDiary2(String dateOneStr,String dateTwoStr){
        tipView.setText("在"+dateOneStr+"到"+dateTwoStr+"的时间段内，共有"+diaryList.size()+"条日记");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示从搜索结果来的数据
     * @param ids
     */
    private void showSimpleDiary(ArrayList<Integer> ids,String searchValue){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("搜索结果");
        diaryList = diaryService.getSimpleDiaryByIds(ids);
        tipView.setText("在标签中或未加密日记中，包含字符["+searchValue+"]的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示从标签选择来的数据
     * @param labelStr
     */
    private void showSimpleDiary(String labelStr){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle(labelStr);
        diaryList = diaryService.getSimpleDiaryByLabel(labelStr);
        tipView.setText("含有标签["+labelStr+"]的日记共有"+diaryList.size()+"条");
        adapter = new DiaryAxisAdapter(diaryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    /**
     * 展示所有标签
     */
    private void showAllLabel(){
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setTitle("标签集");
        Set<String> labels = diaryService.getAllLabels();
        if(labels.isEmpty()){
            tipView.setText("系统中还没有日记加过标签");
        }else{
            tipView.setText("在所有日记中，共统计到["+labels.size()+"]个标签。\n搜索只支持输入小写字母。");
            List<DiaryVo> vos = new ArrayList<>();
            labels.forEach(label -> {
                DiaryVo vo = new DiaryVo();
                vo.setId(-1);
                vo.setContent(label);
                vo.setModifiedDate("");
                vo.setPicSrcList(new ArrayList<>());
                vos.add(vo);
            });
            diaryList = vos;
            adapter = new DiaryAxisAdapter(diaryList);
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

    private void showProgressAlertDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setCancelable(false);
        LinearLayout linearLayout = new LinearLayout(DiaryListActivity.this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        ImageView imageView = new ImageView(DiaryListActivity.this);
        imageView.setImageResource(R.drawable.loading);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500,500);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof AnimatedImageDrawable){
            AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) drawable;
            animatedImageDrawable.start();
        }
        linearLayout.addView(imageView);
        builder.setView(linearLayout);
        ProgressADL = builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                beSelectedToDelID.clear();
                break;
            }
            case R.id.toolbar_diary_list_flow:{
                //由于在时间轴界面默认显示信息流按钮，所以在这里加一个判断，如果列表的日记数量小于2，则按钮不响应
                if(diaryList.size()<2){
                    BaseUtils.shortTipInSnack(recyclerView,"日记数量小于2，不允许跳转信息流");
                    break;
                }
                AtomicReference<String> select = new AtomicReference<>();
                final String[] items = {"倒序","顺序"};
                select.set(items[0]);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("改用信息流展示，请先选则时间排序类型");
                builder.setSingleChoiceItems(items, 0, (dialogInterface, i) -> {
                    select.set(items[i]);
                });
                builder.setPositiveButton("确定", (dialog, which) -> {
                    ArrayList<Integer> ids = new ArrayList<>();
                    diaryList.forEach(diaryVo -> ids.add(diaryVo.getId()));
                    Intent intent = new Intent(this, TimeAscActivity.class);
                    intent.putExtra(TimeAscActivity.OPEN_FROM_TYPE,TimeAscActivity.OPEN_FROM_SIMPLE_DIARY_LIST);
                    intent.putIntegerArrayListExtra("ids",ids);
                    switch (select.get()) {
                        case "倒序":{
                            intent.putExtra("sortType",1);
                            break;
                        }
                        case "顺序":{
                            intent.putExtra("sortType",2);
                            break;
                        }
                        default:break;
                    }
                    startActivity(intent);
                });
                builder.setNegativeButton("取消",null);
                builder.setCancelable(false);
                builder.show();
                break;
            }
            case R.id.toolbar_day_sel:{
                if(beSelectedToDelID.size()>0){
                    Intent intent = new Intent(DiaryListActivity.this, TimeAscActivity.class);
                    intent.putExtra(TimeAscActivity.OPEN_FROM_TYPE,TimeAscActivity.OPEN_FROM_DELETE_LIST);
                    intent.putIntegerArrayListExtra("delIds",(ArrayList<Integer>) beSelectedToDelID);
                    DiaryListActivity.this.startActivity(intent);
                }else{
                    BaseUtils.shortTipInSnack(recyclerView,"没有被选中的项目！");
                }
                break;
            }
            default:break;
        }
        return true;
    }

    /**
     * 需要更换点击事件，所以换了一个Adapter
     */
    private static class DiaryAxisAdapterForDeleteList extends RecyclerView.Adapter{
        private List<DiaryVo> diaryVoList;
        private Context context;

        static class ViewHolder extends RecyclerView.ViewHolder{
            View view;
            TextView dateView;
            TextView diaryCutView;
            ImageView imageView;
            int diaryId;
            boolean beSelected =false;
            String time;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;
                dateView = itemView.findViewById(R.id.axis_item_date);
                diaryCutView = itemView.findViewById(R.id.axis_item_diary_cut);
                imageView = itemView.findViewById(R.id.axis_item_image);
            }
        }

        public DiaryAxisAdapterForDeleteList(List<DiaryVo> diaryVoList) {
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
            final DiaryAxisAdapterForDeleteList.ViewHolder viewHolder = new DiaryAxisAdapterForDeleteList.ViewHolder(view);
            viewHolder.view.setOnClickListener(v->{
                if(!viewHolder.beSelected){
                    //改为被选中
                    viewHolder.beSelected = true;
                    viewHolder.dateView.setText("被选中");
                    viewHolder.dateView.setBackgroundResource(R.drawable.axis_time4);
                    beSelectedToDelID.add(viewHolder.diaryId);
                }else{
                    //改为不选中
                    viewHolder.beSelected = false;
                    viewHolder.dateView.setText(viewHolder.time);
                    viewHolder.dateView.setBackgroundResource(R.drawable.axis_time2);
                    beSelectedToDelID.remove((Integer) viewHolder.diaryId);
                }
                BaseUtils.shortTipInSnack(view,"已被选中个数："+beSelectedToDelID.size());
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            DiaryAxisAdapterForDeleteList.ViewHolder newHolder = (DiaryAxisAdapterForDeleteList.ViewHolder) holder;
            DiaryVo diaryVo = diaryVoList.get(position);
            newHolder.diaryId = diaryVo.getId();
            newHolder.time = diaryVo.getModifiedDate();
            newHolder.diaryCutView.setText(diaryVo.getContent());
            if(beSelectedToDelID.contains(newHolder.diaryId)){
                newHolder.beSelected = true;
                newHolder.dateView.setText("被选中");
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time4);
            }else{
                //改为不选中
                newHolder.dateView.setText(newHolder.time);
                newHolder.dateView.setBackgroundResource(R.drawable.axis_time2);
                newHolder.beSelected = false;
            }
            if (diaryVo.getPicSrcList().size()>0){
//                Glide.with(context)
//                        .load(diaryVo.getPicSrcList().get(0))
//                        .into(newHolder.imageView);
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.load_image)//图片加载出来前，显示的图片
                        .fallback( R.drawable.bad_image) //url为空的时候,显示的图片
                        .error(R.drawable.bad_image);//图片加载失败后，显示的图片
                Glide.with(context).load(diaryVo.getPicSrcList().get(0)).apply(options).into(newHolder.imageView);
            }else{
                newHolder.imageView.setImageResource(R.drawable.transparent);
            }
        }

        @Override
        public int getItemCount() {
            return diaryVoList.size();
        }
    }
}