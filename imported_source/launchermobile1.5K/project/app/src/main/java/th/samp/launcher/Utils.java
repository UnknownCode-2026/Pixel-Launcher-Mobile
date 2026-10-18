package th.samp.launcher;

import static th.samp.launcher.Config.APP_PATH;
import static th.samp.launcher.Config.GAME_PATH;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import es.dmoral.toasty.Toasty;

public class Utils extends AppCompatActivity {

    protected void onCreate(Bundle bundle) { super.onCreate(bundle); }

    static boolean downloading = false;
    static Integer typeInstall = 0;
    public static Integer INSTALL_TYPE_CLIENT  = 1;
    public static Integer INSTALL_TYPE_REINSTALL = 2;
    public static Integer INSTALL_TYPE_UPDATE_GAMEFILES = 3;
    public static Integer INSTALL_TYPE_UPDATE_SAMPAPK = 4;


    // 1 - reinstall the game (only files)
    // 2 - graph (gta_sa.set)

    public static boolean getDownloading() { return downloading; }
    public static Integer getInstallType () { return typeInstall; }
    public static boolean setDownloading(boolean value) { return downloading = value; }
    public static Integer setInstallType(int type) { return typeInstall = type; }

    public static void writeLog(Activity activity, char type, String message) {
        File logFile = new File(activity.getExternalFilesDir((String) null).getPath() + "/logs.txt");
        try {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.ENGLISH);
            if (logFile.exists()) {
                Writer output = new BufferedWriter(new FileWriter(logFile, true));
                if (type == 'e') {
                    Log.e("LOG", message);
                    output.write("\nERROR: ");
                } else if (type == 'i') {
                    Log.i("LOG", message);
                    output.write("\nINFO: ");
                } else if (type == 'w') {
                    Log.w("LOG", message);
                    output.write("\nWARNING: ");
                }
                output.write(formatForDateNow.format(dateNow) + " - " + message);
                output.flush();
                output.close();
            } else if (logFile.createNewFile()) {
                Writer output2 = new BufferedWriter(new FileWriter(logFile, false));
                if (type == 'e') {
                    output2.write("ERROR: ");
                } else if (type == 'i') {
                    output2.write("INFO: ");
                } else if (type == 'w') {
                    output2.write("WARNING: ");
                }
                output2.write(formatForDateNow.format(dateNow) + " - " + message);
                output2.flush();
                output2.close();
            }
        } catch (IOException e) {
            Log.e("LOG", e.toString());
        }
    }
    public static void writeLog(Context context, char type, String message) {
        File logFile = new File(context.getExternalFilesDir((String) null).getPath() + "/logs.txt");
        try {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.ENGLISH);
            if (logFile.exists()) {
                Writer output = new BufferedWriter(new FileWriter(logFile, true));
                if (type == 'e') {
                    output.write("\nERROR: ");
                } else if (type == 'i') {
                    output.write("\nINFO: ");
                } else if (type == 'w') {
                    output.write("\nWARNING: ");
                }
                output.write(formatForDateNow.format(dateNow) + " - " + message);
                output.flush();
                output.close();
            } else if (logFile.createNewFile()) {
                Writer output2 = new BufferedWriter(new FileWriter(logFile, false));
                if (type == 'e') {
                    output2.write("ERROR: ");
                } else if (type == 'i') {
                    output2.write("INFO: ");
                } else if (type == 'w') {
                    output2.write("WARNING: ");
                }
                output2.write(formatForDateNow.format(dateNow) + " - " + message);
                output2.flush();
                output2.close();
            }
        } catch (IOException e) {
            Log.e("LOG", e.toString());
        }
    }
    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }
    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" ТB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" МB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" КB");
        } else {
            hrSize = dec.format(b).concat(" bytes");
        }
        return hrSize;
    }
    public static void showMessage(String _s, Context context) {
        Toasty.info(context, _s, Toast.LENGTH_LONG).show(); }

    public static boolean isInternetConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) { return true; }
        return false;
    }

    public static boolean isGameInstalled() {
        String str = GAME_PATH;
        File file = new File(str + "/anim/");
        File file2 = new File(str + "/audio/");
        File file3 = new File(str + "/data/");
        File file4 = new File(str + "/models/");
        File file5 = new File(str + "/texdb/");
        File file6 = new File(str + "/SAMP/", "settings.ini");
        return file.exists() && file2.exists() && file3.exists() && file4.exists() && file5.exists() && file6.exists();
    }

    public static void writeLog(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String text = sw.toString();
        writeFile(APP_PATH+"/log.txt", text);
    }

    public static void writeFile(String path, String str)
    {
        File file = new File(path);
        try { if (!file.exists()) file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File(path), false);
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) { e.printStackTrace(); } finally { try { if (fileWriter != null) fileWriter.close(); } catch (IOException e) { e.printStackTrace(); }}
    }
}
