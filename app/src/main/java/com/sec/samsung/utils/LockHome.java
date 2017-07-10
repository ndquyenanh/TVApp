package com.sec.samsung.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import com.sec.samsung.tvcontentsync.R;

/**
 * Created by sev_user on 15-Dec-16.
 */

public class LockHome {

    private OverlayDialog mOverlayDialog;

    public LockHome() {
        // Debug.log();
    }

    public void setLock(Activity a) {
        if (SettingManager.getModeUser(a) && !Utils.isTablet(a)) {
            if (mOverlayDialog == null) {
                mOverlayDialog = new OverlayDialog(a);
            }

            mOverlayDialog.show();
        } else {
            reset();
        }
    }

    public void reset() {
        if (mOverlayDialog != null) {
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
        }
    }

    private static class OverlayDialog extends AlertDialog {

        protected OverlayDialog(Activity a) {
            super(a, R.style.OverlayDialog);

            WindowManager.LayoutParams p = getWindow().getAttributes();
            p.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            p.dimAmount = 0.0f;
            p.width = 0;
            p.height = 0;
            p.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(p);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, 0xffffff);
            setOwnerActivity(a);
            setCancelable(false);
        }
    }
}
