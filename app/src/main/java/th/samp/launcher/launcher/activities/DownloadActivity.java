package th.samp.launcher.launcher.activities;

import static th.samp.launcher.Config.GAME_PATH;
import static th.samp.launcher.Config.PATH_DOWNLOADS;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StatFs;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.PointerIconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.indeterminate.IndeterminateCenteredRoundCornerProgressBar;
import com.hzy.libp7zip.P7ZipApi;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.ini4j.Wini;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import es.dmoral.toasty.Toasty;
import th.samp.launcher.App;
import th.samp.launcher.BuildConfig;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.Preferences;
import th.samp.launcher.launcher.fragments.ServersFragment;

public class DownloadActivity extends AppCompatActivity {
    private TextView downloadText, downloadPercent;
    private RoundCornerProgressBar downloadProgress;
    private IndeterminateCenteredRoundCornerProgressBar unZipProgress;

    private Handler handler;

    public int downloadId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_load);
        downloadProgress = findViewById(R.id.progressView);
        unZipProgress = findViewById(R.id.progressBarUnzip);
        downloadText = findViewById(R.id.textView2);
        downloadPercent = findViewById(R.id.textView15);
        long freeMemory = getFreeMemory();
        if (freeMemory < 3000) {
            handler.postDelayed(() -> {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.item_dialog_settings);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_full);
                dialog.getWindow().setLayout(-1, -2);
                ((TextView) dialog.findViewById(R.id.message)).setText("คุณมีหน่วยความจำเหลือน้อย ไม่สามารถทำการติดตั้งได้");
                ((TextView) dialog.findViewById(R.id.ok)).setOnClickListener(view1 -> {
                    view1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
                    handler.postDelayed(() -> dialog.dismiss(), 200);
                });
                dialog.show();
            }, 200);
            Utils.writeLog(this, 'i', "หน่วยความจำน้อย");
            return;
        }
        //
        if(App.getInstance().downloadID == null) {
            Utils.writeLog(this, 'e', "error, downloadid is null");
            return;
        }
        startDownload(App.getInstance().downloadID);
    }

    private long getFreeMemory() {
        try {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            return (statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong()) / 1048576;
        } catch (Exception unused) {
            return 268435455;
        }
    }

    private void startDownload(final int id)
    {
        try {
            clearDownloadsPath();
            downloadId = createDownloadTask(id).start();
        } catch (Exception e) {
            Utils.writeLog(DownloadActivity.this, 'e', "ข้อผิดพลาด startDownload: " + e.getMessage());
        }
    }

    private BaseDownloadTask createDownloadTask(final int position)
    {
        String url = "";
        boolean isDir = false;
        String path = "null";

        switch (position) {
            case 1:
                url = App.getInstance().URL_CLIENT;
                path = PATH_DOWNLOADS + "launcher.apk";
                break;
            case 2:
                url = App.getInstance().URL_GAME_FILES;
                path = PATH_DOWNLOADS + "game.zip";
                break;
            case 3:
                url = App.getInstance().URL_GAME_FILES_UPDATE;
                path = PATH_DOWNLOADS + "files_upd.zip";
                break;
            case 4:
                url = App.getInstance().URL_CLIENT_SAMPAPK;
                path = PATH_DOWNLOADS + "client.apk";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + position);
        }

        return FileDownloader.getImpl().create(url).
                setPath(path, false).
                setCallbackProgressTimes(300).
                setMinIntervalUpdateSpeed(400).
                setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.pending(task, soFarBytes, totalBytes);
                    }
                    @SuppressLint("SetTextI18n")
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);
                        final float percent = soFarBytes / (float) totalBytes;
                        downloadProgress.setMax(100);
                        downloadProgress.setProgress((int) (percent * 100));
                        downloadText.setText("กำลังโหลดไฟล์เกม...");
                        downloadPercent.setText((int) (percent * 100)+"%");
                        createNotification("กำลังโหลดไฟล์เกม... - "+(int) (percent * 100)+"%\n" +
                                Utils.formatFileSize(soFarBytes) + "/" +
                                Utils.formatFileSize(totalBytes) + " ความเร็ว: " +
                                Utils.formatFileSize(task.getSpeed() * 1024));
                        if(position == 1) {
                            downloadText.setText("อัปเดตรันเชอร์...");
                            downloadPercent.setText((int) (percent * 100)+"%");
                            createNotification("อัปเดตรันเชอร์... - "+(int) (percent * 100)+"%\n" +
                                    Utils.formatFileSize(soFarBytes) + "/" +
                                    Utils.formatFileSize(totalBytes) + " ความเร็ว: " +
                                    Utils.formatFileSize(task.getSpeed() * 1024));
                        }
                        if(position == 3) {
                            downloadText.setText("อัพเดทไฟล์เกม...");
                            downloadPercent.setText((int) (percent * 100)+"%");
                            createNotification("อัพเดทไฟล์เกม... - "+(int) (percent * 100)+"%\n" +
                                    Utils.formatFileSize(soFarBytes) + "/" +
                                    Utils.formatFileSize(totalBytes) + " ความเร็ว: " +
                                    Utils.formatFileSize(task.getSpeed() * 1024));
                        }
                        if(position == 4) {
                            downloadText.setText("กำลังโหลดapk client ...");
                            downloadPercent.setText((int) (percent * 100)+"%");
                            createNotification("ดาวน์โหลดapk client... - "+(int) (percent * 100)+"%\n" +
                                    Utils.formatFileSize(soFarBytes) + "/" +
                                    Utils.formatFileSize(totalBytes) + " ความเร็ว: " +
                                    Utils.formatFileSize(task.getSpeed() * 1024));
                        }
                    }
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                        Utils.writeLog(DownloadActivity.this, 'e', "ข้อผิดพลาด FileDownloader: " + e);
                        Toasty.error(DownloadActivity.this, "ข้อผิดพลาด: "+e, Toast.LENGTH_LONG).show();
                    }
                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        Utils.setDownloading(true);
                    }
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.paused(task, soFarBytes, totalBytes);
                        Utils.setDownloading(false);
                    }
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
                        if(position == 1) {
                            Toasty.info(DownloadActivity.this, "ยืนยันการติดตั้ง").show();
                            Utils.setDownloading(false);
                            installAPK("launcher");
                        }
                        else if(position == 2) {
                            downloadText.setText("กำลังแตกไฟล์...");
                            UnZipZip("game");
                        }
                        else if (position == 3) {
                            downloadText.setText("กำลังแตกไฟล์...");
                            String filePath = GAME_PATH + "data/ver.ini";
                            String sectionHeader = "[versions]\ngameFilesVersion" + " = " + App.getInstance().targetGameFilesVersion.toString();
                            writeFile(filePath, sectionHeader);
                            UnZipZip("files_upd");
                        }
                        else if (position == 4) {
                            Toasty.info(DownloadActivity.this, "ยืนยันการติดตั้ง").show();
                            String filePath = GAME_PATH + "data/apk.ini";
                            String sectionHeader = "[versions]\nsampapkVersion" + " = " + App.getInstance().targetClientSampVersion.toString();
                            writeFile(filePath, sectionHeader);
                            Utils.setDownloading(false);
                            installAPK("client");
                            loadFragment(new ServersFragment());
                        }
                        //Utils.setDownloading(false);
                    }
                    @Override
                    protected void warn(BaseDownloadTask task) { super.warn(task); }
                });
    }

    public static void writeFile(String path, String str) {
        createNewFile(path);
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(new File(path), false);
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_place, fragment);
        transaction.commit();
    }

    private static void createNewFile(String path) {
        int lastSep = path.lastIndexOf(File.separator);
        if (lastSep > 0) {
            String dirPath = path.substring(0, lastSep);
            makeDir(dirPath);
        }

        File file = new File(path);

        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeDir(String path) {
        if (!isExistFile(path)) {
            File file = new File(path);
            file.mkdirs();
        }
    }

    public static boolean isExistFile(String path) {
        File file = new File(path);
        return file.exists();
    }

    public void UnZipZip(final String zipname)
    {
        UnZipTask unzipTask = new UnZipTask(this);
        unzipTask.execute(PATH_DOWNLOADS, GAME_PATH, zipname+".zip");
    }

    private class UnZipTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public UnZipTask(Context context) { this.context = context; }
        @Override
        protected String doInBackground(String... params) {
            String filePath = params[0];
            String destinationPath = params[1];
            String fileName = params[2];
            P7ZipApi.executeCommand(UnZip.getExtractCmd(filePath+fileName, destinationPath));
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utils.setDownloading(true);
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            downloadPercent.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);
            unZipProgress.setVisibility(View.VISIBLE);
            downloadText.setText("กำลังแตกไฟล์...");
            createNotification("กำลังแตกไฟล์...");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) { super.onProgressUpdate(progress); }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            clearDownloadsPath();
            Utils.setDownloading(false);
            downloadProgress.setProgress(0);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
            if (result != null) {
                Toasty.error(DownloadActivity.this, "ข้อผิดพลาดในการแตกไฟล์: " + result, Toast.LENGTH_SHORT).show();
            } else {
                File settings = new File(GAME_PATH+"SAMP/settings.ini");
                if(settings.exists()) {
                    try {
                        Wini w = new Wini(settings);
                        w.put("client", "name", Preferences.getString(DownloadActivity.this, Preferences.NICKNAME));
                        w.store();
                    } catch (IOException e) {
                        Utils.writeLog(DownloadActivity.this, 'e', "ข้อผิดพลาด: "+e.getMessage());
                    }
                }
                Toasty.success(DownloadActivity.this, "ติดตั้งเกมแล้ว!", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
        }

        private void mkdirs(java.io.File outdir, String path)
        {
            java.io.File d = new java.io.File(outdir, path);
            if(!d.exists()) d.mkdirs();
        }

        private String dirpart(String name)
        {
            int s = name.lastIndexOf(java.io.File.separatorChar);
            return s == -1 ? null : name.substring(0, s);
        }
    }

    private void installAPK(String apkname) {
        try {
            File file = new File(PATH_DOWNLOADS, apkname + ".apk");
            Intent intent;
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
                    intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(apkUri);
                } else {
                    Uri apkUri = Uri.fromFile(file);
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        } catch (Exception e) {
            Utils.writeLog(this, 'e', "ข้อผิดพลาดในการติดตั้ง:" + e.getMessage());
        }
    }

    private void clearDownloadsPath()
    {
        File download_path = new File(PATH_DOWNLOADS);
        if (!download_path.exists()) {
            download_path.mkdirs();
        } else {
            File client = new File(PATH_DOWNLOADS, "client.apk");
            if(client.exists()) client.delete();
            File launcher = new File(PATH_DOWNLOADS, "launcher.apk");
            if(launcher.exists()) launcher.delete();
            File gameFiles = new File(PATH_DOWNLOADS, "game.zip");
            if(gameFiles.exists()) gameFiles.delete();
            File filesUpd = new File(PATH_DOWNLOADS, "files_upd.zip");
            if(filesUpd.exists()) filesUpd.delete();
            File filesGraph = new File(PATH_DOWNLOADS, "files_graph.zip");
            if(filesGraph.exists()) filesGraph.delete();
        }
    }

    @SuppressLint("RestrictedApi")
    public void createNotification(String str) {
        NotificationManager notifManager = null;
        NotificationCompat.Builder builder = null;
        Intent intent;
        PendingIntent pendingIntent;
        notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notifManager.getNotificationChannel("downloading_channel_1") == null) {
                NotificationChannel notificationChannel = new NotificationChannel("downloading_channel_1", "Загрузка", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription("ดาวน์โหลดและแตกไฟล์");
                notificationChannel.enableVibration(false);
                notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
                notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW); // IMPORTANCE_HIGH
                notificationChannel.setVibrationPattern(new long[]{0});
                notifManager.createNotificationChannel(notificationChannel);
            }
            builder = new NotificationCompat.Builder(this, "downloading_channel_1");
            intent = new Intent(this, getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentTitle("LAUNCHER MOBILE").setSmallIcon(R.mipmap.ic_launcher_round).setVibrate(new long[]{0}).setContentText(str).setAutoCancel(true).setContentIntent(pendingIntent).setTicker("SAMP STATE").setOnlyAlertOnce(true).setOngoing(true);
        } else {
            intent = new Intent(this, getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentTitle("LAUNCHER MOBILE").setSmallIcon(R.mipmap.ic_launcher_round).setVibrate(new long[]{0}).setContentText(str).setAutoCancel(true).setContentIntent(pendingIntent).setTicker("SAMP STATE").setOnlyAlertOnce(true).setOngoing(true).setPriority(1);
        }
        notifManager.notify(PointerIconCompat.TYPE_HAND, builder.build());
    }
}
