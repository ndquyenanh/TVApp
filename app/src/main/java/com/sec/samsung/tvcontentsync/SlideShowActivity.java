package com.sec.samsung.tvcontentsync;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.LockHome;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;
// import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Arrays;

import com.bumptech.glide.Glide;

/**
 * Created by Qnv96 on 23-Nov-16.
 */

public class SlideShowActivity extends BaseActivity {
    private ViewFlipper mViewFlipper;
    private String mFolderImages;

    private ImageView[] IMGS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_slideshow_layout);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (mViewFlipper != null)
                    mViewFlipper.stopFlipping();

                Debug.log("Stop slide show");
                finish();
            }
        };

        File mFolder;
        mTimePlaying = mIntent.getLongExtra(Define.PLAY_TIME, 1);
        mFolderImages = mIntent.getStringExtra(Define.PLAY_FILE_NAME);
        file_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_FILE_ID, -1);
        schedule_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);

        mFolder = new File(Define.APP_PATH + mFolderImages);
        Debug.logI(mFolder.getAbsolutePath(), "time = " + mTimePlaying, file_id, schedule_id);

        if (!mFolder.exists()) {
           // Utils.showToast(this, "Folder " + mFolderImages + " does not exist!!!");
            Debug.logW("Folder " + mFolderImages + " does not exist!!!");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        File[] files = mFolder.listFiles();
        int len = files.length;
        Debug.log("files = " + len);
        if (files == null || len == 0) {
            //Utils.showToast(this, "Folder " + mFolderImages + " does not contain any photos");
            Debug.logW("Folder " + mFolderImages + " does not contain any photos");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        mViewFlipper = (ViewFlipper) findViewById(R.id.simpleViewFlipper);
        Utils.sendScheduledResponse2Server(this, file_id, schedule_id, true);
        Arrays.sort(files);
        IMGS = new ImageView[len];
        for (int i = 0; i < len; i++) {
            IMGS[i] = new ImageView(this);
            // IMGS[i].setImageURI(Uri.fromFile(files[i]));
            // Picasso.with(this).load(files[i]).into(IMGS[i]);
            Glide.with(this).load(files[i]).into(IMGS[i]);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            IMGS[i].setLayoutParams(layoutParams);
            IMGS[i].setScaleType(ImageView.ScaleType.FIT_XY);
            mViewFlipper.addView(IMGS[i]);
        }

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        mViewFlipper.setInAnimation(in);
        mViewFlipper.setOutAnimation(out);

        mViewFlipper.setFlipInterval(Define.PLAY_SLIDE_TIME_INTERVAL);
        delayPlaying();
        mViewFlipper.setAutoStart(true);
        mViewFlipper.startFlipping();
        mHandler.postDelayed(mRunnable, mTimePlaying - Define.TIME_GIAM_DI);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.log();
        // Utils.appStatus = Define.PLAYING_STATUS;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.log("onResume slide show");
        if (!mViewFlipper.isFlipping()){
            mViewFlipper.startFlipping();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.logW("onPause slide show " + getFilePlaying());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Debug.logW("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debug.logW("onDestroy slide show " + getFilePlaying());

        if (IMGS != null && IMGS.length >= 1) {
            for (int  i = 0; i <IMGS.length; i++){
                Drawable drawable = IMGS[i].getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    bitmap.recycle();
                }

                IMGS[i] = null;
            }
        }

        if (mViewFlipper != null) {
            mViewFlipper.removeAllViews();
            mViewFlipper = null;
        }
    }

    @Override
    public void cancelCalendar() {
        Debug.logW("cancel Calendar slide show " + getFilePlaying());
        finish();
    }

    @Override
    public String getFilePlaying() {
        return mFolderImages;
    }
}
