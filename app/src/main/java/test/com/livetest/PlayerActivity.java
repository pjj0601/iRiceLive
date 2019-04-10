package test.com.livetest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.anenn.flowlikeviewlib.FlowLikeView;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by Sikang on 2017/5/2.
 */

public class PlayerActivity extends Activity implements View.OnClickListener {
    private String path = "";
    private VideoView mVideoView;
    private FlowLikeView likeViewLayout;
//    private EditText mEditText;
//    private Button mStartBtn;
//    private Button mStopBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
//        mEditText = (EditText) findViewById(R.id.url);

//        mStartBtn = (Button) findViewById(R.id.start);
//        mStopBtn = (Button) findViewById(R.id.stop);
//        mStartBtn.setOnClickListener(this);
//        mStopBtn.setOnClickListener(this);

        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mVideoView.setVideoPath("rtmp://119.23.111.7:1935/hls/test");

        initView();

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
    private void initView() {
        likeViewLayout = (FlowLikeView) findViewById(R.id.flowLikeView);
    }

    public void addLikeView(View view) {
        likeViewLayout.addLikeView();
    }


}
