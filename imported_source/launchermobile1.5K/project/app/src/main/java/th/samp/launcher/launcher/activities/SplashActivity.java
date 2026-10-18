package th.samp.launcher.launcher.activities;

import static th.samp.launcher.Config.GAME_PATH;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.messaging.FirebaseMessaging;
import com.liulishuo.filedownloader.FileDownloader;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import th.samp.launcher.App;
import th.samp.launcher.BuildConfig;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.Preferences;
import th.samp.launcher.launcher.fragments.ServersFragment;
import th.samp.launcher.launcher.fragments.UpdateClient;
import th.samp.launcher.launcher.network.ApiService;
import th.samp.launcher.launcher.network.Links;
import th.samp.launcher.launcher.network.Server;
import th.samp.launcher.launcher.network.Story;

public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    // permissions
    private static final int MAIN_PERMISSIONS_REQUEST_CODE = 300;
    private Handler handler;
    String[] main_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        if (EasyPermissions.hasPermissions(this, main_permissions)) {
            Utils.writeLog(this, 'i', "app sucessfully started!");
            Init();
        } else {
            Utils.writeLog(this, 'i', "app not have full permissions!!!");
            EasyPermissions.requestPermissions(this,
                    "แอปต้องได้รับอนุญาตในการเขียนไปยังหน่วยความจำ",
                    MAIN_PERMISSIONS_REQUEST_CODE, main_permissions);
        }
    }
    private void Init()
    {
        FileDownloader.setup(this);
        if (!Preferences.getBoolean(this, Preferences.FIRST_START)) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Utils.writeLog(SplashActivity.this, 'e', "FCM Reg failed: "+task.getException());
                    return;
                }
                Preferences.putString(App.getInstance(), Preferences.USER_FCM_KEY, task.getResult());
                Utils.writeLog(SplashActivity.this, 'i', "FCM reg success! Token: "+task.getResult());
            });
            Preferences.putBoolean(SplashActivity.this, Preferences.FIRST_START, true);
            loadAPI();
        }
        else {
            loadAPI();
        }
    }

    // загрузка апи, новостей, серверов, итд
    private void loadAPI() {
        ApiService.getInstance().getApiService().getLinks().enqueue(new Callback<Links>() {
            public void onResponse(Call<Links> call, Response<Links> response) {
                if(response.isSuccessful())
                {
                    if(response.body() != null) {
                        // версии
                        App.getInstance().targetClientVersion = response.body().getTargetClientVersion();
                        App.getInstance().targetClientSampVersion = response.body().getTargetClientSampVersion();
                        App.getInstance().targetGameFilesVersion = response.body().getTargetGameFilesVersion();
                        // ссылки на файлы
                        App.getInstance().URL_CLIENT = response.body().getUrlClient();
                        App.getInstance().URL_CLIENT_SAMPAPK = response.body().getUrlClientSampapk();
                        App.getInstance().URL_GAME_FILES = response.body().getUrlFiles();
                        App.getInstance().URL_GAME_FILES_UPDATE = response.body().getUrlFilesUpdate();
                        // ресурсы проекта
                        App.getInstance().URL_FORUM = response.body().getUrlForum();
                        App.getInstance().URL_DONATE = response.body().getUrlDonate();
                        // соц сети
                        App.getInstance().URL_VK = response.body().getUrlVKontakte();
                        App.getInstance().URL_DISCORD = response.body().getUrlDiscord();
                        App.getInstance().URL_YOUTUBE = response.body().getUrlYoutube();
                        // загрузка остального
                        loadServers();
                    }
                } else {
                    Toasty.error(SplashActivity.this, "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ Launcher โปรดลองอีกครั้งในภายหลัง", Toasty.LENGTH_LONG).show();
                    Utils.writeLog(SplashActivity.this, 'e', "ข้อผิดพลาดในการโหลดรายการ API: "+response.errorBody());
                }
            }
            public void onFailure(Call<Links> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "ข้อผิดพลาดในการโหลดรายการ API: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "คุณไม่มีการเชื่อมต่ออินเทอร์เน็ต", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "ข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์ เซิร์ฟเวอร์ไม่พร้อมใช้งาน", Toasty.LENGTH_LONG).show();
            }
        });
    }
    private void loadServers() {
        ApiService.getInstance().getApiService().getServers().enqueue(new Callback<ArrayList<Server>>() {
            public void onResponse(Call<ArrayList<Server>> call, Response<ArrayList<Server>> response) {
                if(response.isSuccessful())
                {
                    if (response.body() != null) {
                        App.getInstance().serverList.clear();
                        App.getInstance().serverList.addAll(response.body());
                        if (App.getInstance().serverListener != null) {
                            App.getInstance().serverListener.onChange();
                        }
                        loadStories();
                    }
                } else {
                    Utils.writeLog(SplashActivity.this, 'e', "เกิดข้อผิดพลาดในการโหลดเซิร์ฟเวอร์: "+response.errorBody());
                }
            }
            public void onFailure(Call<ArrayList<Server>> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "เกิดข้อผิดพลาดในการโหลดเซิร์ฟเวอร์: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "คุณไม่มีการเชื่อมต่ออินเทอร์เน็ต", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "ข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์ เซิร์ฟเวอร์ไม่พร้อมใช้งาน", Toasty.LENGTH_LONG).show();
            }
        });
    }

    private void loadStories() { // загрузка новостей
        ApiService.getInstance().getApiService().getStories().enqueue(new Callback<ArrayList<Story>>() {
            public void onResponse(Call<ArrayList<Story>> call, Response<ArrayList<Story>> response) {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        App.getInstance().stories.clear();
                        App.getInstance().stories.addAll(response.body());
                        runOnUiThread(() -> startLauncher());
                    }
                } else {
                    Utils.writeLog(SplashActivity.this, 'e', "ข้อผิดพลาดในการโหลดรายการข่าว: "+response.errorBody());
                }
            }
            public void onFailure(Call<ArrayList<Story>> call, Throwable th) {
                Utils.writeLog(SplashActivity.this, 'e', "ข้อผิดพลาดในการโหลดรายการข่าว: "+th.getMessage());
                if(!Utils.isInternetConnected(SplashActivity.this))
                {
                    Toasty.error(SplashActivity.this, "คุณไม่มีการเชื่อมต่ออินเทอร์เน็ต", Toasty.LENGTH_LONG).show();
                    return;
                }
                Toasty.error(SplashActivity.this, "ข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์ เซิร์ฟเวอร์ไม่พร้อมใช้งาน", Toasty.LENGTH_LONG).show();
            }
        });
    }

    //-------------------------------------------------------------------

    private void startLauncher() {
        if(/*!BuildConfig.DEBUG && */App.getInstance().targetClientVersion != BuildConfig.VERSION_CODE) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new UpdateClient());
            transaction.commit();
            return;
        }
        handler.postDelayed((Runnable) () -> {
            Intent intent = new Intent(this, MainActivity.class);
            if(Preferences.getString(App.getInstance(), Preferences.NICKNAME).isEmpty())
            {
                intent = new Intent(this, StartActivity.class);
            }
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
        }, 1000);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    public void onPermissionsGranted(int requestCode, List<String> list) {
        if(requestCode == MAIN_PERMISSIONS_REQUEST_CODE) {
            Init();
        }
    }
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if(requestCode == MAIN_PERMISSIONS_REQUEST_CODE) {
            Utils.writeLog(SplashActivity.this, 'i', "ข้อผิดพลาดเกี่ยวกับการอนุญาต");
            Toasty.error(SplashActivity.this, "แอพต้องมีสิทธิ์ทั้งหมดตามที่ขอ !!!", Toast.LENGTH_LONG).show();
            //finish();
        }
    }
}