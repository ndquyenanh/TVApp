package com.sec.samsung.tvcontentsync;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.LockHome;
import com.sec.samsung.utils.Utils;

import java.io.File;

/**
 * Created by Qnv96 on 12/02/2016.
 */

public class AudioActivity extends BaseActivity {
    private String mAudioFile;
    private MediaPlayer mp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mp != null) {
                    mp.stop();
                }

                Debug.log("Stop Audio");
                finish();
            }
        };

        mTimePlaying = mIntent.getLongExtra(Define.PLAY_TIME, 1);
        mAudioFile = mIntent.getStringExtra(Define.PLAY_FILE_NAME);
        file_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_FILE_ID, -1);
        schedule_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);

        File file = new File(Define.APP_PATH + mAudioFile);
        Debug.log(file.getAbsolutePath(), "time = " + mTimePlaying, file_id, schedule_id);

        if (!file.exists()) {
            Utils.showToast(this, "Folder " + file + " does not exist!!!");
            Debug.logW("Folder " + file + " does not exist!!!");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        mp = MediaPlayer.create(this, Uri.fromFile(file));
        mp.setLooping(true);
        Utils.sendScheduledResponse2Server(this, file_id, schedule_id, true);

        delayPlaying();
        mp.start();
        mHandler.postDelayed(mRunnable, mTimePlaying - Define.TIME_GIAM_DI);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Debug.log("onResume Audio " + getFilePlaying());
        if (!mp.isPlaying())
            mp.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null) {
            mp.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debug.log("onDestroy Audio " + getFilePlaying());

        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    @Override
    public void cancelCalendar() {
        Debug.log("cancel Calendar Audio " + getFilePlaying());
        finish();
    }

    @Override
    public String getFilePlaying() {
        return mAudioFile;
    }
}
