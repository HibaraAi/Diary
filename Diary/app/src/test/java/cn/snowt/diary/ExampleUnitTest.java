package cn.snowt.diary;

import org.junit.Test;

import static org.junit.Assert.*;

import cn.snowt.diary.entity.Diary;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test01(){
        Diary diary = new Diary();
        diary.setContent("dasdasdas");
        System.out.println(diary);
    }
}