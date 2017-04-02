package com.BBsRs.vkmusicsyncvol2.Services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.ObjectSerializer;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.perm.kate.api.Api;

public class PlayerService extends Service implements OnPreparedListener, OnCompletionListener, OnBufferingUpdateListener, OnErrorListener{
	
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
	
	//notification
	PendingIntent contentIntent, PendingIntentPrevSong, PendingIntentNextSong, PendingIntentPlayPause, PendingDeleteIntent;
	NotificationCompat.Builder mBuilder;
	Notification notification;
	NotificationManager mNotificationManager;
	
	SharedPreferences sPref;
	private final Handler handler = new Handler();
	
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionOriginal = new ArrayList<MusicCollection>();
	int currentTrack;
	int hLength;
	String abTitle;
	
	ArrayList<MusicCollection> musicCollectionToDelete = new ArrayList<MusicCollection>();
	
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
		
    	//init vkapi
	    account.restore(getApplicationContext());
        api=new Api(account.access_token, Constants.CLIENT_ID);
		
		//init notifications
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.INTENT_PLAYER_OPEN_ACTIVITY), 0);        
		PendingIntentPrevSong = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.INTENT_PLAYER_PREV).putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false), 0);
		PendingIntentNextSong = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.INTENT_PLAYER_NEXT).putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, false), 0);
		PendingIntentPlayPause = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.INTENT_PLAYER_PLAY_PAUSE).putExtra(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY), 0);
		PendingDeleteIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(Constants.INTENT_PLAYER_KILL_SERVICE_ON_PAUSE), 0);
		
		//enable receivers
		getApplicationContext().registerReceiver(restartPlayer, new IntentFilter(Constants.INTENT_PLAYER_RESTART));
		getApplicationContext().registerReceiver(playPause, new IntentFilter(Constants.INTENT_PLAYER_PLAY_PAUSE));
		getApplicationContext().registerReceiver(next, new IntentFilter(Constants.INTENT_PLAYER_NEXT));
		getApplicationContext().registerReceiver(prev, new IntentFilter(Constants.INTENT_PLAYER_PREV));
		getApplicationContext().registerReceiver(seekChange, new IntentFilter(Constants.INTENT_PLAYER_SEEK_CHANGE));
		getApplicationContext().registerReceiver(requestBackSwitchInfo, new IntentFilter(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO));
		getApplicationContext().registerReceiver(killServiceOnPause, new IntentFilter(Constants.INTENT_PLAYER_KILL_SERVICE_ON_PAUSE));
		getApplicationContext().registerReceiver(changeRepeat, new IntentFilter(Constants.INTENT_PLAYER_REPEAT));
		getApplicationContext().registerReceiver(changeShuffle, new IntentFilter(Constants.INTENT_PLAYER_SHUFFLE));
		getApplicationContext().registerReceiver(NoisyAudioStreamReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
		getApplicationContext().registerReceiver(startContentActivty, new IntentFilter(Constants.INTENT_PLAYER_OPEN_ACTIVITY));
		getApplicationContext().registerReceiver(isInOwnerList, new IntentFilter(Constants.INTENT_IS_IN_OWNERS_LIST_ACTION));
		getApplicationContext().registerReceiver(isDownloaded, new IntentFilter(Constants.INTENT_IS_DOWNLOADED_ACTION));
		getApplicationContext().registerReceiver(changeSongDownloadPercentage, new IntentFilter(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE));
	}
	
	@SuppressWarnings("unchecked")
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//create audio focus things
		afListenerSound = new AFListener();
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		int result = am.requestAudioFocus(afListenerSound,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
		
		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			//no access granted!!!
			stopSelf();
		}
		
		try {
			//init prefernces
			sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			//null list
			musicCollection = new ArrayList<MusicCollection>();
			
			hLength = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_LIST_CURR_HLENGTH, 0);
			currentTrack = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, 0);
	    	abTitle = intent.getExtras().getString(Constants.BUNDLE_LIST_TITLE_NAME);
	    	//retrive music list from shared prefs
			for (int iteration = 0; iteration <= hLength; iteration++){
				android.content.SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_PLAYER_LIST_COLLECTIONS + iteration, Context.MODE_PRIVATE);
				musicCollection.addAll((ArrayList<MusicCollection>) ObjectSerializer.deserialize(prefs.getString(Constants.PREFERENCES_PLAYER_LIST_COLLECTIONS, ObjectSerializer.serialize(new ArrayList<MusicCollection>()))));
			}
			
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
		getApplicationContext().unregisterReceiver(changeRepeat);
		getApplicationContext().unregisterReceiver(changeShuffle);
		getApplicationContext().unregisterReceiver(NoisyAudioStreamReceiver);
		getApplicationContext().unregisterReceiver(startContentActivty);
		getApplicationContext().unregisterReceiver(isInOwnerList);
		getApplicationContext().unregisterReceiver(isDownloaded);
		getApplicationContext().unregisterReceiver(changeSongDownloadPercentage);
		
		//release player
		releaseMP();
		
		// Abandon audio focus when playback complete    
		if (am!=null && afListenerSound!=null)
			am.abandonAudioFocus(afListenerSound);
		
		//delete selected files
		if (!musicCollectionToDelete.isEmpty()){
			//update all music lists
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, true).commit();
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, true).commit();
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, true).commit();
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, true).commit();
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, true).commit();
			//delete music wich user decided
			for (MusicCollection AudioToDeleteFromStorage : musicCollectionToDelete){
				File f = new File(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, "")+"/"+(AudioToDeleteFromStorage.artist+" - "+AudioToDeleteFromStorage.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
				if (f.exists()) f.delete();
			}
		}
		
		//stop foreground
		mNotificationManager.cancel(Constants.NOTIFICATION_PLAYER);
		
		//release wake lock
		if (wl !=null )
			wl.release();
	}
	
	private void showNotification(){
		if(mediaPlayer == null)
			return;
		
		//create new
		mBuilder = new NotificationCompat.Builder(this);
		
		boolean nougat = Build.VERSION.SDK_INT >= 24;
		
		mBuilder.setContentTitle(musicCollection.get(currentTrack).artist)
		.setContentText(musicCollection.get(currentTrack).title)
		.setSmallIcon(mediaPlayer.isPlaying() || !prepared ? R.drawable.ic_music_notification_small_icon : R.drawable.ic_music_notification_pause_small_icon)
		.setContentIntent(contentIntent)
		.addAction(R.drawable.ic_not_prev, nougat? getString(R.string.player_prev_short_not) : "", PendingIntentPrevSong)
		.addAction(!mediaPlayer.isPlaying() && prepared ? R.drawable.ic_not_play : R.drawable.ic_not_pause, !mediaPlayer.isPlaying() && prepared ? nougat? getString(R.string.player_play_short_not) : "" : nougat? getString(R.string.player_pause_short_not) : "", PendingIntentPlayPause)
		.addAction(R.drawable.ic_not_next, nougat? getString(R.string.player_next_short_not) : "", PendingIntentNextSong)
		.setOngoing(mediaPlayer.isPlaying() || !prepared)
		.setAutoCancel(false)
		.setProgress(0, 0, false)
		.setPriority(NotificationCompat.PRIORITY_MAX);
		
		//show not
		notification = mBuilder.build();
		//set ticker
		notification.tickerText = getString(R.string.player_now)+" "+musicCollection.get(currentTrack).artist+" - "+musicCollection.get(currentTrack).title;
		notification.deleteIntent = PendingDeleteIntent;
		
		mNotificationManager.notify(Constants.NOTIFICATION_PLAYER, notification);
	}
	
	AudioManager am;
	AFListener afListenerSound;
	
	class AFListener implements OnAudioFocusChangeListener{
		
		public int volume = -1;
		
		@Override
		public void onAudioFocusChange(int focus) {
			switch (focus) {
			case AudioManager.AUDIOFOCUS_LOSS:
				//opened other music or video app, focus loss
				stopSelf();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				//focus loss transient: phone call, etc, just pause music and resume on gain
				Intent pause = new Intent(Constants.INTENT_PLAYER_PLAY_PAUSE);
	        	pause.putExtra(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_PAUSE_ONLY);
				sendBroadcast(pause);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lower the volume notification, etc
				if (am!=null){
					volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
					//30 percent of current volume
					am.setStreamVolume(AudioManager.STREAM_MUSIC, volume*30/100, 0);
		    	  }
				break;
			case AudioManager.AUDIOFOCUS_GAIN:
				// Resume playback 
				Intent play = new Intent(Constants.INTENT_PLAYER_PLAY_PAUSE);
	        	play.putExtra(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_PLAY_ONLY);
				sendBroadcast(play);
				
				// Raise volume back to normal
				if (am!=null && volume != -1)
					am.setStreamVolume(AudioManager.STREAM_MUSIC, volume == -1 ? am.getStreamVolume(AudioManager.STREAM_MUSIC) : volume, 0);
				break;
			}
		}
	}
	
	private BroadcastReceiver startContentActivty = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//user decide to open player activity
	    	startActivity(new Intent(getApplicationContext(), ContentActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	    	//try to open player fragment after open activity
	    	handler.postDelayed(new Runnable(){
				@Override
				public void run() {
					Intent openPlayerFragment = new Intent(Constants.INTENT_PLAYER_OPEN_ACTIVITY_PLAYER_FRAGMENT);
					
					int cr = currentTrack;
					if (!musicCollectionOriginal.isEmpty()){
						int index=0;
						for (MusicCollection one : musicCollectionOriginal){
							if (one.aid == musicCollection.get(currentTrack).aid && one.owner_id == musicCollection.get(currentTrack).owner_id && one.artist.equals(musicCollection.get(currentTrack).artist) && one.title.equals(musicCollection.get(currentTrack).title)){
								cr=index;
								break;
							}
							index++;
						}
					}
					
					//create bundle to player list
					Bundle playerBundle  = new Bundle();
					playerBundle.putInt(Constants.BUNDLE_PLAYER_LIST_CURR_HLENGTH, hLength);
					playerBundle.putInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, cr);
					playerBundle.putInt(Constants.BUNDLE_PLAYER_LIST_SIZE, musicCollection.size());
					playerBundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, abTitle);
					
					openPlayerFragment.putExtras(playerBundle);
					
					sendBroadcast(openPlayerFragment);
				}
	    	}, 500);
	    }
	};
	
	private BroadcastReceiver NoisyAudioStreamReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
	        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
	        	// Pause playback, on headphones out
	        	Intent pause = new Intent(Constants.INTENT_PLAYER_PLAY_PAUSE);
	        	pause.putExtra(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_PAUSE_ONLY);
				sendBroadcast(pause);
	        }
	    }
	};
	
	private BroadcastReceiver changeSongDownloadPercentage = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			final MusicCollection audioToChangeDownloadPercentage = arg1.getParcelableExtra(Constants.INTENT_EXTRA_ONE_AUDIO);
			Log.v("here", audioToChangeDownloadPercentage.isDownloaded+"");
			
			if (musicCollection != null){
				int position = 0;
				for (MusicCollection one : musicCollection){
					if (one.aid == audioToChangeDownloadPercentage.aid && one.owner_id == audioToChangeDownloadPercentage.owner_id && one.artist.equals(audioToChangeDownloadPercentage.artist) && one.title.equals(audioToChangeDownloadPercentage.title)){
						one.url = audioToChangeDownloadPercentage.url;
						one.isDownloaded = audioToChangeDownloadPercentage.isDownloaded;

						if (position == currentTrack){
							Intent b = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_DOWNLOADED);
							b.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_DOWNLOADED_STATUS, musicCollection.get(currentTrack).isDownloaded);
							sendBroadcast(b);
						}
					}
					position++;
				}
			}
		}
	};
	
	private BroadcastReceiver isDownloaded = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	
	    	canSwitch = false;
	    	handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 525);
	    	
			switch (musicCollection.get(currentTrack).isDownloaded){
			case Constants.LIST_ACTION_DOWNLOAD:
				musicCollection.get(currentTrack).isDownloaded = Constants.LIST_ACTION_DOWNLOAD_STARTED;
				
				//delete song from deleteList
				int index = 0;
				for (MusicCollection one : musicCollectionToDelete){
					if (one.aid == musicCollection.get(currentTrack).aid && one.owner_id == musicCollection.get(currentTrack).owner_id && one.artist.equals(musicCollection.get(currentTrack).artist) && one.title.equals(musicCollection.get(currentTrack).title)){
						musicCollectionToDelete.remove(index);
						break;
					}
					index++;
				}
				
				try {
					if (!isMyServiceRunning(DownloadService.class)){
						//start service with audio in Intent
						Intent startDownload = new Intent(getApplicationContext(), DownloadService.class);
						startDownload.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicCollection.get(currentTrack));
						startService(startDownload);
					} else {
						Intent addSongToDownloadQueue = new Intent(Constants.INTENT_ADD_SONG_TO_DOWNLOAD_QUEUE);
						addSongToDownloadQueue.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicCollection.get(currentTrack));
						sendBroadcast(addSongToDownloadQueue);
					}
				} finally{
					Intent b = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_DOWNLOADED);
					b.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_DOWNLOADED_STATUS, musicCollection.get(currentTrack).isDownloaded);
					sendBroadcast(b);
				}
				break;
			case Constants.LIST_ACTION_DELETE:
				musicCollection.get(currentTrack).isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
				
				musicCollectionToDelete.add(musicCollection.get(currentTrack));
				
				Intent b = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_DOWNLOADED);
				b.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_DOWNLOADED_STATUS, musicCollection.get(currentTrack).isDownloaded);
				sendBroadcast(b);
				break;
			default:
				//update here, cuz we won't use fragment 
				musicCollection.get(currentTrack).isDownloaded = Constants.LIST_ACTION_DOWNLOAD;

				Intent b1 = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_DOWNLOADED);
				b1.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_DOWNLOADED_STATUS, musicCollection.get(currentTrack).isDownloaded);
				sendBroadcast(b1);
				
				Intent removeSongFromDownloadQueue = new Intent(Constants.INTENT_REMOVE_SONG_FROM_DOWNLOAD_QUEUE);
				removeSongFromDownloadQueue.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicCollection.get(currentTrack));
				context.sendBroadcast(removeSongFromDownloadQueue);
				break;
			}
	    }
	};
	
	private BroadcastReceiver isInOwnerList = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	
	    	canSwitch = false;
	    	handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 525);
			
			switch (musicCollection.get(currentTrack).isInOwnerList){
			case Constants.LIST_ACTION_ADD: case Constants.LIST_ACTION_RESTORE:
				new Thread (new Runnable(){
					@Override
					public void run() {
						try {
							long oid = musicCollection.get(currentTrack).owner_id != -1 ? musicCollection.get(currentTrack).owner_id : account.user_id;
							if (musicCollection.get(currentTrack).isInOwnerList == Constants.LIST_ACTION_RESTORE)
								api.restoreAudio(musicCollection.get(currentTrack).aid, oid, null, null);
							else
								api.addAudio(musicCollection.get(currentTrack).aid, oid, null, null, null);
							//notify list with v sign
							handler.post(new Runnable(){
								@Override
								public void run() {
									if (musicCollection.get(currentTrack).isInOwnerList == Constants.LIST_ACTION_RESTORE)
										musicCollection.get(currentTrack).isInOwnerList = Constants.LIST_ACTION_REMOVE;
									else {
										musicCollection.get(currentTrack).isInOwnerList = Constants.LIST_ACTION_ADDED;
										//force update owner list
										sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, true).commit();
									}
									Intent a = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_IN_OWNERS_LIST);
									a.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_IN_OWNERS_LIST_STATUS, musicCollection.get(currentTrack).isInOwnerList);
									sendBroadcast(a);
								}
							});
						} catch (Exception e){
							e.printStackTrace();
							handler.post(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), String.format(getString(R.string.content_activity_error_on_adding_to_owner_list), musicCollection.get(currentTrack).artist + " - " + musicCollection.get(currentTrack).title), Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}).start();
				break;
			case Constants.LIST_ACTION_REMOVE:
				new Thread (new Runnable(){
					@Override
					public void run() {
						try {
							long oid = musicCollection.get(currentTrack).owner_id != -1 ? musicCollection.get(currentTrack).owner_id : account.user_id;
							api.deleteAudio(musicCollection.get(currentTrack).aid, oid);
							//notify list with + sign
							handler.post(new Runnable(){
								@Override
								public void run() {
									musicCollection.get(currentTrack).isInOwnerList = Constants.LIST_ACTION_RESTORE;
									//force update owner list
									sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, true).commit();
									
									Intent a = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_IN_OWNERS_LIST);
									a.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_IN_OWNERS_LIST_STATUS, musicCollection.get(currentTrack).isInOwnerList);
									sendBroadcast(a);
								}
							});
						} catch (Exception e){
							e.printStackTrace();
							handler.post(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), String.format(getString(R.string.content_activity_error_on_remove_from_owner_list), musicCollection.get(currentTrack).artist + " - " + musicCollection.get(currentTrack).title), Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}).start();
				break;
			}
	    }
	};
	
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
	    			if (seekToInt == mediaPlayer.getDuration() && ! mediaPlayer.isLooping()){
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
	
	private BroadcastReceiver changeShuffle = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	if (mediaPlayer == null)
	    		return;
	    	
	    	if (musicCollectionOriginal.isEmpty()){
	    		musicCollectionOriginal = new ArrayList<MusicCollection>();
	    		musicCollectionOriginal.addAll(musicCollection);
	    		Collections.shuffle(musicCollection, new Random(System.currentTimeMillis()));
	    	}
	    	else {
	    		//determine new position in list
	    		int index=0;
    			for (MusicCollection one : musicCollectionOriginal){
    				if (one.aid == musicCollection.get(currentTrack).aid && one.owner_id == musicCollection.get(currentTrack).owner_id && one.artist.equals(musicCollection.get(currentTrack).artist) && one.title.equals(musicCollection.get(currentTrack).title)){
    					currentTrack = index;
    					break;
    				}
    				index++;
    			}
    			
	    		musicCollection = new ArrayList<MusicCollection>();
	    		musicCollection.addAll(musicCollectionOriginal);
	    		musicCollectionOriginal = new ArrayList<MusicCollection>();
	    	}

	    	Intent i3 = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_SHUFFLE);
			i3.putExtra(Constants.INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS, !musicCollectionOriginal.isEmpty());
			sendBroadcast(i3);
	    	
	    	canSwitch = false;
	    	handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 525);
	    }
	};
	
	private BroadcastReceiver changeRepeat = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if (!canSwitch)
	    		return;
	    	if (mediaPlayer == null)
	    		return;
	    	
	    	mediaPlayer.setLooping(!mediaPlayer.isLooping());
	    	
	    	Intent i = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_REPEAT);
			i.putExtra(Constants.INTENT_PLAYER_PLAYBACK_REPEAT_STATUS, mediaPlayer.isLooping());
			sendBroadcast(i);
	    	
	    	canSwitch = false;
	    	handler.removeCallbacks(resuming);
			handler.postDelayed(resuming, 525);
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
	    		if (intent.getExtras().getInt(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY) == Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_PAUSE_ONLY ||
	    				intent.getExtras().getInt(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY) == Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY){
		    		mediaPlayer.pause();
		    		if (wl !=null && wl.isHeld())
						wl.release();
	    		}
	    	} else {
	    		if (intent.getExtras().getInt(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY) == Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_PLAY_ONLY ||
		    			intent.getExtras().getInt(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY) == Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY){
		    		if (wl !=null && !wl.isHeld())
		    			wl.acquire();
		    		mediaPlayer.start();
		    		startPlayProgressUpdater();
	    		}
	    	}
	    	
	    	Intent i = new Intent(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE);
			i.putExtra(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS, mediaPlayer.isPlaying() || !prepared);
			sendBroadcast(i);
			
			//set current notification
	        showNotification();
	    	
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
			handler.removeCallbacks(updatePlayback);
			
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
			handler.removeCallbacks(updatePlayback);
			
			lastAction = false;
			
			//strat play music
	    	initMP();
	    	
	    	//send back to screen
			sendBackInfoToScreen(false, intent.getExtras().getBoolean(Constants.INTENT_PLAYER_BACK_SWITCH_FITS));
	    }
	};
	
	private BroadcastReceiver restartPlayer = new BroadcastReceiver() {
	    @SuppressWarnings("unchecked")
		@Override
	    public void onReceive(Context context, Intent intent) {
	    	//null new list
	    	ArrayList<MusicCollection> musicCollectionNew = new ArrayList<MusicCollection>();
	    	
	    	int currentTrackNew = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION);
	    	abTitle = intent.getExtras().getString(Constants.BUNDLE_LIST_TITLE_NAME);
	    	hLength = intent.getExtras().getInt(Constants.BUNDLE_PLAYER_LIST_CURR_HLENGTH, 0);
	    	
	    	//retrive music list from shared prefs
			for (int iteration = 0; iteration <= hLength; iteration++){
				try {
					android.content.SharedPreferences prefs = getSharedPreferences(Constants.PREFERENCES_PLAYER_LIST_COLLECTIONS + iteration, Context.MODE_PRIVATE);
					musicCollectionNew.addAll((ArrayList<MusicCollection>) ObjectSerializer.deserialize(prefs.getString(Constants.PREFERENCES_PLAYER_LIST_COLLECTIONS, ObjectSerializer.serialize(new ArrayList<MusicCollection>()))));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	    	
	    	if (musicCollectionNew.size() != musicCollection.size() || 
	    			!musicCollection.get(currentTrack).artist.equals(musicCollectionNew.get(currentTrackNew).artist) || 
	    			!musicCollection.get(currentTrack).title.equals(musicCollectionNew.get(currentTrackNew).title)){
	    		
	    		//destroy shuffle
	    		musicCollectionOriginal = new ArrayList<MusicCollection>();
	    		
	    		musicCollection = musicCollectionNew;
		    	currentTrack = currentTrackNew;
		    	//strat play music
		    	initMP();
	    	} 
	    }
	};
	
	private void sendBackInfoToScreen(boolean direction, boolean fits){
		//set current notification
        showNotification();
		
		int cr = currentTrack;
		if (!musicCollectionOriginal.isEmpty()){
			int index=0;
			for (MusicCollection one : musicCollectionOriginal){
				if (one.aid == musicCollection.get(currentTrack).aid && one.owner_id == musicCollection.get(currentTrack).owner_id && one.artist.equals(musicCollection.get(currentTrack).artist) && one.title.equals(musicCollection.get(currentTrack).title)){
					cr=index;
					break;
				}
				index++;
			}
		}
		
		Intent backSwitchInfo = new Intent(Constants.INTENT_PLAYER_BACK_SWITCH_TRACK_INFO);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_DIRECTION, direction);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, fits);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_POSITION, cr);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_SIZE, musicCollection.size());
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_LIST_TITLE_NAME, abTitle);
		backSwitchInfo.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_ONE_AUDIO, (Parcelable)musicCollection.get(currentTrack));
		sendBroadcast(backSwitchInfo);
		
		Intent a = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_IN_OWNERS_LIST);
		a.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_IN_OWNERS_LIST_STATUS, musicCollection.get(currentTrack).isInOwnerList);
		sendBroadcast(a);
		
		Intent b = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_IS_DOWNLOADED);
		b.putExtra(Constants.INTENT_PLAYER_PLAYBACK_IS_DOWNLOADED_STATUS, musicCollection.get(currentTrack).isDownloaded);
		sendBroadcast(b);
		
		if (mediaPlayer == null)
			return;
		
		//play pause status
		Intent i = new Intent(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE);
		i.putExtra(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS, mediaPlayer.isPlaying() || !prepared);
		sendBroadcast(i);
		
		Intent i2 = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_REPEAT);
		i2.putExtra(Constants.INTENT_PLAYER_PLAYBACK_REPEAT_STATUS, mediaPlayer.isLooping());
		sendBroadcast(i2);
		
		Intent i3 = new Intent(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_SHUFFLE);
		i3.putExtra(Constants.INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS, !musicCollectionOriginal.isEmpty());
		sendBroadcast(i3);
	}
	
	private void initMP(){
		boolean looping = false;
		if (mediaPlayer != null) looping = mediaPlayer.isLooping();
		prepared = false;
		
		releaseMP();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setLooping(looping);
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
		handler.removeCallbacks(updatePlayback);
		
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
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (what==-38 && extra == 0)
			return false;
		Toast.makeText(getApplicationContext(), getString(R.string.player_error), Toast.LENGTH_LONG).show();
		
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				handler.removeCallbacks(updatePlayback);
				Intent openLastFragment = new Intent(Constants.INTENT_PLAYER_OPEN_ACTIVITY_LAST_FRAGMENT);
				sendBroadcast(openLastFragment);
				
				stopSelf();
			}
		}, 1000);
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
		    	handler.removeCallbacks(updatePlayback);
		        handler.postDelayed(updatePlayback,500);
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
	Runnable updatePlayback = new Runnable() {
        public void run() {
            startPlayProgressUpdater();
        }
    };
    
	private boolean isMyServiceRunning(Class<?> serviceClass) {			//returns true is service running
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
