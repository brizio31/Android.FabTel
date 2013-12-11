package it.cloudhome.android.fabtel;

import android.app.Activity;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.cloudhome.android.fabtel.Services.SIPBroadcastService;

public class CentraleMain extends ActivitySIP
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,AdapterView.OnItemClickListener {

    //region Dichiarazione Variabili locali
    private ListView list;
    private ProgressDialog pbCarica;
    private ArrayList<ClsContatto> contactList=new ArrayList<ClsContatto>(); //lista delle persone che la listview visualizzerˆ
    private String modo,internoSelezionato;
    private CharSequence[] items = { "Chiama interno", "Avvia Chat" };
    private String selezione;
    private Context ctx;
    private Connection connection;

    private Intent svc;

    private DBContatto db;

    public SipManager mSipManager = null;
    public SipProfile mSipProfile;
    public SipAudioCall call;

    public SipSession mSip;

    //public IncomingCallReceiver callReceiver;
    //endregion

    //region Variabili locali per gestione Menu
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    //endregion

    //region Override Metodi Activity
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_centrale_main);

        CaricaSettings();
        CaricaLista();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if(!isMyServiceRunning())
        {
            svc = new Intent(this,SIPBroadcastService.class);
            startService(svc);
        }
    }

    //endregion

    //region Routines di appoggio

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SIPBroadcastService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void CaricaLista(){

        db = new DBContatto(getApplicationContext());
        db.open();
        List<ClsContatto> contatti=db.GetCollection(null, null, null);
        db.close();
        //ClsContatto [] Utenti;
        contactList.clear();
        for (ClsContatto clsContatto : contatti)
        {
            contactList.add(clsContatto);
        }

        //Questa � la lista che rappresenta la sorgente dei dati della listview
        //ogni elemento � una mappa(chiave->valore)
        ArrayList<HashMap<String, Object>> data=new ArrayList<HashMap<String,Object>>();


        for(int i=0;i<contactList.size();i++){
            ClsContatto c=contactList.get(i);// per ogni persona all'inteno della lista

            HashMap<String,Object> personMap=new HashMap<String, Object>();//creiamo una mappa di valori
            personMap.put("imgStato", c.getImage()); // per la chiave image, inseriamo la risorsa dell immagine
            personMap.put("nome", c.getNome()); // per la chiave name,l'informazine sul nome
            personMap.put("stato", c.getStato());// per la chiave surnaname, l'informazione sul cognome
            personMap.put("interno", c.getInterno()); // per la chiave name,l'informazine sul nome
            personMap.put("imgPhone", c.getPhoneImage()); // per la chiave image, inseriamo la risorsa dell immagine
            data.add(personMap);  //aggiungiamo la mappa di valori alla sorgente dati


            String[] from={"imgStato","nome","stato","interno","imgPhone"}; //dai valori contenuti in queste chiavi
            int[] to={R.id.contattoImage,R.id.contattoNome,R.id.contattoStato,R.id.contattoInterno,R.id.phoneImage};//agli id delle view

            //costruzione dell adapter
            SimpleAdapter adapter=new SimpleAdapter(
                    getApplicationContext(),
                    data,//sorgente dati
                    R.layout.contatto_interno, //layout contenente gli id di "to"
                    from,
                    to);




                /*
                //Caricamento contatti dalla rubrica interna

                Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, "DISPLAY_NAME LIKE '%GIO%'", null, "DISPLAY_NAME");

                startManagingCursor(cursor);

                String[] from = new String[] { Phone.DISPLAY_NAME, Phone.NUMBER};

                int[] to = new int[] { R.id.contattoNome, R.id.contattoStato};
                SimpleCursorAdapter adapter=new SimpleCursorAdapter(
                        this,
                        R.layout.contatto, //layout contenente gli id di "to"
                        cursor,//sorgente dati
                        from,
                        to);
                */


            //utilizzo dell'adapter
            ((ListView)findViewById(R.id.personListView)).setAdapter(adapter);
            list = (ListView)findViewById(R.id.personListView);
            list.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // TODO Auto-generated method stub
        //Cursor C = (Cursor) adapter.getItem(position);
        String a = list.getItemAtPosition(position).toString();
        int startPos=a.indexOf("interno")+8;
        int endPos=a.indexOf(",", startPos);
        internoSelezionato=a.substring(startPos, endPos);
        modo="placecall";
        Log.d("Fabtel - Chiamata per: ", internoSelezionato);
        //Lancio la chiamata
        //Intent tel=new Intent(this,Telefono.class);
        /*
        tel.putExtra("modo","placecall");
        tel.putExtra("numero",internoSelezionato);
        startActivity(tel);
        */
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Conferma chiusura applicazione")
                    .setMessage("Vuoi uscire da FabTel?")
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(!autoStart)
                            {
                                stopService(svc);
                            }
                            //Stop the activity
                            //TerminateSIP();
                            CentraleMain.this.finish();
                        }

                    })
                    .setNegativeButton("NO", null)
                    .show();
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }

    }
    //endregion

    //region Gestione Menu
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                Intent r=new Intent(this,Rubrica.class);
                startActivity(r);
                mTitle = getString(R.string.title_section1);
                break;
            case 3:
                //this.unregisterReceiver(callReceiver);
                //TerminateSIP();
                mTitle = getString(R.string.title_section3);
                /*
                Intent t=new Intent(this,Telefono.class);
                startActivity(t);
                */
                mTitle = getString(R.string.title_section1);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                Intent i=new Intent(this,ClientConfig.class);
                startActivity(i);
                mTitle = getString(R.string.title_section1);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.centrale_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((CentraleMain) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    //endregion
}
