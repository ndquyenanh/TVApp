package com.sec.samsung.tvcontentsync;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sec.samsung.schedule.ScheduleDBManager;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.IOException;

/**
 * Created by sev_user on 14-Dec-16.
 */

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().beginTransaction().replace(android.R.id.content, new MySetting()).commit();
            }
        });

        TvContentSyncApplication.getInstance().setCurActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public static class MySetting extends PreferenceFragment {

        private Context context;

        public MySetting() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            context = getActivity();

            Preference btn = findPreference("about_us");
            btn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.showAlert(getActivity(), "About us", "TVContentSync v" + Utils.getVersionName(context) + "\nDevelopment team: \n" +
                            "Nguyen Vuong Quyen\n" +
                            "Nguyen Tuan Anh\n" +
                            "Cao Duc Toan\n" +
                            "Bui Minh Trang\n" +
                            "For more information, please contact us:\n" +
                            "vuong.quyen@samsung.com");
                    return false;
                }
            });

            Preference deleteDb = findPreference("clear_db");
            deleteDb.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.showAlert(getActivity(), "Do you want to delete Schedule database?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ScheduleDBManager dbManager = new ScheduleDBManager(getActivity());
                            dbManager.deleteAll();
                            Utils.showToast(getActivity(), "Clear DB");
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return false;
                }
            });

            Preference getFullLog = findPreference("get_full_log");
            getFullLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utils.showToast(getActivity(), "Saved full log in " + Define.APP_PATH_LOGS + " !!!");
                    try {
                        Utils.extractLogToFile(getActivity());
                    } catch (IOException e) {
                        Debug.logW(e.getLocalizedMessage());
                    }
                    return false;
                }
            });

            //EditTextPreference ftpName = (EditTextPreference) findPreference(Define.FTP_NAME);
            //ftpName.set
//            Preference sendLog = findPreference("ftp_account");
//            sendLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    final AlertDialog dialog = builder.create();
//
//                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.ftp_layout, null);
//                    final EditText name = (EditText) view.findViewById(R.id.et_ftp_name);
//                    final EditText pwd = (EditText) view.findViewById(R.id.et_ftp_pwd);
//
//                    String nameStr = SettingManager.getSetting(getActivity(), Define.FTP_NAME, String.class);
//                    String pwdStr = SettingManager.getSetting(getActivity(), Define.FTP_PASSWORD, String.class);
//                    name.setText(nameStr);
//                    pwd.setText(pwdStr);
//
//                    Button ok = (Button) view.findViewById(R.id.btn_ok);
//                    Button cancel = (Button) view.findViewById(R.id.btn_cancel);
//                    ok.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            SettingManager.saveSetting(getActivity(), Define.FTP_NAME, name.getText());
//                            SettingManager.saveSetting(getActivity(), Define.FTP_PASSWORD, pwd.getText());
//                            dialog.dismiss();
//                        }
//                    });
//
//                    cancel.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    // builder.setView(view);
//                    dialog.setView(view);
//                    dialog.show();
//                    return false;
//                }
//            });
        }
    }
}
