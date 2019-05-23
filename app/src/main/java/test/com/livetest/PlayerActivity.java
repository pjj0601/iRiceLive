package test.com.livetest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.anenn.flowlikeviewlib.FlowLikeView;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.VideoView;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

/**
 * Created by Sikang on 2017/5/2.
 */

public class PlayerActivity extends Activity implements View.OnClickListener,IGetMessageCallBack{

    private MyServiceConnection serviceConnection;
    private MQTTService mqttService;

    private String path = "";
    private VideoView mVideoView;
    private FlowLikeView likeViewLayout;

//    private EditText mEditText;
//    private Button mStartBtn;
//    private Button mStopBtn;

    private DanmakuView mDanmakuView;
    private boolean showDanmaku;
    private DanmakuContext danmakuContext;

    /**
     * 弹幕解析器
     */
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };
    private LinearLayout mOperationLayout;
    private Button mSend;
    private EditText mText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        serviceConnection = new MyServiceConnection();
        serviceConnection.setIGetMessageCallBack(PlayerActivity.this);

        Intent intent = new Intent(this, MQTTService.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        if (!LibsChecker.checkVitamioLibs(this))
            return;
//        mEditText = (EditText) findViewById(R.id.url);

//        mStartBtn = (Button) findViewById(R.id.start);
//        mStopBtn = (Button) findViewById(R.id.stop);
//        mStartBtn.setOnClickListener(this);
//        mStopBtn.setOnClickListener(this);

        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVideoView.setVideoPath("rtmp://119.23.111.7:1935/hls/test");

        initView_like();
        initView();
        initDanmaku();
    }






    /**
     * 初始化View组件
     */
    private void initView() {
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        mOperationLayout = (LinearLayout) findViewById(R.id.operation_layout);
        mSend = (Button) findViewById(R.id.send);
        mText = (EditText) findViewById(R.id.edit_text);
        //给弹幕层设置点击事件，判断是否显示发弹幕操作层
        mDanmakuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperationLayout.getVisibility()==View.GONE){
                    mOperationLayout.setVisibility(View.VISIBLE);
                }else {
                    mOperationLayout.setVisibility(View.GONE);
                }
            }
        });
        //给发送按钮设置点击事件，发送弹幕
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mText.getText().toString();
                if (!TextUtils.isEmpty(data)){
                    addDanmaku(data,true);
                    MQTTService.publish("测试一下子");
                    mText.setText("");

                }
            }
        });

        //监听由于输入法弹出所致的沉浸问题
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });
    }



    /**
     * 初始化弹幕组件
     */
    private void initDanmaku() {
        //给弹幕视图设置回调，在准备阶段获取弹幕信息并开始
        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku = true;
                mDanmakuView.start();
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        //缓存，提升绘制效率
        mDanmakuView.enableDanmakuDrawingCache(true);
        //DanmakuContext主要用于弹幕样式的设置
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN,3);//描边
        danmakuContext.setDuplicateMergingEnabled(true);//重复合并
        danmakuContext.setScrollSpeedFactor(1.2f);//弹幕滚动速度
        //让弹幕进入准备状态，传入弹幕解析器和样式设置
        mDanmakuView.prepare(parser,danmakuContext);
        //显示fps、时间等调试信息
        mDanmakuView.showFPS(true);
    }

    /**
     * 随机生成一些弹幕内容以供测试
     */
    private void generateSomeDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (showDanmaku) {
                    int time = new Random().nextInt(300);
                    String content = "" + time + time;
                    addDanmaku(content, false);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * 向弹幕View中添加一条弹幕
     *
     * @param content    弹幕的具体内容
     * @param withBorder 弹幕是否有边框
     */
    private void addDanmaku(String content, boolean withBorder) {
        //弹幕实例BaseDanmaku,传入参数是弹幕方向
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textSize = sp2px(20);
        danmaku.textColor = Color.WHITE;
        danmaku.setTime(mDanmakuView.getCurrentTime());
        //加边框
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        mDanmakuView.addDanmaku(danmaku);
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    public void setMessage(String message) {

        mqttService = serviceConnection.getMqttService();
        mqttService.toCreateNotification(message);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
        showDanmaku = false;
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }
    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.start:
//                path = mEditText.getText().toString();
//                if (!TextUtils.isEmpty(path)) {
//                    mVideoView.setVideoPath(path);
//                }
//                break;
//            case R.id.stop:
//                mVideoView.stopPlayback();
//                break;
//        }
    }
    private void initView_like() {
        likeViewLayout = (FlowLikeView) findViewById(R.id.flowLikeView);
    }

    public void addLikeView(View view) {
        likeViewLayout.addLikeView();
    }


}
