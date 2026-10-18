package th.samp.launcher.launcher.network;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure;
import androidx.core.app.NotificationCompat;
import androidx.core.internal.view.SupportMenu;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import th.samp.launcher.R;
import th.samp.launcher.launcher.Preferences;
import th.samp.launcher.launcher.activities.SplashActivity;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.d("Notification", "Message Notification Body: " + remoteMessage.getData());
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        @SuppressLint("WrongConstant")
        NotificationCompat.Builder contentIntent = new NotificationCompat.Builder((Context) this, "channelId").
                setSmallIcon((int) R.mipmap.ic_launcher).
                setContentTitle(data.get("title")).
                setContentText(data.get("text")).
                setAutoCancel(true).
                setSound(Settings.System.DEFAULT_NOTIFICATION_URI).
                setContentIntent(PendingIntent.getActivity(this, 0, intent, BasicMeasure.EXACTLY));
        String str = data.get("image");
        if (str == null) {
            contentIntent.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        } else if (str.isEmpty()) {
            contentIntent.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        } else {
            contentIntent.setLargeIcon(getBitmapFromURL(str));
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("channelId", "channelId", NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes build = new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_NOTIFICATION).
                    setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            notificationChannel.setDescription("Новое сообщение");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(true);
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, build);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        if (Preferences.getBoolean(getApplicationContext(), Preferences.NOTIFICATION, true)) {
            notificationManager.notify(0, contentIntent.build());
        }
    }

    public static Bitmap getBitmapFromURL(String str) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            return BitmapFactory.decodeStream(httpURLConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
