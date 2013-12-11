package it.cloudhome.android.fabtel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Fabrizio.Clerici on 06/12/13.
 */
public class SplashScreen extends Activity {

    protected int _splashTime = 2000; // time to display the splash screen in ms
    protected boolean _active=true;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_splash);
        ctx=this;

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    Intent i = new Intent(ctx,CentraleMain.class);
                    startActivity(i);
                }
            }
        };
        splashTread.start();
    }

}
