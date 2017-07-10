package com.sec.samsung.tvcontentsync;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.IOException;

/**
 * Created by Qnv96 on 16-Mar-17.
 */

public class SendLog extends Activity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        // setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.send_log);

        Button btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);

        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(this);

        //Utils.showToastLong(this, "Exception occurred, restarting app " + getIntent().getStringExtra("exception_name"));
        SettingManager.saveSetting(this, Define.JSON_RESTART_APP, true);
        Utils.restartApp(this);
    }

    @Override
    public void onClick(View v) {
        // respond to button clicks in your UI
        switch (v.getId()){

            case R.id.button1:
                try {
                    Utils.extractLogToFile(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finish();
                break;

            case R.id.button2:
                System.exit(0);
                break;

            default:
                break;
        }
    }



}
