package cn.snowt.diary.async;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import cn.snowt.diary.util.SimpleResult;

/**
 * @Author: HibaraAi
 * @Date: 2024-07-04 19:07
 * @Description: 异步任务。
 * 用于展示进度条。旧的没有进度条的方法不改，做一个新的类，专门处理异步任务。
 * 需要提供一个Handler。
 * 子类需要重写doAsync()方法，将耗时操作放入重写的方法，需要在重写的方法里调用updateProgress()通知主线程更新进度。
 * 调用startAsyncTask()方法即可启动异步任务。
 * handler中message的obj包含异步执行的结果--->SimpleResult result = (SimpleResult) msg.obj。
 * msg.what = FINISH_TASK表示完成了异步任务;
 * msg.what = START_TASK表示刚开启了异步任务,这个时候可以弹出进度条;
 * msg.what = UPDATE_PROGRESS可以通知主线程更新进度条的进度
 */
public abstract class MyAsyncTask {
    public static final int FINISH_TASK = 1;
    public static final int START_TASK = 2;
    public static final int UPDATE_PROGRESS = 3;


    private final Handler handler;
    protected SimpleResult result;

    public MyAsyncTask(Handler handler) {
        this.handler = handler;
    }

    /**
     * 启动异步任务
     */
    final void startAsyncTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //doAsync
                showProgress();
                doAsync();
                finishTask();
            }
        }).start();
    }

    /**
     * 子类实现具体的异步任务
     */
    abstract void doAsync();

    /**
     * 发送整个任务已完成的Message。
     */
    private void finishTask() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message message = new Message();
        message.what = FINISH_TASK;
        message.obj = result;
        handler.sendMessage(message);
    }

    /**
     * 发送展示进度条的Message。
     */
    private void showProgress(){
        Message message = new Message();
        message.what = START_TASK;
        handler.sendMessage(message);
    }

    /**
     * 发送更新进度条的Message。
     * 进度条使用百分比，需传入当前的进度整数
     * @param integer 当前进度
     */
    final void updateProgress(Integer integer) {
        Message message = new Message();
        message.what = UPDATE_PROGRESS;
        message.arg1 = integer;
        handler.sendMessage(message);
    }
}
