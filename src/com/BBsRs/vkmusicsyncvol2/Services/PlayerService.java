package com.BBsRs.vkmusicsyncvol2.Services;

import java.util.ArrayList;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;

public class PlayerService extends Service implements OnPreparedListener, OnCompletionListener, OnBufferingUpdateListener, OnErrorListener{
	
	String LOG_TAG = "Player Service";
	
	SharedPreferences sPref;
	private final Handler handler = new Handler();
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	int currentTrack;
	
	private MediaPlayer mediaPlayer;
	int bufferingInMillis = 0;
	int seekToInt = 0;
	boolean canSwitch = false;
	boolean prepared = false;
	boolean lastAction = true;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		//wake lock
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getApplicationContext().getPackageName());
        wl.setReferenceCounted(false);
		wl.acquire();
		
		//enable receivers
		getApplicationContext().registerReceiver(restartPlayer, new IntentFilter(Constants.INTENT_PLAYER_RESTART));
		getApplicationContext().registerReceiver(playPause, new IntentFilter(Constants.INTENT_PLAYER_PLAY_PAUSE));
		getApplicationContext().registerReceiver(next, new IntentFilter(Constants.INTENT_PLAYER_NEXT));
		getApplicationContext().registerReceiver(prev, new IntentFilter(Constants.INTENT_PLAYER_PREV));
		getApplicationContext().registerReceiver(seekChange, new IntentFilter(Constants.INTENT_PLAYER_SEEK_CHANGE));
		getApplicationContext().registerReceiver(requestBackSwitchInfo, new IntentFilter(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO));
		getApplicationContext().registerReceiver(killServiceOnPause, new IntentFilter(Constants.INTENT_PLAYER_KILL_SERVICE_ON_PAUSE));
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			//init prefernces
			sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
	    	musicCollection = intent.getExtras().getParcelableArrayList(Constants.BUNDLE_PLAYER_LIST_COLLECTIONS);
	    	currentTrack = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, 0);
	    	
	    	//strat play music
	    	initMP();
		} catch (Exception e){
			e.printStackTrace();
			stopSelf();
		}
    	
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void onDestroy() {
		super.onDestroy();
		//disable receivers
		getApplicationContext().unregisterReceiver(restartPlayer);
		getApplicationContext().unregisterReceiver(playPause);
		getApplicationContext().unregisterReceiver(next);
		getApplicationContext().unregisterReceiver(prev);
		getApplicationContext().unregisterReceiver(seekChange);
		getApplicationContext().unregisterReceiver(requestBackSwitchInfo);
		getApplicationContext().unregisterReceiver(killServiceOnPause);
		//release player
		releaseMP();
		
		//release wake lock
		if (wl !=null )
			wl.release();
	}
	
	private BroadcastReceiver killServiceOnPause = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!mediaPlayer.isPlaying() && prepared)
	    		stopSelf();
	    }
	};
	
	private BroadcastReceiver requestBackSwitchInfo = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//send back to screen (main info)
			sendBackInfoToScreen(true, false);
	    }
	};
	
	private BroadcastReceiver seekChange = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	//seek to
	    	try {
	    		if (mediaPlayer!=null){
	    			seekToInt=intent.getIntExtra(Constants.INTENT_PLAYER_SEEK_TO, 0);
	    			if (seekToInt == mediaPlayer.getDuration()){
	    				//play next on end of the song
	    				Intent next = new Intent(Constants.INTENT_PLAYER_NEXT);
	    				next.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false);
	    				sendBroadcast(next);
	    			} else {
	    				mediaPlayer.seekTo(seekToInt);
	    			}
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	};
	
	@SuppressLint("Wakelock") 
	private BroadcastReceiver playPause = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	if (mediaPlayer == null)
	    		return;
	    	
	    	if (mediaPlayer.isPlaying()){
	    		mediaPlayer.pause();
	    		if (wl !=null && wl.isHeld())
					wl.release();
	    	} else {
	    		if (wl !=null && !wl.isHeld())
					wl.acquire();
	    		mediaPlayer.start();
	    		startPlayProgressUpdater();
	    	}
	    	
	    	Intent i = new Intent(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE);
			i.putExtra(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS, mediaPlayer.isPlaying() || !prepared);
			sendBroadcast(i);
	    	
	    	canSwitch = false;
	    	handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 525);
	    }
	};
	
	
	private BroadcastReceiver next = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	//determine next pos
	    	if (musicCollection.size()-1<=currentTrack)
	    		currentTrack = 0; 
	    	else
	    		currentTrack++;
	    	
	    	//stop send track info
			handler.removeCallbacks(notification);
			
			lastAction = true;
			
			//strat play music
	    	initMP();
	    	
	    	//send back to screen
			sendBackInfoToScreen(true, intent.getExtras().getBoolean(Constants.INTENT_PLAYER_BACK_SWITCH_FITS));
	    }
	};
	
	private BroadcastReceiver prev = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	
	    	//determine prev pos
	    	if (currentTrack==0)
	    		currentTrack = musicCollection.size()-1; 
	    	else
	    		currentTrack--;
	    	
	    	//stop send track info
			handler.removeCallbacks(notification);
			
			lastAction = false;
			
			//strat play music
	    	initMP();
	    	
	    	//send back to screen
			sendBackInfoToScreen(false, intent.getExtras().getBoolean(Constants.INTENT_PLAYER_BACK_SWITCH_FITS));
	    }
	};
	
	private BroadcastReceiver restartPlayer = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	musicCollection = intent.getExtras().getParcelableArrayList(Constants.BUNDLE_PLAYER_LIST_COLLECTIONS);
	    	currentTrack = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION);
	    	//strat play music
	    	initMP();
	    }
	};
	
	private void sendBackInfoToScreen(boolean direction, boolean fits){
		//play pause status
		Intent i = new Intent(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE);
		i.putExtra(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS, mediaPlayer.isPlaying() || !prepared);
		sendBroadcast(i);
		
		Intent backSwitchInfo = new Intent(Constants.INTENT_PLAYER_BACK_SWITCH_TRACK_INFO);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_DIRECTION, direction);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, fits);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_POSITION, currentTrack);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_SIZE, musicCollection.size());
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_ONE_AUDIO, (Parcelable)musicCollection.get(currentTrack));
		sendBroadcast(backSwitchInfo);
	}
	
	private void initMP(){
		prepared = false;
		
		releaseMP();
		mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(musicCollection.get(currentTrack).url);
	        mediaPlayer.prepareAsync();
	        handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 750);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), getString(R.string.player_error), Toast.LENGTH_LONG).show();
			handler.removeCallbacks(resuming);
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					//
					canSwitch = true;
					
					if (lastAction){
						//play next on end of the song
						Intent next = new Intent(Constants.INTENT_PLAYER_NEXT);
						next.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false);
						sendBroadcast(next);
					} else {
						//play prev on end of the song
						Intent prev = new Intent(Constants.INTENT_PLAYER_PREV);
						prev.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false);
						sendBroadcast(prev);
					}
				}}, 1000);
			e.printStackTrace();
		}
	}
	
	final Runnable resuming = new Runnable(){
		@Override
		public void run() {
			//
			canSwitch = true;
		}
	};
	
	private void releaseMP() {
		//stop send track info
		handler.removeCallbacks(notification);
		
		if (mediaPlayer != null) {
			try {
				mediaPlayer.release();
				mediaPlayer = null;
				//
				canSwitch = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		if (percent == 100 || percent == 0 || mediaPlayer==null || !mediaPlayer.isPlaying()){
			bufferingInMillis=0;
			return;
		}
		bufferingInMillis=(mediaPlayer.getDuration()/100*percent)+seekToInt;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		//play next on end of the song
		Intent next = new Intent(Constants.INTENT_PLAYER_NEXT);
		next.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false);
		sendBroadcast(next);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				if (mediaPlayer != null){
					prepared = true;
					mediaPlayer.start();
					startPlayProgressUpdater();
				}
			}
		}, 500);
	}
	
	public void startPlayProgressUpdater() {
		Intent i = new Intent(Constants.INTENT_UPDATE_PLAYBACK);
		
		if (mediaPlayer==null){
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, 0);
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT_BUFFERING, 0);
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, 0);
			sendBroadcast(i);
			return;
		}
		try {
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, mediaPlayer.getCurrentPosition());
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT_BUFFERING, bufferingInMillis);
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, mediaPlayer.getDuration());
			sendBroadcast(i);
			
		    if (mediaPlayer.isPlaying()) {
		    	handler.removeCallbacks(notification);
		        handler.postDelayed(notification,500);
		    }
		} catch (Exception e){
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, 0);
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT_BUFFERING, 0);
			i.putExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, 0);
			sendBroadcast(i);
			e.printStackTrace();
			return;
		}
	}
	Runnable notification = new Runnable() {
        public void run() {
            startPlayProgressUpdater();
        }
    };

}
