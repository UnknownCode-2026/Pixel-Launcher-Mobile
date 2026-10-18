package th.samp.launcher.launcher.adapters;

import static th.samp.launcher.Config.API_LINK;
import static th.samp.launcher.Config.GAME_PATH;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.ini4j.Wini;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import th.samp.launcher.App;
import th.samp.launcher.BuildConfig;
import th.samp.launcher.Config;
import th.samp.launcher.R;
import th.samp.launcher.Utils;
import th.samp.launcher.launcher.activities.DownloadActivity;
import th.samp.launcher.launcher.fragments.ServersFragment;
import th.samp.launcher.launcher.network.SampQuery;
import th.samp.launcher.launcher.network.Server;
public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {
    public ServersFragment monitoringFragment;
    private ArrayList<Server> serverList;

    private Context context;
    public ServerAdapter(ServersFragment monitoringFragment2, ArrayList<Server> arrayList) {
        this.monitoringFragment = monitoringFragment2;
        this.serverList = arrayList;
        this.context = context;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_server, viewGroup, false));
    }
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String fileUrl = API_LINK+"apk.ini"; // URL of the file to be downloaded
        String destinationPath = GAME_PATH+"data/apk.ini"; // Path where the file should be saved
        new Thread(() -> {
            InetAddress ServerAddress = null;
            SampQuery query;
            try {
                ServerAddress = InetAddress.getByName(serverList.get(i).getIP());
            } catch (UnknownHostException e) {
                Utils.writeLog(monitoringFragment.getContext(), 'e', "เซิร์ฟเวอร์ผิดพลาด: " + e.getMessage());
            }
            if(ServerAddress != null) {
                if (ServerAddress.getHostAddress() != null || !ServerAddress.getHostAddress().isEmpty()) {
                    query = new SampQuery(ServerAddress.getHostAddress(), serverList.get(i).getPort());
                    if (query.connect()) {
                        String[] serverInfo = query.getInfo();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            viewHolder.online.setText(serverInfo[1] + "/" + serverInfo[2]);
                        });
                        query.close();
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            viewHolder.online.setText("ไม่สามารถใช้ได้");
                        });
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        viewHolder.online.setText("ไม่สามารถใช้ได้");
                    });
                }
            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    viewHolder.online.setText("ไม่สามารถใช้ได้");
                });
            }
        }).start();
        viewHolder.serverName.setText(serverList.get(i).getName());
        viewHolder.serverID.setText("0"+serverList.get(i).getID());
        viewHolder.btnPlay.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(monitoringFragment.getContext(), R.anim.button_click));
            if (!Utils.isGameInstalled()) {
                Toasty.info(monitoringFragment.requireContext(),
                        BuildConfig.DEBUG ? "โหมดทดสอบ Launcher: ยังไม่มีไฟล์เกม กรุณารอการอัปเดต" : "ยังไม่ได้ติดตั้งไฟล์เกม",
                        Toasty.LENGTH_LONG).show();
                return;
            }
            File gameFiles = new File(GAME_PATH + "data/ver.ini");
            Integer TARGET_GAMEFILES_VERSION = 0;
            if (gameFiles.exists()) {
                try {
                    Wini w = new Wini(gameFiles);
                    TARGET_GAMEFILES_VERSION = Integer.valueOf(w.get("versions", "gameFilesVersion"));
                    w.store();
                } catch (IOException e) {
                    Utils.writeLog(monitoringFragment.getContext(), 'e', e.getMessage());
                }
            }
            if (App.getInstance().targetGameFilesVersion != TARGET_GAMEFILES_VERSION) {
                App.getInstance().downloadID = App.INSTALL_TYPE_UPDATE_GAMEFILES;
                monitoringFragment.getContext().startActivity(new Intent(monitoringFragment.getContext(), DownloadActivity.class));
                monitoringFragment.getActivity().finish();
                return;
            }
            File apkFiles = new File(GAME_PATH + "data/apk.ini");
            Integer TARGET_SAMPAPK_VERSION = 0;
            if (apkFiles.exists()) {
                try {
                    Wini w = new Wini(apkFiles);
                    TARGET_SAMPAPK_VERSION = Integer.valueOf(w.get("versions", "sampapkVersion"));
                    w.store();
                } catch (IOException e) {
                    Utils.writeLog(monitoringFragment.getContext(), 'e', e.getMessage());
                }
            }
            if (App.getInstance().targetClientSampVersion != TARGET_SAMPAPK_VERSION) {
                App.getInstance().downloadID = App.INSTALL_TYPE_UPDATE_SAMPAPK;
                monitoringFragment.getContext().startActivity(new Intent(monitoringFragment.getContext(), DownloadActivity.class));
                monitoringFragment.getActivity().finish();
                return;
            }
            File settings = new File(GAME_PATH + "SAMP/settings.ini");
            if(settings.exists()) {
                Wini w = null;
                try {
                    w = new Wini(settings);
                    // выбор сервера (клиент читает конфиг и подключается к определенному серверу)
                    w.put("client", "server", serverList.get(i).getID());
                    w.store();
                } catch (IOException e) {
                    Utils.writeLog(monitoringFragment.getContext(), 'e', "ข้อผิดพลาด: "+e.getMessage());
                }
                String packageName = "com.rockstargames.gtafl";
                Intent launchIntent = monitoringFragment.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    monitoringFragment.getContext().startActivity(launchIntent);
                }else{
                    App.getInstance().downloadID = App.INSTALL_TYPE_UPDATE_SAMPAPK;
                    monitoringFragment.getContext().startActivity(new Intent(monitoringFragment.getContext(), DownloadActivity.class));
                    monitoringFragment.getActivity().finish();
                }
                monitoringFragment.getActivity().finish();
            }
        });
    }


    public int getItemCount() { return this.serverList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView serverName;
        public TextView online;
        public TextView serverID;
        public ImageButton btnPlay;

        public ViewHolder(View view) {
            super(view);
            this.serverID = (TextView) view.findViewById(R.id.textView11);
            this.serverName = (TextView) view.findViewById(R.id.textView12);
            this.online = (TextView) view.findViewById(R.id.textView14);
            this.btnPlay = (ImageButton) view.findViewById(R.id.button5);
        }
    }
}
