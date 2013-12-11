package it.cloudhome.android.fabtel.Services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.cloudhome.android.fabtel.CentraleMain;
import it.cloudhome.android.fabtel.ClsUtility;
import it.cloudhome.android.fabtel.R;
import it.cloudhome.android.fabtel.SplashScreen;

/**
 * Created by Fabrizio.Clerici on 06/12/13.
 */
public class SIPBroadcastService extends Service {

    private Timer timer=new Timer();
    private static final long UPDATE_INTERVAL = 20*1000;
    private int chiamate=0;

    protected String strSettings,serverAddress,username,password,pbxcode,myphone,chatAddress,chatPassword,chatServer,SIPServer,VPNServer,SSID="";
    protected boolean useVPN,autoStart=false;

    private boolean launched=false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // init the service here
        startService();
        doNotify("Avvio Servizio", "Il Servizio è stato Avviato correttamente");
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        shutdownService();
    }
    private void startService()
    {

        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        CheckWifiStatus();
                    }
                },
                0,
                UPDATE_INTERVAL);
        Log.i(getClass().getSimpleName(), "Service started!!!");
    }
    private void shutdownService()
    {
        if (timer != null) timer.cancel();
        doNotify("Avvio Servizio", "Il Servizio è stato Terminato");
        Log.i(getClass().getSimpleName(), "Service stopped!!!");
    }

    private void doNotify(String Title, String Message)
    {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        //Instantiate the Notification:
        int icon = R.drawable.ic_launcher;
        CharSequence tickerText = "FabTel";
        long when = System.currentTimeMillis();

        android.app.Notification notification = new android.app.Notification(icon, tickerText, when);
        notification.defaults |= Notification.DEFAULT_SOUND;    //Suona
        notification.defaults |= Notification.DEFAULT_LIGHTS;   //LED
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        //Define the notification's message and PendingIntent:
        Context context = getApplicationContext();
        CharSequence contentTitle = String.format("FabTel: %s",Title);
        CharSequence contentText = Message;
        Intent notificationIntent = new Intent(this, CentraleMain.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        //Pass the Notification to the NotificationManager:
        final int HELLO_ID = chiamate++;

        mNotificationManager.notify(HELLO_ID, notification);
        if (chiamate>32768)
            chiamate=1;
    }


    //region Verifica Connessione WIFI
    private void LanciaApp()
    {
        if(autoStart)
        {
            Intent i=new Intent(this,SplashScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            boolean p = isActivityRunning();
            if(!p)
            {
                launched=true;
                startActivity(i);
            }
        }
    }
    private void CheckWifiStatus()
    {
        CaricaSettings();
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().toLowerCase();
        if(ssid.contains(SSID.toLowerCase()))
        {
            LanciaApp();
        }
            /*
            else
            {
                //kill Process
                ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                Log.d("Service VextUP","vvvvvvvvv Process 2 Kill vvvvvvvvvv");
                for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
                    if (pid.processName.equals("it.solutions.innext.vextapp"))
                        am.killBackgroundProcesses(pid.processName);
                    Log.d("Service VextUP",pid.processName + "killed");
                    break;
                }
            }
            */
    }

    public boolean isActivityRunning() {

        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (
                    task.baseActivity.getClassName().equalsIgnoreCase("it.cloudhome.android.fabtel.centralemain")||
                            task.baseActivity.getClassName().equalsIgnoreCase("it.cloudhome.android.fabtel.splashscreen")||
                            task.baseActivity.getClassName().equalsIgnoreCase("it.cloudhome.android.fabtel.telefono")||
                            task.baseActivity.getClassName().equalsIgnoreCase("it.cloudhome.android.fabtel.rubrica")
                    )
            {
                return true;
            }
        }

        return false;
        //return isActivityFound;
    }
    protected void CaricaSettings(){
        String strXML="";
        try {
            FileInputStream fis = openFileInput("user_settings.mod");
            int size=fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            strXML=new String(buffer);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            try {
                InputStream is = getResources().openRawResource(R.raw.user_settings);
                int size = is.available();

                // Read the entire resource into a local byte buffer.
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                strXML=new String(buffer);

                //result=XMLGetFirstNodeValue(strXML, chiave);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        strSettings=strXML;
        try {
            ClsUtility Uti=new ClsUtility();
            serverAddress = Uti.XMLGetFirstNodeValue(strSettings, "sipserver");
            username = Uti.XMLGetFirstNodeValue(strSettings, "user");
            password = Uti.XMLGetFirstNodeValue(strSettings, "password");
            pbxcode  = Uti.XMLGetFirstNodeValue(strSettings, "pbxcode");
            //myphone  = Uti.XMLGetFirstNodeValue(strSettings, "myphone");
            SIPServer = Uti.XMLGetFirstNodeValue(strSettings, "sipserver");
            VPNServer=Uti.XMLGetFirstNodeValue(strSettings, "vpnserver").split(":")[0];
            useVPN=Uti.XMLGetFirstNodeValue(strSettings, "usevpn").equals("true");
            autoStart=Uti.XMLGetFirstNodeValue(strSettings, "autostart").equals("true");
            SSID=Uti.XMLGetFirstNodeValue(strSettings, "ssid");
            //=Uti.XMLGetFirstNodeValue(strSettings, "mychatsrv");
            //SIPServer = Uti.XMLGetFirstNodeValue(strSettings, "SIPServer");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //endregion

}
