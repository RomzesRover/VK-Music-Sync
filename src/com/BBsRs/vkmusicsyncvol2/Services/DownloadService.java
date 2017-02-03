package com.BBsRs.vkmusicsyncvol2.Services;

import java.io.File;
import java.util.ArrayList;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.CustomEnvironment;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;

public class DownloadService extends Service {
	
	SharedPreferences sPref;
	private final Handler handler = new Handler();
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	boolean stopCurrent = false;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		getApplicationContext().registerReceiver(addSongToDownloadQueue, new IntentFilter(Constants.INTENT_ADD_SONG_TO_DOWNLOAD_QUEUE));
		getApplicationContext().registerReceiver(removeSongFromDownloadQueue, new IntentFilter(Constants.INTENT_REMOVE_SONG_FROM_DOWNLOAD_QUEUE));
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null) == null){
    		sPref.edit().putString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, (new CustomEnvironment(this)).DownloadDirectoryDecide()).commit();
    	}
		
		musicCollection.add((MusicCollection)intent.getParcelableExtra(Constants.INTENT_EXTRA_ONE_AUDIO));
		
		startEachDownload();
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();
		getApplicationContext().unregisterReceiver(addSongToDownloadQueue);
		getApplicationContext().unregisterReceiver(removeSongFromDownloadQueue);
	}
	
	private BroadcastReceiver addSongToDownloadQueue = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			musicCollection.add((MusicCollection)arg1.getParcelableExtra(Constants.INTENT_EXTRA_ONE_AUDIO));
		}
	};
	
	private BroadcastReceiver removeSongFromDownloadQueue = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			MusicCollection songToRemoveFromDownloadQueue = (MusicCollection)arg1.getParcelableExtra(Constants.INTENT_EXTRA_ONE_AUDIO);
			
			int position = 0;
			for (MusicCollection one : musicCollection){
				if (one.aid == songToRemoveFromDownloadQueue.aid && one.owner_id == songToRemoveFromDownloadQueue.owner_id && one.artist.equals(songToRemoveFromDownloadQueue.artist) && one.title.equals(songToRemoveFromDownloadQueue.title) && one.url.contains(songToRemoveFromDownloadQueue.url)){
					musicCollection.remove(position);
					if (position == 0){
						stopCurrent = true;
					}
					break;
				}
				position++;
			}
		}
	};
	
	public void startEachDownload(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				while (true){
					//stop if list is empty
					if (musicCollection.size()==0) break;
					
					//resume download
					stopCurrent = false;
					
					MusicCollection currentDownload = musicCollection.get(0);
					downloadFileFromOneMusicCollection(currentDownload);
					
					//remove from download queue
					musicCollection.remove(currentDownload);
				}
				stopSelf();
			}
		}).start();
	}
	
	public void downloadFileFromOneMusicCollection(MusicCollection musicToDownload){
		
		Log.v("Start Download song", musicToDownload.artist + " - " +musicToDownload.title);
		Log.v("From", musicToDownload.url);
		Log.v("To", sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null));
		
		//check current download directory to availability 
	    File checker = null;
	    try {
    		checker = new File(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null)+"/1.txt");
    		checker.mkdirs();
    		checker.createNewFile();
    		checker.delete();
    	} catch (Exception e){
	    	handler.post(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.download_folder_is_unavailable), Toast.LENGTH_LONG).show();
				}
	    	});
	    	musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
			Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
			sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
			getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
    		stopSelf();
    		return;
    	}
		
	    //start download
		for (int i=0; i<10; i++){
			musicToDownload.isDownloaded += 10;
			try {
				Thread.sleep(750);
				
				//stop download
				if (stopCurrent) {
					//delete downloaded file
					return;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//send percentage to fragment
			Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
			sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
			getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
			
		}
		
		//sleep to show success icon
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//send that song is downloaded
		musicToDownload.isDownloaded = Constants.LIST_ACTION_DELETE;
		
		Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
		sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
		getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
		
	}
}
