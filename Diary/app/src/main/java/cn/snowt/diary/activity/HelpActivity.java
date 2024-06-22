package cn.snowt.diary.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.snowt.diary.R;
import cn.snowt.diary.adapter.HelpAdapter;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.Constant;
import cn.snowt.diary.vo.DiaryVo;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 22:58
 * @Description: 帮助和关于
 */
public class HelpActivity extends AppCompatActivity {
    public static final String OPEN_TYPE = "openType";  //此界面展示为何类型
    public static final Integer OPEN_TYPE_EGG = 1;  //彩蛋界面
    public static final Integer OPEN_TYPE_HELP = 2;  //帮助界面
    public static final Integer OPEN_TYPE_UPDATE= 3;  //更新信息界面

    private ActionBar actionBar;
    private RecyclerView recyclerView;
    private HelpAdapter helpAdapter;

    List<DiaryVo> diaryVos = new ArrayList<DiaryVo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        bindViewAndSetListener();
        Intent intent = getIntent();
        int openType = intent.getIntExtra(OPEN_TYPE, -1);
        switch (openType) {
            case 1:{ //public static final Integer OPEN_TYPE_EGG = 1;  //彩蛋界面
                showEasterEgg();
                break;
            }
            case 2:{  //public static final Integer OPEN_TYPE_HELP = 2;  //帮助界面
                showHelp();
                break;
            }
            case 3:{  //public static final Integer OPEN_TYPE_UPDATE= 3;  //更新信息界面
                showUpdateInfo();
                break;
            }
            default:{
                BaseUtils.alertDialogToShow(this,"标题","尚未定义的界面");
                break;
            }
        }
    }

    /**
     * 加载更新信息
     */
    private void showUpdateInfo() {
        actionBar.setTitle("历次更新信息");

        DiaryVo diaryVo = new DiaryVo();
        diaryVo.setId(R.drawable.head_hibara);
        diaryVo.setMyUuid("HibaraAi");
        diaryVo.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo.setLocationStr("我已经在尽力完善APP了");
        diaryVo.setLabelStr("#本次更新内容#");
        diaryVo.setContent(Constant.STRING_UPDATE_0);
        diaryVos.add(diaryVo);

        DiaryVo diaryVo1 = new DiaryVo();
        diaryVo1.setId(R.drawable.head_hibara);
        diaryVo1.setMyUuid("HibaraAi");
        diaryVo1.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo1.setLocationStr("我已经在尽力完善APP了");
        diaryVo1.setLabelStr("#1.0版本#");
        diaryVo1.setContent(Constant.STRING_UPDATE_1);
        diaryVos.add(diaryVo1);

        DiaryVo diaryVo2 = new DiaryVo();
        diaryVo2.setId(R.drawable.head_hibara);
        diaryVo2.setMyUuid("HibaraAi");
        diaryVo2.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo2.setLocationStr("我已经在尽力完善APP了");
        diaryVo2.setLabelStr("#1.1版本#");
        diaryVo2.setContent(Constant.STRING_UPDATE_2);
        diaryVos.add(diaryVo2);

        DiaryVo diaryVo3 = new DiaryVo();
        diaryVo3.setId(R.drawable.head_hibara);
        diaryVo3.setMyUuid("HibaraAi");
        diaryVo3.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo3.setLocationStr("我已经在尽力完善APP了");
        diaryVo3.setLabelStr("#1.2版本#");
        diaryVo3.setContent(Constant.STRING_UPDATE_3);
        diaryVos.add(diaryVo3);

        DiaryVo diaryVo4 = new DiaryVo();
        diaryVo4.setId(R.drawable.head_hibara);
        diaryVo4.setMyUuid("HibaraAi");
        diaryVo4.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo4.setLocationStr("我已经在尽力完善APP了");
        diaryVo4.setLabelStr("#1.2.3版本#");
        diaryVo4.setContent(Constant.STRING_UPDATE_4);
        diaryVos.add(diaryVo4);

        DiaryVo diaryVo5 = new DiaryVo();
        diaryVo5.setId(R.drawable.head_hibara);
        diaryVo5.setMyUuid("HibaraAi");
        diaryVo5.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo5.setLocationStr("我已经在尽力完善APP了");
        diaryVo5.setLabelStr("#1.2.4版本#");
        diaryVo5.setContent(Constant.STRING_UPDATE_5);
        diaryVos.add(diaryVo5);

        DiaryVo diaryVo6 = new DiaryVo();
        diaryVo6.setId(R.drawable.head_hibara);
        diaryVo6.setMyUuid("HibaraAi");
        diaryVo6.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo6.setLocationStr("我已经在尽力完善APP了");
        diaryVo6.setLabelStr("#1.2.5版本#");
        diaryVo6.setContent(Constant.STRING_UPDATE_6);
        diaryVos.add(diaryVo6);

        DiaryVo diaryVo7 = new DiaryVo();
        diaryVo7.setId(R.drawable.head_hibara);
        diaryVo7.setMyUuid("HibaraAi");
        diaryVo7.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo7.setLocationStr("我已经在尽力完善APP了");
        diaryVo7.setLabelStr("#1.2.5.2版本#");
        diaryVo7.setContent(Constant.STRING_UPDATE_7);
        diaryVos.add(diaryVo7);

        DiaryVo diaryVo8 = new DiaryVo();
        diaryVo8.setId(R.drawable.head_hibara);
        diaryVo8.setMyUuid("HibaraAi");
        diaryVo8.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo8.setLocationStr("我已经在尽力完善APP了");
        diaryVo8.setLabelStr("#1.2.6版本#");
        diaryVo8.setContent(Constant.STRING_UPDATE_8);
        diaryVos.add(diaryVo8);

        DiaryVo diaryVo9 = new DiaryVo();
        diaryVo9.setId(R.drawable.head_hibara);
        diaryVo9.setMyUuid("HibaraAi");
        diaryVo9.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo9.setLocationStr("我已经在尽力完善APP了");
        diaryVo9.setLabelStr("#1.2.6.2版本#");
        diaryVo9.setContent(Constant.STRING_UPDATE_9);
        diaryVos.add(diaryVo9);

        DiaryVo diaryVo10 = new DiaryVo();
        diaryVo10.setId(R.drawable.head_hibara);
        diaryVo10.setMyUuid("HibaraAi");
        diaryVo10.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo10.setLocationStr("我已经在尽力完善APP了");
        diaryVo10.setLabelStr("#1.3.0版本#");
        diaryVo10.setContent(Constant.STRING_UPDATE_10);
        diaryVos.add(diaryVo10);

        DiaryVo diaryVo11 = new DiaryVo();
        diaryVo11.setId(R.drawable.head_hibara);
        diaryVo11.setMyUuid("HibaraAi");
        diaryVo11.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo11.setLocationStr("我已经在尽力完善APP了");
        diaryVo11.setLabelStr("#1.3.1版本#");
        diaryVo11.setContent(Constant.STRING_UPDATE_11);
        diaryVos.add(diaryVo11);

        DiaryVo diaryVo12 = new DiaryVo();
        diaryVo12.setId(R.drawable.head_hibara);
        diaryVo12.setMyUuid("HibaraAi");
        diaryVo12.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo12.setLocationStr("我已经在尽力完善APP了");
        diaryVo12.setLabelStr("#1.3.2版本#");
        diaryVo12.setContent(Constant.STRING_UPDATE_12);
        diaryVos.add(diaryVo12);

        DiaryVo diaryVo13 = new DiaryVo();
        diaryVo13.setId(R.drawable.head_hibara);
        diaryVo13.setMyUuid("HibaraAi");
        diaryVo13.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo13.setLocationStr("我已经在尽力完善APP了");
        diaryVo13.setLabelStr("#1.3.3版本#");
        diaryVo13.setContent(Constant.STRING_UPDATE_13);
        diaryVos.add(diaryVo13);

        DiaryVo diaryVo14 = new DiaryVo();
        diaryVo14.setId(R.drawable.head_hibara);
        diaryVo14.setMyUuid("HibaraAi");
        diaryVo14.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo14.setLocationStr("我已经在尽力完善APP了");
        diaryVo14.setLabelStr("#1.4.0版本#");
        diaryVo14.setContent(Constant.STRING_UPDATE_14);
        diaryVos.add(diaryVo14);

        helpAdapter = new HelpAdapter(diaryVos);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(helpAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 加载帮助信息
     */
    private void showHelp() {
        actionBar.setTitle("帮助&关于");
        
        DiaryVo diaryVo = new DiaryVo();
        diaryVo.setId(R.drawable.head_hibara);
        diaryVo.setMyUuid("帮助");
        diaryVo.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo.setLocationStr("这里要有字才好看");
        diaryVo.setLabelStr("#功能介绍#");
        diaryVo.setContent(Constant.STRING_HELP_1);
        diaryVos.add(diaryVo);
        
        DiaryVo diaryVo2 = new DiaryVo();
        diaryVo2.setId(R.drawable.head_hibara);
        diaryVo2.setMyUuid("帮助");
        diaryVo2.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo2.setLocationStr("这里要有字才好看");
        diaryVo2.setLabelStr("#安全相关#");
        diaryVo2.setContent(Constant.STRING_HELP_2);
        diaryVos.add(diaryVo2);
        
        DiaryVo diaryVo3 = new DiaryVo();
        diaryVo3.setId(R.drawable.head_hibara);
        diaryVo3.setMyUuid("帮助");
        diaryVo3.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo3.setLocationStr("这里要有字才好看");
        diaryVo3.setLabelStr("#权限使用说明#");
        diaryVo3.setContent(Constant.STRING_HELP_3);
        diaryVos.add(diaryVo3);
        
        DiaryVo diaryVo4 = new DiaryVo();
        diaryVo4.setId(R.drawable.head_hibara);
        diaryVo4.setMyUuid("帮助");
        diaryVo4.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo4.setLocationStr("这里要有字才好看");
        diaryVo4.setLabelStr("#登录有关#");
        diaryVo4.setContent(Constant.STRING_HELP_4);
        diaryVos.add(diaryVo4);
        
        DiaryVo diaryVo5 = new DiaryVo();
        diaryVo5.setId(R.drawable.head_hibara);
        diaryVo5.setMyUuid("帮助");
        diaryVo5.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo5.setLocationStr("这里要有字才好看");
        diaryVo5.setLabelStr("#设置项说明#");
        diaryVo5.setContent(Constant.STRING_HELP_5);
        diaryVos.add(diaryVo5);
        
        DiaryVo diaryVo6 = new DiaryVo();
        diaryVo6.setId(R.drawable.head_hibara);
        diaryVo6.setMyUuid("帮助");
        diaryVo6.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo6.setLocationStr("这里要有字才好看");
        diaryVo6.setLabelStr("#Q&A#");
        diaryVo6.setContent(Constant.STRING_HELP_6);
        diaryVos.add(diaryVo6);
        
        DiaryVo diaryVo7 = new DiaryVo();
        diaryVo7.setId(R.drawable.head_hibara);
        diaryVo7.setMyUuid("帮助");
        diaryVo7.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVo7.setLocationStr("这里要有字才好看");
        diaryVo7.setLabelStr("#写在最后#");
        diaryVo7.setContent(Constant.STRING_HELP_7);
        diaryVos.add(diaryVo7);

        DiaryVo diaryVoAb = new DiaryVo();
        diaryVoAb.setId(R.drawable.head_hibara);
        diaryVoAb.setMyUuid("关于");
        diaryVoAb.setModifiedDate(BaseUtils.dateToString(new Date()));
        diaryVoAb.setLocationStr("这里要有字才好看");
        diaryVoAb.setLabelStr("#关于#");
        diaryVoAb.setQuoteDiaryStr("复制地址");
        diaryVoAb.setContent(Constant.STRING_ABOUT);
        diaryVos.add(diaryVoAb);
        
        helpAdapter = new HelpAdapter(diaryVos);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(helpAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void bindViewAndSetListener() {
        setSupportActionBar(findViewById(R.id.default_toolbar_inc));
        actionBar = getSupportActionBar();
        if(null!=actionBar){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = findViewById(R.id.help_recyclerview);
    }

    /**
     * 加载彩蛋信息
     */
    private void showEasterEgg() {
        actionBar.setTitle("彩蛋!");
        DiaryVo diaryVo = new DiaryVo();
        diaryVo.setId(R.drawable.head_hibara);
        diaryVo.setMyUuid("HibaraAi");
        diaryVo.setModifiedDate("2024-06-22 10:17");
        diaryVo.setWeatherStr("晴空万里");
        diaryVo.setLocationStr("广东省河源市");
        diaryVo.setLabelStr("#居然被你发现了彩蛋#");
        String egg = "在1.5.0版本中，本想要删除扫雷游戏这个功能的，因为觉得这个游戏与软件主体不搭。测试时只隐藏了功能入口，准备在下一版彻底删除的。\n" +
                "但突发奇想，要不做个彩蛋吧，留在程序里也不错。居然被你找到了！\n\n" +
                "居然这么有缘，那就多分享一下关于这个APP的东西。\n" +
                "为什么做这个APP？\n" +
                "一是个人觉得互联网是不安全的，天天发私密的内容、照片到微博、朋友圈是危险的，说不定就被爬虫爬去了，爬去干嘛也不知道，况且" +
                "现在AI这么发达，恶意用途更多。理论上来说，根据公开内容“人肉”一个人不是什么难事，安全起见，我从来不在互联网上发布自己的照片。\n" +
                "二是在我寻找互联网上的日记类软件时，发现要么功能达不到我的要求，要么就是广告一堆，要么就是要钱。。。。我是喜欢白嫖的人，打钱是不可能的了。" +
                "曾经有一段时间用微信朋友圈只发仅自己可见的内容，但发现，好像微信还是会分析你，然后给你推送广告，并且有个致命缺点，微信不支持导出！" +
                "我们的数据只是微信的垄断筹码而已，试想，要不是那么多人使用微信，你会非微信不用不可吗？而且你有没发现，几乎所有互联网社交类产品，竟无一例外的不支持导出数据。" +
                "综上两大点，我决定自己做一个，反正我有计算机基础，而且这个APP得需求也不是很难，稍微学一下应该就能完成的，最重要的是整个APP捏在自己手里，想怎么来就怎么来。" +
                "\n\nAPP的开发历程。" +
                "确实一开始认为开发一个APP很简单的，起初我的设想也很简单，只要能记一些文字，加几张配图就行。第一版也确实很快完成了基础功能，但....确实很基础。" +
                "几乎像个残缺品。没有进度条展示，这个截至更到1.5.0了也都还没有解决，实在是太懒了，不想学。随着我自己用的越来越久，我的想法也逐渐增多，" +
                "很多东西都是刚开始设计时没考虑过的，例如标签这个玩意，一开始我就想显示一下就完事了，但是后来觉得，加个按标签寻找的功能，后来又发现好多标签" +
                "重复的，又加了个同名标签功能，再后来，甚至觉得标签应该有一级标签二级标签这样的，当然这个没做啦，因为按照标签目前的设计，改起来太复杂了。" +
                "又比如数据分析这个功能，我记得好早好早就加入了这个功能，但已经鸽了几年了都没完善哈哈哈哈哈哈哈哈哈哈。以后有机会一定完善。最主要的是，我并不是学设计的，" +
                "界面UI全都是用安卓自带的组件，界面肯定跟不上时代了，配色、组件比例啥的我也是随便来的，差不多可以就差不多的样子。" +
                "\n\n虚荣。" +
                "截至目前，除了没有进度条展示，作为本地日记App，我认为已经做得很强大了，目前市面上没有哪家的功能做得有我好，但目前的用户量应该不怎么高，" +
                "因为我的演示视频目前为止也才3000多播放量，收到的离线点赞也不多。没有做推广，如果你觉得软件不错，记得帮我安利一下。" +
                "\n\n分享一下本APP的其他内容。" +
                "消消乐这个名字只是单纯的单纯的为了和我别的软件一下是ABB式的游戏命名。\n" +
                "默认头像及背景图是谭松韵，刚开始开发APP时在看她的剧，觉得可爱就用了她，曾经想换过，但既然是缘分，就没换了。\n" +
                "HibaraAi是灰原哀的音译英文名。作为开发者时寄语，如帮助界面、更新信息界面的动漫头像也是灰原哀，懂了吧，我是哀迷。\n" +
                "今天是2024年6月22日，透露一下鸿蒙NEXT的适配想法，其实我很早有学鸿蒙开发了，但它的ArkUI并不好用，也没有很多参考代码可以搜索到，" +
                "而且你应该也发现了，我基本只用系统提供的组件，而鸿蒙提供的组件很少、且还有很多bug。例如安卓版的侧滑菜单栏，鸿蒙不提供这个，要我自己去学自己去写代码去实现" +
                "，这是不可能的。工作量太大了，我还要上班的。再比如鸿蒙左上角的返回按钮，居然不能完全隐藏，虽然不显示，但它还是占那个位置，再比如他的Coast提示居然会" +
                "打断返回操作。总之就是各种不完善，大概学了两个月的周末，放弃了，不如来研究安卓的PDF导出。而且也不知道鸿蒙到时能不能像安卓一样apk文件安装，要是只能上架" +
                "应用商店安装的话，我大概率是不会上架的，注册开发者还要手持身份证拍照给华为，呵呵。所以目前还在观望、考虑要不要适配鸿蒙。" +
                "";
        diaryVo.setContent(egg);
        diaryVo.setQuoteDiaryStr("去玩扫雷");
        diaryVos.add(diaryVo);
        helpAdapter = new HelpAdapter(diaryVos);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(helpAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                finish();
                break;
            }
            default:break;
        }
        return true;
    }
}