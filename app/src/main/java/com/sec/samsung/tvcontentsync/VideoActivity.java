package com.sec.samsung.tvcontentsync;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.LockHome;
import com.sec.samsung.utils.Utils;

import java.io.File;

/**
 * Created by Qnv96 on 24-Dec-16.
 */

public class VideoActivity extends BaseActivity {
    private String mVideoFile;

    private VideoView mVideoView;
    private int mPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mVideoView != null)
                    mVideoView.stopPlayback();

                Debug.logV("Play time is end so stopped playing!!!! " + mVideoFile);
                finish();
            }
        };

        mTimePlaying = mIntent.getLongExtra(Define.PLAY_TIME, 0);
        mTimePlaying -= Define.TIME_GIAM_DI;

        mVideoFile = mIntent.getStringExtra(Define.PLAY_FILE_NAME);
        file_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_FILE_ID, -1);
        schedule_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);

        Debug.logV(mVideoFile, mTimePlaying, file_id, schedule_id);
        File file = new File(Define.APP_PATH + mVideoFile);
        if (!file.exists()) {
            Utils.showToast(this, "Video " + mVideoFile + " does not exist!!!");
            Debug.logW("Video " + mVideoFile + " does not exist!!!");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setVideoURI(Uri.fromFile(file));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        Utils.sendScheduledResponse2Server(this, file_id, schedule_id, true);
        delayPlaying();
        mVideoView.start();
        mHandler.postDelayed(mRunnable, mTimePlaying);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.logV("mPos = " + mPos);

        if (mPos != -1) {
            mVideoView.seekTo(mPos);
        }

        if (!mVideoView.isPlaying())
            mVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.logV(mVideoFile);

        mPos = mVideoView.getCurrentPosition();
        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debug.logV("onDestroy video " + getFilePlaying());
        mVideoView = null;
    }

    @Override
    public void cancelCalendar() {
        Debug.logV("cancel Calendar video " + getFilePlaying());
        finish();
    }

    @Override
    public String getFilePlaying() {
        return mVideoFile;
    }
}
