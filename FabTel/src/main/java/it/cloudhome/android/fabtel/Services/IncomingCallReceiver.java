package it.cloudhome.android.fabtel.Services;

import java.io.IOException;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;

/*** Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    /**
     * Processes the incoming call, answers it, and hands it over to the
     * WalkieTalkieActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
	
	private MediaPlayer mMediaPlayer;
	private Intent i;
	private Context ctx;
	private SipAudioCall.Listener listener;
	private SipAudioCall incomingCall;
	private AlertDialog ad;
	private AlertDialog adAnswer;
	private String CallerID;


	@Override
    public void onReceive(Context context, Intent intent) {
        incomingCall = null;
        i=intent;
        ctx=context;
        /*
        try {
            listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
     		   @Override
    		   public void onCallEnded(SipAudioCall call) {
    		      // Do something.
     			   StopRinging();
     			   	try {
     				   call.endCall();
					} catch (SipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			   call.close();
    			   adAnswer.dismiss();
    			   ad.dismiss();
    		   }
            };
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            Log.d("IncomingCall VextUP","vvvvvvvvv Process 2 Kill vvvvvvvvvv");
            for (ActivityManager.RunningAppProcessInfo pid : am.getRunningAppProcesses()) {
                if (pid.processName.equals("it.solutions.innext.vextapp"))
                    am.moveTaskToFront(pid.pid,ActivityManager.MOVE_TASK_WITH_HOME);
                Log.d("IncomingCall VextUP", pid.processName + "killed");
                break;
            }
            getCall();
            StartRinging();
	        adAnswer = new AlertDialog.Builder(context)
	        .setIcon(android.R.drawable.ic_dialog_dialer)
	        .setTitle("Chiamata SIP in Arrivo da " + CallerID)
	        .setMessage("Vuoi rispondere alla chiamata?")
	        .setPositiveButton("SI", new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {

            //Stop the activity
            Rispondi();
	            }

	        })
	        .setNegativeButton("NO", new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {

	                //Stop the activity
	            	DropCall();
	            }

	        })
	        .show();
            //wtActivity.updateStatus(incomingCall);
        } catch (Exception e) {
            if (incomingCall != null) {
                incomingCall.close();
            }
        }
        */
    }
    /*
    private void DropCall()
    {
        StopRinging();
        ActivitySIP wtActivity = (ActivitySIP) ctx;
        try {
			incomingCall = wtActivity.mSipManager.takeAudioCall(i, listener);
			incomingCall.endCall();
			
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        wtActivity.call = incomingCall;	
    }
    private void Rispondi()
    {
       StopRinging();
       ActivitySIP wtActivity = (ActivitySIP) ctx;
       try {
	        incomingCall.answerCall(30);
	        incomingCall.startAudio();
	        //incomingCall.setSpeakerMode(true);
	        if(incomingCall.isMuted()) {
	            incomingCall.toggleMute();
	        }	        
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       wtActivity.call = incomingCall;    	
       
       ad = new AlertDialog.Builder(ctx)
       .setIcon(android.R.drawable.ic_dialog_dialer)
       .setTitle(String.format("Chiamata con %s",CallerID))
       .setMessage("Terminare la chiamata?")
       .setPositiveButton("SI", new DialogInterface.OnClickListener() {

           @Override
           public void onClick(DialogInterface dialog, int which) {

               //Stop the activity
           	try {
					incomingCall.endCall();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           	incomingCall.close();
           }

       })
       .show();
    }
    private void StartRinging()
    {
        try {
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE); 
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(ctx, alert);
			final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
			           mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			           mMediaPlayer.setLooping(true);
			           mMediaPlayer.prepare();
			           mMediaPlayer.start();
			 }
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    private void StopRinging()
    {
    	try {
			if (mMediaPlayer.isPlaying())
			{
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void getCall()
    {
        ActivitySIP wtActivity = (ActivitySIP) ctx;
        try {
			incomingCall = wtActivity.mSipManager.takeAudioCall(i, listener);
			CallerID=EstraiChiamante(incomingCall.getPeerProfile());
				incomingCall.getPeerProfile().getUriString(); // .getDisplayName();
			
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        wtActivity.call = incomingCall;    	
    }
    private String EstraiChiamante(SipProfile profilo)
    {
    	String Numero=profilo.getUriString();
    	String Nome=profilo.getDisplayName() == null ? "" : profilo.getDisplayName();
    	if (Numero.indexOf("@")>0)
    		Numero=Numero.substring(4,Numero.indexOf("@"));
    	else
    		Numero=Numero.substring(4);
    	if (Nome.length()>0)
    		return String.format("%s - %s", Numero,Nome);
    	else
    		return Numero;
    }
    */
}
