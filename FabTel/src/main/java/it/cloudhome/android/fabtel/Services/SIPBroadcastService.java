package it.cloudhome.android.fabtel.Services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
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

    private Context ctx;
    protected SipManager mSipManager = null;
    protected SipProfile mSipProfile;
    protected SipAudioCall call;

    protected SipSession mSip;
    protected IntentFilter filter;

    private boolean wifiActive=false;

    private IncomingCallReceiver callReceiver;

    //region Override Metodi Base
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

    //endregion

    //region Avvio e Termine Servizio
    private void startService()
    {
        CaricaSettings();
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
    //endregion

    //region Invio Messaggi di Notifica
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

    //endregion

    public void sendCommand(String command)
    {
        if(command.equals("ANSWER_CALL"))
        {
            String a ="";
        }
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
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ls = wifiInfo.getLinkSpeed();
        String ssid = wifiInfo.getSSID().toLowerCase();
        if(ssid.contains(SSID.toLowerCase()) && ls >0 )
        {
            if(!wifiActive)
            {
                InitializeSIP();
                doNotify("FabTel WIFI ON", "E' stata rilevata la copertura wifi selezionata il telefono verrà registrato");
            }
            wifiActive=true;
            LanciaApp();
        }
        else
        {
            if(wifiActive)
                doNotify("FabTel WIFI OFF", "E' stata persa la copertura wifi");
            wifiActive=false;
            TerminateSIP();
            this.unregisterReceiver(callReceiver);
        }
    }

    private boolean isActivityRunning() {

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
    private void CaricaSettings(){
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

    //region Gestione SIP
    /************ Gestione SIP Inizio ************/

    protected void InitializeSIP() {
        // TODO Auto-generated method stub
        ctx=this;
        if(mSipManager == null) {
            mSipManager = SipManager.newInstance(this);
        }
        SipProfile.Builder builder;
        try {

            if(useVPN)
                builder = new SipProfile.Builder(username, VPNServer);
            else
                builder = new SipProfile.Builder(username, SIPServer);
            //builder = new SipProfile.Builder("6000", SIPServer);
            //builder = new SipProfile.Builder(username, "192.168.200.106");
            builder.setPassword(password);
            //builder.setPassword("eurostands6000");
            builder.setAuthUserName(username);
            //builder.setAuthUserName("6000");
            builder.setSendKeepAlive(true);
            //builder.setAutoRegistration(true);
            mSipProfile = builder.build();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        filter = new IntentFilter();
        filter.addAction("it.cloudhome.android.fabtel.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        Intent intent = new Intent();
        intent.setAction("it.cloudhome.android.fabtel.INCOMING_CALL");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);

        try {
            mSipManager.open(mSipProfile, pendingIntent, null);
        } catch (SipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            mSipManager.setRegistrationListener(mSipProfile.getUriString(),SIPRlistener);
        } catch (SipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    protected void TerminateSIP()
    {

        if (mSipManager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                mSipManager.close(mSipProfile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("TerminateSIP", "Failed to close local profile.", ee);
        }
    }

    /* Listener x Registrazione e chiamata */
    SipAudioCall.Listener SIPlistener = new SipAudioCall.Listener() {

        @Override
        public void onCallEstablished(SipAudioCall lcall) {
            Log.e("Event", "SIP - Connected");
            lcall.startAudio();
            lcall.setSpeakerMode(true);
            call=lcall;
            /*
            if(ctx instanceof Telefono)
            {
                ((Telefono) ctx).AggiornaStatus("Connected to ");
                ((Telefono) ctx).AggiornaSpeaker();
            }
            */
        }

        @Override
        public void onRingingBack (SipAudioCall lcall){
            //mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            Log.e("Event", "SIP - Ringing " + lcall.toString());
            call=lcall;
        }

        @Override
        public void onCalling(SipAudioCall lcall) {
            //call.startAudio();
            //call.setSpeakerMode(true);
            Log.e("Event", "SIP - Calling " + lcall.toString());
        }
        @Override
        public void onRinging(SipAudioCall lcall, SipProfile caller)
        {
            boolean t = lcall.isInCall();
            t=!t;
        }
        @Override
        public void onError (SipAudioCall lcall, int errorCode, String errorMessage)
        {
            Log.e("Event","ERRORE SIP " + lcall.toString());
            Log.e("Event","ERRORE SIP " + call.toString());

            Log.e("Event", "SIP - Error");
            Log.e("Event", errorMessage);
            call=lcall;
        }

        @Override
        public void onCallEnded(SipAudioCall lcall) {
            // Do something.
            Log.e("Event", "SIP - Call Ended " + lcall.toString());
            lcall.close();
            //ScreenOFF(false);

            try {
                //ad.dismiss();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };


    SipRegistrationListener SIPRlistener= new SipRegistrationListener() {

        public void onRegistering(String localProfileUri) {
            //SIPStato = "Registering with SIP Server...";
            /*
            tvMessage = "Fidoka PBX - SIP: Registering...";
            tvColor = Color.BLUE;
            UIhandler.post(AggiornaTextView);
            */
        }

        @Override
        public void onRegistrationDone(String localProfileUri, long expiryTime) {
            //SIPStato = "Ready";
            int inizio=localProfileUri.indexOf(':');
            int fine=localProfileUri.indexOf('@');
            /*
            if(ctx instanceof Telefono)
            {
                ((Telefono) ctx).SIPStatus=true;
                ((Telefono) ctx).AggiornaSIPStatus();
            }
            */
        }

        public void onRegistrationFailed(String localProfileUri, int errorCode,
                                         String errorMessage) {
            String SIPStato = "Registration failed.  Please check settings.";
            /*
            if(ctx instanceof Telefono)
            {
                ((Telefono) ctx).SIPStatus=false;
                ((Telefono) ctx).AggiornaSIPStatus();
            }
            */
        }
    };

    /************ Gestione SIP Fine ************/
//endregion

}
