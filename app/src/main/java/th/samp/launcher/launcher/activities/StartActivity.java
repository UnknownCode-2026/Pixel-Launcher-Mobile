package th.samp.launcher.launcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;
import th.samp.launcher.App;
import th.samp.launcher.BuildConfig;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.Preferences;

public class StartActivity extends AppCompatActivity
{
    Button download;
    EditText nickName;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_start);
        download = findViewById(R.id.button);
        nickName = findViewById(R.id.editTextTextPersonName);
        nickName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String obj = nickName.getText().toString();
                if (obj.isEmpty()) {
                    Toasty.warning(this, getResources().getString(R.string.enterNik), Toast.LENGTH_LONG).show();
                } else if (!obj.contains("_")) {
                    Toasty.warning(this, getResources().getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
                } else if (obj.length() < 4) {
                    Toasty.warning(this, getResources().getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
                } else {
                    Preferences.putString(this, Preferences.NICKNAME, obj);
                    Toasty.success(this, "บันทึกชื่อเล่นของคุณแล้ว: "+obj).show();
                    startActivity(new Intent(StartActivity.this, BuildConfig.DEBUG ? MainActivity.class : DownloadActivity.class));
                    finish();
                }
            }
            return false;
        });
        download.setOnClickListener(view -> {
            String obj = nickName.getText().toString();
            if (obj.isEmpty()) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.enterNik), Toast.LENGTH_LONG).show();
            } else if (!obj.contains("_")) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.mustContains_), Toast.LENGTH_LONG).show();
            } else if (obj.length() < 4) {
                Toasty.warning(StartActivity.this, getResources().getString(R.string.minLengthNik), Toast.LENGTH_LONG).show();
            } else {
                Preferences.putString(StartActivity.this, Preferences.NICKNAME, obj);
                Toasty.success(StartActivity.this, "บันทึกชื่อเล่นของคุณแล้ว: "+obj).show();
                if(BuildConfig.DEBUG) {
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                } else if(!Utils.isGameInstalled()) {
                    App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
                    startActivity(new Intent(StartActivity.this, DownloadActivity.class));
                    finish();
                }
            }
        });
    }
}
