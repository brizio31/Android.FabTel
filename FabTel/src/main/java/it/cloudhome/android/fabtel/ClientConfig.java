package it.cloudhome.android.fabtel;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import it.cloudhome.android.fabtel.Services.SIPBroadcastService;

/**
 * Created by Fabrizio.Clerici on 22/11/13.
 */
public class ClientConfig extends Activity {

    private String strSettings="";
    private DBContatto dbc;
    private DBChatMessage dbm;
    private ClsContatto contatto;

    private ClsUtility Uti=new ClsUtility();

    EditText txUser,txPassword,txCode,txSSID;
    CheckBox chkVPN,chkAttivazione;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_clientconfig);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        txUser=(EditText) findViewById(R.id.vconf_txLogin);
        txPassword=(EditText) findViewById(R.id.vconf_txPassword);
        txCode=(EditText) findViewById(R.id.vconf_txCodice);
        txSSID=(EditText) findViewById(R.id.vconf_txSSID);
        //txNumber=(EditText) findViewById(R.id.vconf_txCellulare);
        chkVPN= (CheckBox) findViewById(R.id.vconf_chkVPN);
        chkAttivazione= (CheckBox) findViewById(R.id.chkAttivazione);

        CaricaSettings();

    }

    public void RichiediConfig(View v)
    {
        if(!ControlloCampi())
            return;

        String indirizzo="http://pbxportalsrv.cloudapp.net/mobileproxy/klp/getandroidconfig.aspx?code=%s&usr=%s&pwd=%s";
        indirizzo=String.format(indirizzo,txCode.getText(),txUser.getText(),txPassword.getText());
        String strXML;

        String rc=Uti.GetWebPage(indirizzo);
        ParseResult(rc);

    }

    private boolean ControlloCampi()
    {
        return true;
    }

    private void CaricaSettings(){
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

        //Carico i valori sull'interfaccia

        txUser.setText(Uti.XMLGetFirstNodeValue(strSettings, "user"));
        txPassword.setText(Uti.XMLGetFirstNodeValue(strSettings, "password"));
        txCode.setText(Uti.XMLGetFirstNodeValue(strSettings, "pbxcode"));
        try {
            txSSID.setText(Uti.XMLGetFirstNodeValue(strSettings, "ssid"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            chkVPN.setChecked(Uti.XMLGetFirstNodeValue(strSettings, "usevpn").equals("true"));
            chkAttivazione.setChecked(Uti.XMLGetFirstNodeValue(strSettings, "autostart").equals("true"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

}

    private void ParseResult(String risultatoRaw)
    {
        String risultato = risultatoRaw.replaceAll( "&([^;]+(?!(?:\\w|;)))", "&amp;$1" );

        //Parsing Dati di Base
        String buffer="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
        buffer += "<settings>\n";
        buffer += String.format("\t<serveraddress>%s</serveraddress>\n","pbxportalsrv.cloudapp.net/mobileproxy/klp");
        buffer += String.format("\t<user>%s</user>\n",Uti.XMLGetFirstNodeValue(risultato, "username"));
        buffer += String.format("\t<password>%s</password>\n",Uti.XMLGetFirstNodeValue(risultato, "password"));
        buffer += String.format("\t<pbxcode>%s</pbxcode>\n",Uti.XMLGetFirstNodeValue(risultato, "fidokacode"));
        //buffer += String.format("\t<myphone>%s</myphone>\n",txNumber.getText().toString());
        buffer += String.format("\t<sipserver>%s</sipserver>\n",Uti.XMLGetFirstNodeValue(risultato, "localipaddress"));
        buffer += String.format("\t<vpnserver>%s</vpnserver>\n",Uti.XMLGetFirstNodeValue(risultato, "vpnaddress"));
        buffer += String.format("\t<masterserver>%s</masterserver>\n",Uti.XMLGetFirstNodeValue(risultato, "masteraddress"));
        if (chkVPN.isChecked())
            buffer += String.format("\t<usevpn>%s</usevpn>\n","true");
        else
            buffer += String.format("\t<usevpn>%s</usevpn>\n","false");
        if (chkAttivazione.isChecked())
        {
            buffer += String.format("\t<autostart>%s</autostart>\n","true");
        }
        else
        {
            buffer += String.format("\t<autostart>%s</autostart>\n","false");
        }
        buffer += String.format("\t<ssid>%s</ssid>\n",txSSID.getText());
        buffer +="</settings>";

        buffer=buffer.replace("&", "&amp;");

        try {
            FileOutputStream fos = openFileOutput("user_settings.mod", Context.MODE_PRIVATE);
            fos.write(buffer.getBytes());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(chkAttivazione.isChecked())
        {
            //Attivo il servizio
            if(!isMyServiceRunning())
            {
                Intent svc = new Intent(this,SIPBroadcastService.class);
                startService(svc);
            }
        }
        else
        {
            //Spengo il servizio
            //kill Process
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            Log.d("Service FabTel", "vvvvvvvvv Process 2 Kill vvvvvvvvvv");
            for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
                Log.d("Service FabTel", pid.processName);
                if (pid.processName.equals("it.cloudhome.android.fabtel"))
                {
                    am.killBackgroundProcesses(pid.processName);
                    Log.d("Service FabTel",pid.processName + " killed");
                    //break;
                }
            }
        }

        //Eliminazione conversazioni dal DB
        dbm = new DBChatMessage(getApplicationContext());
        dbm.open();
        String strSql="DELETE FROM chat_messages";
        dbm.executeSQL(strSql);
        dbm.close();
        //Eliminazione Utenti dal DB
        //Delete Chat History
        dbc = new DBContatto(getApplicationContext());
        dbc.open();
        strSql="DELETE FROM utenti";
        dbc.executeSQL(strSql);
        //Parsing Utenti
        ArrayList<String> Utenti=Uti.XMLGetAllNodeValue(risultato, "user");
        for (String utente : Utenti) {
            utente=utente.replace("\t", "").replace("\n", "|");
            String[] campi=utente.split("\\|");
            String chatAddress="";
            if (campi.length<4)
                chatAddress="xxx"+campi[2];
            else
                chatAddress=campi[3];
            contatto=new ClsContatto(campi[1], campi[2], chatAddress);
            dbc.Insert(contatto);
        }
        dbc.close();
        Toast.makeText(this, "Configurazione Automatica Terminata regolarmente", 800).show();
    }
    private boolean isMyServiceRunning()
        {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SIPBroadcastService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
