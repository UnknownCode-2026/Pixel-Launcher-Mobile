package th.samp.launcher.launcher.activities;

import static es.dmoral.toasty.Toasty.LENGTH_SHORT;
import static th.samp.launcher.Config.PATH_DOWNLOADS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;

import es.dmoral.toasty.Toasty;
import th.samp.launcher.App;
import th.samp.launcher.BuildConfig;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.fragments.NewsFragment;
import th.samp.launcher.launcher.fragments.ServersFragment;
import th.samp.launcher.launcher.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity
{
    ImageView[] btnImages = new ImageView[3];
    ImageView btnDonate;
    LinearLayout btnNews, btnHome, btnSettings;

    private boolean doubleBackToExitPressedOnce;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        if(!BuildConfig.DEBUG && !Utils.isGameInstalled())
        {
            App.getInstance().downloadID = App.INSTALL_TYPE_GAMEFILES;
            startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        btnImages[0] = findViewById(R.id.news_button);
        btnImages[1] = findViewById(R.id.home_button);
        btnImages[2] = findViewById(R.id.settings_button);
        btnNews = findViewById(R.id.news_btn);
        btnHome = findViewById(R.id.home_btn);
        btnSettings = findViewById(R.id.settings_btn);
        btnDonate = findViewById(R.id.btn_donate);
        btnNews.setOnClickListener(view -> {
            changeImage(0);
            loadFragment(new NewsFragment());
        });
        btnHome.setOnClickListener(view -> {
            changeImage(1);
            loadFragment(new ServersFragment());
        });
        btnSettings.setOnClickListener(view -> {
            changeImage(2);
            loadFragment(new SettingsFragment());
        });
        btnDonate.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.getInstance().URL_DONATE)));
        });
        File download_path = new File(PATH_DOWNLOADS);
        if(!download_path.exists()) { download_path.mkdirs(); }
        changeImage(1);
        loadFragment(new ServersFragment());
    }
    @Override
    public void onBackPressed()
    {
        if (doubleBackToExitPressedOnce) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
            FileDownloader.getImpl().pauseAll();
            System.exit(0);
            return;
        }
        Toasty.info(MainActivity.this, "กดอีกครั้งเพื่อออก", LENGTH_SHORT).show();
        doubleBackToExitPressedOnce = true;
        handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    public void changeImage(int clickedID) {
        if (clickedID == 0) {
            btnImages[0].setImageResource(R.drawable.ic_icon_news_click);
            btnImages[1].setImageResource(R.drawable.ic_icon_home);
            btnImages[2].setImageResource(R.drawable.ic_icon_settings);
        } else if (clickedID == 1) {
            btnImages[0].setImageResource(R.drawable.ic_icon_news);
            btnImages[1].setImageResource(R.drawable.ic_icon_home_click);
            btnImages[2].setImageResource(R.drawable.ic_icon_settings);
        } else if (clickedID == 2) {
            btnImages[0].setImageResource(R.drawable.ic_icon_news);
            btnImages[1].setImageResource(R.drawable.ic_icon_home);
            btnImages[2].setImageResource(R.drawable.ic_icon_settings_click);
        } else {
            return;
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, fragment);
        transaction.commit();
    }
}
