package it.cloudhome.android.fabtel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Fabrizio.Clerici on 02/12/13.
 */
public class Rubrica extends ActivitySIP implements AdapterView.OnItemClickListener{

    //region Variabili locali
    private String strSettings,serverAddress,username,password,pbxcode,modo,numeroDest="";
    private ClsUtility Uti=new ClsUtility();
    private ListView list;
    private ProgressDialog pbCarica;
    private SimpleCursorAdapter adapter;
    private String selezione="";

    //private double lat,lon,speed,alt=0;

    private boolean SIP_ENABLED=false;

    CharSequence[] items = { "Centrale Aziendale", "Smartphone" };
    //endregion

    //region Override Metodi Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_rubrica);
        CaricaSettings();
        CaricaContatti();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //GetPosition(view);
        // TODO Auto-generated method stub
        Cursor C = (Cursor) adapter.getItem(position);
        numeroDest  = NormalizzaNumero(C.getString(C.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        modo="placecall";
        /*
        Intent tel=new Intent(this,Telefono.class);
        tel.putExtra("modo",modo);
        tel.putExtra("numero",numeroDest);
        startActivity(tel);
        */

    }

    //endregion

    //region Routines di appoggio

    private String NormalizzaNumero(String numero){
        String result="";
        numero=numero.replace("+39","");
        for (int i = 0; i < numero.length(); i++) {
            if (numero.charAt(i)=="+".charAt(0))
                result+="00";
            if(numero.charAt(i)<58 && numero.charAt(i)>47)
                result+=numero.substring(i,i+1);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private void CaricaContatti(){
        //Caricamento contatti dalla rubrica interna

        EditText txRicerca=(EditText) findViewById(R.id.TxRicerca);
        txRicerca.clearFocus();
        String filtro=null;
        Cursor cursor;
        filtro="DISPLAY_NAME LIKE '%"+ txRicerca.getText()+"%'";
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, filtro, null, "DISPLAY_NAME");

        startManagingCursor(cursor);


        String[] from = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        int[] to = new int[] { R.id.rubricaNome, R.id.rubricaNumero};

        adapter=new SimpleCursorAdapter(
                this,
                R.layout.ute_rubrica, //layout contenente gli id di "to"
                cursor,//sorgente dati
                from,
                to);

        //utilizzo dell'adapter
        ((ListView)findViewById(R.id.rubricaListView)).setAdapter(adapter);
        list = (ListView)findViewById(R.id.rubricaListView);
        list.setOnItemClickListener(this);
    }
    public void RubricaRicarica(View v){
        CaricaContatti();
    }

    private void PlaceCall(){
//
    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    //endregion
}
