package it.cloudhome.android.fabtel;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.ParseException;

/**
 * Created by Fabrizio.Clerici on 02/12/13.
 */
public class ActivitySIP extends Activity {

    protected Context ctx;
    protected Connection connection;


    protected ActivitySIP cent;
    protected ActivitySIP tele;

    protected SipManager mSipManager = null;
    protected SipProfile mSipProfile;
    protected SipAudioCall call;

    protected SipSession mSip;
    //protected IncomingCallReceiver callReceiver;
    protected IntentFilter filter;

    protected String strSettings,serverAddress,username,password,pbxcode,myphone,chatAddress,chatPassword,chatServer,SIPServer,VPNServer="";
    protected boolean useVPN,autoStart=false;
    protected ClsUtility Uti=new ClsUtility();


    protected void CaricaSettings(){
        String strXML;
        try {
            FileInputStream fis = openFileInput("user_settings.mod");
            int size=fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            strXML=new String(buffer);
            strSettings=strXML;
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

                strSettings=strXML;
                //result=XMLGetFirstNodeValue(strXML, chiave);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            serverAddress = Uti.XMLGetFirstNodeValue(strSettings, "sipserver");
            username = Uti.XMLGetFirstNodeValue(strSettings, "user");
            password = Uti.XMLGetFirstNodeValue(strSettings, "password");
            pbxcode  = Uti.XMLGetFirstNodeValue(strSettings, "pbxcode");
            //myphone  = Uti.XMLGetFirstNodeValue(strSettings, "myphone");
            SIPServer = Uti.XMLGetFirstNodeValue(strSettings, "sipserver");
            VPNServer=Uti.XMLGetFirstNodeValue(strSettings, "vpnserver").split(":")[0];
            useVPN=Uti.XMLGetFirstNodeValue(strSettings, "usevpn").equals("true");
            autoStart=Uti.XMLGetFirstNodeValue(strSettings, "autostart").equals("true");
            //=Uti.XMLGetFirstNodeValue(strSettings, "mychatsrv");
            //SIPServer = Uti.XMLGetFirstNodeValue(strSettings, "SIPServer");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected boolean vpnEnabled=false;

    /************ Gestione SIP Inizio ************/

    protected void InitializeSIP(Context context) {
        // TODO Auto-generated method stub
        ctx=context;
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
        filter.addAction("android.SipDemo.INCOMING_CALL");
        //callReceiver = new IncomingCallReceiver();
        //this.registerReceiver(callReceiver, filter);

        Intent intent = new Intent();
        intent.setAction("android.SipDemo.INCOMING_CALL");
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

}
