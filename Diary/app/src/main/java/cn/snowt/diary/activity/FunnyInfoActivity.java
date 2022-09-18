package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.entity.Diary;
import cn.snowt.diary.entity.FunnyInfo;
import cn.snowt.diary.service.DiaryService;
import cn.snowt.diary.service.FunnyInfoService;
import cn.snowt.diary.service.impl.DiaryServiceImpl;
import cn.snowt.diary.service.impl.FunnyInfoServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-10-12, 0012 14:22:53
 * @Description: 展示有趣的统计数据的Activity
 */
public class FunnyInfoActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private TextView tip;
    private ListView listView;

    private FunnyInfo info;
    private FunnyInfoService funnyInfoService = new FunnyInfoServiceImpl();
    private DiaryService diaryService = new DiaryServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funny_info);
        bindViewAndSetListener();
        try {
            showFunnyInfo();
        } catch (Exception e){
            SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
            edit.remove(FunnyInfoServiceImpl.LAST_FUNNY_INFO_IN_SP_NAME);
            edit.remove(FunnyInfoServiceImpl.FUNNY_INFO_IN_SP_NAME);
            edit.apply();
        }
    }

    private void showFunnyInfo() {
        info = funnyInfoService.getFunnyInfoInSp();
        if(null==info){
            tip.setText("提示: 你还没进行过数据分析,点击右上角的刷新按钮即可分析数据");
            return;
        }
        String string = BaseUtils.getSharedPreference().getString(FunnyInfoServiceImpl.LAST_FUNNY_INFO_IN_SP_NAME, "程序异常");
        tip.setText("提示: 此次展示的数据刷新于"+string);
        List<String> list = new ArrayList<>();
        list.add("本机的软件安装日期:"+BaseUtils.dateToString(info.getInstallDate()));
        Diary diary = LitePal.find(Diary.class, info.getFirstDiaryId());
        list.add("在["+BaseUtils.dateToString(diary.getModifiedDate())+"]你在消消乐记下了第一个日记");
        list.add(info.getLongestDiaryContentLength()+"是目前为止你记下最长篇日记的字数。ID为"+info.getLongestDiaryId()+"(将来再支持日记展示和跳转)");
        list.add(info.getShortestDiaryContentLength()+"是目前为止你记下最短篇日记的字数。ID为"+info.getShortestDiaryId());
        String s;
        if(null!=info.getMaximumNumOfWords()){
            if(info.getMaximumNumOfWords()==0){
                s = "100以内";
            }else{
                s = info.getMaximumNumOfWords()+"00-"+info.getMaximumNumOfWords()+"99";
            }
            list.add("你的单篇日记字数大多都在"+s+"，目前为止共有"+info.getWordSegmentNum()+"篇");
        }
        list.add("你已经在系统中记下了"+info.getTotalLabelNum()+"个标签，其中最常用的标签是"+info.getMostAppearLabel()+"。但还有"+info.getNoLabelDiaryNum()+"条日记是没有标签的。");
        list.add("目前为止,你已经记录了"+info.getTotalDiaryNum()+"条日记。");
        list.add("在"+info.getMonthWithTheMostDiary()+"这个月，你记录了最多日记，高达"+ info.getMonthWithTheMostDiaryCount()+"条");
        list.add("在"+BaseUtils.dateToString(info.getDayWithTheMostDiary())+"这天，你一口气记下"+info.getNumOfDayWithTheMostDiary()+"条日记");
        list.add("你最喜欢在"+info.getMostFrequentTimeToKeepDiary()+"点写日记");
        final String[] yearAbout = {"你在\n"};
        info.getNumOfDairyPerYear().forEach((year,count)->{
            yearAbout[0] += year+"年里共写了"+count+"篇日记\n";
        });
        list.add(yearAbout[0]);
        list.add("追更过"+info.getCommentSum()+"次日记");
        list.add("有"+info.getHaveCommentDiarySum()+"条日记是被追更过的");
        list.add("追更过次数最多的日记是:ID="+info.getMostCommentedDiaryId()+",有"+info.getMostCommentedDiaryCount()+"条评论，看来那天是满满的回忆吧");
        list.add("目前为止，你一共存储了"+info.getImageSum()+"张日记配图");
        list.add("目前为止，你一共存储了"+info.getVideoSum()+"个视频");
        list.add("你所有日记里，记录了最多的天气是"+info.getMostWeatherInDiary());
        list.add("消消乐一共陪你度过"+info.getRainSumInDiary()+"个下雨天");
        list.add("你一共记录过"+info.getSpecialDaySum()+"个特殊日期");
        list.add("目前为止你所有日记加评论，字数已经达到了"+info.getNumOfTotalWords()+"个");
        list.add("扫雷最快通关秒数是"+info.getFastestGame());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            BaseUtils.shortTipInSnack(listView,"别点了，这么丑的界面，不想做功能 ORz");
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            BaseUtils.shortTipInSnack(listView,"也别长按，屏幕按爆了也没反应 ORz");
            return true;
        });
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("数据分析");
        }
        tip = findViewById(R.id.ac_funny_tip);
        listView = findViewById(R.id.ac_funny_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_funny_info,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            case R.id.toolbar_funny_info_help:{
                String tip = "原本呢，是想做个像华为运动健康那样的周报的，但想了一下，不想整的这么麻烦，还每周" +
                        "刷新一次。于是就做了现在这个使用统计，查询所有已存储的数据，展示一些比较有代表性的数据，" +
                        "比如累计写下了多少字、最长的一篇日记是哪个这样的。具体都展示了哪些就不赘述了，你自己" +
                        "刷新一次就可以看了。\n" +
                        "但是这个分析很慢(取决于你的加密日记数量和日记字数的多少)，因为还是只注重功能实现，" +
                        "不考虑性能优化。也不给予分析进度。所以还请慢慢的等。\n" +
                        "而且，目前的功能还不完善，界面丑，描述不有趣。比如我直接就展示了你累计写下了多少字，" +
                        "并没有多展示一个说法——“相当于写了多少本新华字典”这样的，嗯，我现在懒得搞，弄烦了，" +
                        "等我下次有兴趣再打开这个项目的时候一定加。";
                BaseUtils.alertDialogToShow(this,"数据分析说明",tip);
                break;
            }
            case R.id.toolbar_funny_info_refresh:{
                List<DiaryVo> diaryVoList = diaryService.getDiaryVoList(0, 1);
                if(diaryVoList.isEmpty()){
                    BaseUtils.alertDialogToShow(this,"提示","你都没有记录过日记，分析啥呢???");
                }else{
                    new Thread(() -> {
                        funnyInfoService.refreshFunnyInfo();
                        BaseUtils.simpleSysNotice(this,"数据分析已经完成了");
                    }).start();
                    BaseUtils.alertDialogToShow(this,"提示","后台已经开始分析了，在分析完成之前不要再次发起分析请求，也不要关闭本程序(可以浏览其他界面或是最小化到后台)，分析应该会在几分钟内完成，快的话几秒钟。");
                }
               break;
            }
            default:break;
        }
        return true;
    }
}