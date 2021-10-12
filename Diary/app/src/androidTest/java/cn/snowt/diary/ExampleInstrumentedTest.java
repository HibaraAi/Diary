package cn.snowt.diary;

import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import cn.snowt.diary.service.LabelService;
import cn.snowt.diary.service.impl.LabelServiceImpl;
import cn.snowt.diary.util.BaseUtils;
import cn.snowt.diary.util.SimpleResult;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void test01(){
        SharedPreferences.Editor edit = BaseUtils.getSharedPreference().edit();
//        edit.remove("sameLabel");
//        edit.apply();
        LabelService labelService = new LabelServiceImpl();
        Map beforeAdd = labelService.getAllSameLabel();
        SimpleResult result = labelService.addSameLabel("#谭松韵##两小姐#");
       // SimpleResult result = labelService.updateSameLabel(1,"#撒贝宁##小撒##狗头侦探#");
        Map afterAdd = labelService.getAllSameLabel();
        List<String> sameLabels = labelService.getSameLabelsByOne("#AA#");
        System.out.println();
    }
}