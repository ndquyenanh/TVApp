package com.sec.samsung.tvcontentsync;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Qnv96 on 03-May-17.
 */

public class MediaActivity extends BaseActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener{

    private String mFolderMedia;
    private MediaPlayer mediaPlayer;
    private ImageView mImageView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private int index;

    private File[] mFiles;
    private int mLen;
    private boolean isOutTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delayPlaying();

        // setContentView(R.layout.activity_media);
        setContentView(R.layout.my_layout);
        Debug.log("Beginning media " + getFilePlaying());

        File mFolder;

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                //if (mediaPlayer != null) {
                //    mediaPlayer.stop();
                //}

                isOutTime = true;
                Debug.log("End of time so Stop Media " + getFilePlaying());
                finish();
            }
        };

        // mSurfaceView = (SurfaceView) findViewById(R.id.my_surface);
        // mImageView = (ImageView) findViewById(R.id.my_image);

        mSurfaceView = (SurfaceView) findViewById(R.id.my_surface_view);
        mImageView = (ImageView) findViewById(R.id.my_image_view);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        isOutTime = false;

        mTimePlaying = mIntent.getLongExtra(Define.PLAY_TIME, 1);
        mFolderMedia = mIntent.getStringExtra(Define.PLAY_FILE_NAME);
        file_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_FILE_ID, -1);
        schedule_id = mIntent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);
        index = 0;

        mFolder = new File(Define.APP_PATH + mFolderMedia);
        Debug.logI(mFolder.getAbsolutePath(), "time = " + mTimePlaying, file_id, schedule_id);

        if (!mFolder.exists()) {
            //Utils.showToast(this, "Folder " + mFolderMedia + " does not exist!!!");
            Debug.logW("Folder " + mFolderMedia + " does not exist!!!");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        mFiles = mFolder.listFiles();
        mLen = mFiles.length;
        Debug.log("files = " + mLen);
        if (mFiles == null || mLen == 0) {
            //Utils.showToast(this, "Folder " + mFolderMedia + " does not contain any media files");
            Debug.logW("Folder " + mFolderMedia + " does not contain any media files");
            Utils.sendScheduledResponse2Server(this, file_id, schedule_id, false);
            finish();
            return;
        }

        Arrays.sort(mFiles);
        // mApp.updateNotification(this, "Playing " + getFilePlaying());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.log(getFilePlaying());
        // Utils.appStatus = Define.PLAYING_STATUS;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Debug.logE(intent.getStringExtra(Define.PLAY_FILE_NAME));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.log("RESUME " + getFilePlaying());
        // Utils.sendScheduledResponse2Server(this, file_id, schedule_id, true);
        mHandler.postDelayed(mRunnable, mTimePlaying - Define.TIME_GIAM_DI);
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * cancel schedule
     */
    @Override
    public void cancelCalendar() {
        Debug.log("cancel Calendar Media " + getFilePlaying());
        finish();
    }

    /**
     * get file which is playing
     *
     * @return file name
     */
    @Override
    public String getFilePlaying() {
        return mFolderMedia;
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a {@link Surface}, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(mHolder);

        File f = mFiles[index % mLen];

        try {
            mediaPlayer.setDataSource(this, Uri.fromFile(f));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setLooping(false);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            Debug.logW(e);
        }

        setVisibility(f);
        Debug.logV("Created media player for " + getFilePlaying());
    }

    private void setVisibility(File file) {
//        String name = Utils.getFileExt(file);
//        name = "." + name;
//        Debug.log(name);

        // if (Arrays.asList(Define.AUDIO_FILE_EX).contains(name)) {
            //mSurfaceView.setVisibility(View.INVISIBLE);
            //mImageView.setVisibility(View.VISIBLE);
        if (Utils.isVideo(this, file)){
            mSurfaceView.bringToFront();
            mSurfaceView.getParent().requestLayout();
        } else {
            //mSurfaceView.setVisibility(View.VISIBLE);
            //mImageView.setVisibility(View.INVISIBLE);
            mImageView.bringToFront();
            mImageView.getParent().requestLayout();
        }
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Debug.logV(getFilePlaying());
    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Debug.logI(getFilePlaying());
    }

    /**
     * Called when the media file is ready for playback.
     *
     * @param mp the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Debug.log("Start playing " + mFiles[index % mLen].getName());
        mp.start();
        // mp.setOnCompletionListener(this);
    }

    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Debug.log("onCompletion " + mFiles[index % mLen].getName());
        index++;
        //if (index >= mFiles.length) {
        //    index = 0;
        //}

        File f = mFiles[index % mLen];
        File f_p = mFiles[(index-1) % mLen];
        boolean t_m = false;
        if (Utils.getFileExt(f).equals("mp3") && Utils.getFileExt(f_p).equals("mp3")){
            t_m = true;
        }

        try {
            mediaPlayer.reset();
            // mediaPlayer = MediaPlayer.create(this, Uri.fromFile(f));
            if (t_m){
                // Debug.log("for music");
                // mediaPlayer = new MediaPlayer();
            }
            //
            mediaPlayer.setDataSource(this, Uri.fromFile(f));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            // mp.start();
        } catch (Exception e) {
            Debug.logW(e);
        }

        setVisibility(f);
        Debug.log("Prepared playing " + f.getName() + ", index = " + (index % mLen));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.logW("Pause media " + getFilePlaying());
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (isOutTime){
            Debug.log("End time play " + getFilePlaying());
            destroyMedia();
        }
    }

    private void destroyMedia(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mSurfaceView != null) {
            mSurfaceView.setVisibility(View.GONE);
            mSurfaceView = null;
        }

        if (mHolder != null) {
            mHolder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debug.logW("Destroy media " + getFilePlaying());
        destroyMedia();
    }

    /**
     * Called to indicate an error.
     *
     * @param mp    the MediaPlayer the error pertains to
     * @param what  the type of error that has occurred:
     *              <ul>
     *              <li>{@link #MEDIA_ERROR_UNKNOWN}
     *              <li>{@link #MEDIA_ERROR_SERVER_DIED}
     *              </ul>
     * @param extra an extra code, specific to the error. Typically
     *              implementation dependent.
     *              <ul>
     *              <li>{@link #MEDIA_ERROR_IO}
     *              <li>{@link #MEDIA_ERROR_MALFORMED}
     *              <li>{@link #MEDIA_ERROR_UNSUPPORTED}
     *              <li>{@link #MEDIA_ERROR_TIMED_OUT}
     *              <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
     *              </ul>
     * @return True if the method handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Debug.logE("Error " + what);
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Debug.logW(getFilePlaying());
    }
}
