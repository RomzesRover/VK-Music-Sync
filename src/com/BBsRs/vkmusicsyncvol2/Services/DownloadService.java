package com.BBsRs.vkmusicsyncvol2.Services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.StatFs;
import android.util.Log;

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.CustomEnvironment;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DownloadService extends Service {
	
	SharedPreferences sPref;
	private final Handler handler = new Handler();
	
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	boolean stopCurrent = false;

	@Override
	public IBinder onBind(Intent arg0) {
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
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
	public void downloadFileFromOneMusicCollection(final MusicCollection musicToDownload){
		
		File downloadFile = null;
		long startTime = System.currentTimeMillis();
		
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
	    try {
	           File dir = new File (sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null)+"/");               

	           if(dir.exists()==false) {
	                dir.mkdirs();
	           }

	           downloadFile = new File(dir, (musicToDownload.artist+" - "+musicToDownload.title).replaceAll("[\\/:*?\"<>|]", ""));
	           
	           //if part downloaded file exist delete it !
	           if (downloadFile.exists())
	        	   downloadFile.delete();
	           
	           //if music already exists return true
	           if (new File(downloadFile.getAbsolutePath()+".mp3").exists()){
	        	   	//send that song is downloaded
		       		musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOADED;
		       		
		       		Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
		       		sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
		       		getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
	        	   
		       		//sleep to show success icon
		       		try {
		       			Thread.sleep(1000);
		       		} catch (InterruptedException e) {
		       			e.printStackTrace();
		       		}
		       		
		       		//send that song is downloaded
		       		musicToDownload.isDownloaded = Constants.LIST_ACTION_DELETE;
		       		musicToDownload.url = downloadFile.getAbsolutePath();
		       		
		       		sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
		       		sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
		       		getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
		       		return;
	           }
	           
	           //check for right link
	           if (musicToDownload.url == null || musicToDownload.url.length()<1){
		   	    	handler.post(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), String.format(getApplicationContext().getString(R.string.download_rightholders_error), musicToDownload.artist + " - " +musicToDownload.title), Toast.LENGTH_LONG).show();
						}
			    	});
			    	musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
					Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
					sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
					getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
		    		return;
	           }
	           
	           //Start download Logging
	           Log.d("DownloadService", "Start Download song: " + musicToDownload.artist + " - " +musicToDownload.title);
	           Log.d("DownloadService", "From: " + musicToDownload.url);
	           Log.d("DownloadService", "To: " + sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null));
	           
	           //create a connection and determine file size
	           URL url = new URL(musicToDownload.url);
	           /* Open a connection to that URL. */
	           URLConnection conexion = url.openConnection();
	           conexion.connect();
	           int lenghtOfFile = conexion.getContentLength();
	       	   Log.d("DownloadService", "Lenght of file: " + lenghtOfFile);
	           
	       	   //check for available space
	       	   StatFs stat = new StatFs(dir.getPath());
	       	   long sdAvailSize = 0;
	       	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
	       		   sdAvailSize = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
	       	   } else {
	       		   sdAvailSize = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
	       	   }
	       	   if ((long)lenghtOfFile*2 > sdAvailSize){
		   	    	handler.post(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.download_no_free_space) + " " + musicToDownload.artist + " - " +musicToDownload.title, Toast.LENGTH_LONG).show();
						}
			    	});
			    	musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
					Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
					sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
					getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
		    		return;
	       	   }
	       	   
	       	   //define inputstream and start download
	       	   InputStream input = new BufferedInputStream(url.openStream());
	       	   OutputStream output = new FileOutputStream(downloadFile);
	       	   //to calculate download block
	       	   byte data[] = new byte[1024];
	       	   int count, last = 0;
	       	   long total=0;
	       	   
	       	   while ((count = input.read(data)) != -1) {
	       		   //check if user cancel download
	       		   if (stopCurrent){
	       			   Log.d("DownloadService", "stop download");
	       			   
	       			   //stop current download all is ok!
	    	       	   output.flush();
	    	       	   output.close();
	    	       	   input.close();
	    	       	   
	       			   if (downloadFile.exists())
	       				   downloadFile.delete();
	       			   return;
	       		   }
	       		   total += count;
	       		   if (((int)((total*90)/lenghtOfFile))-last >= 10){
	       			   last = ((int)((total*90)/lenghtOfFile));
	       			   
	       			   musicToDownload.isDownloaded = last;
	       			   //send percentage to fragment
	       			   Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
	       			   sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
	       			   getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
	       		   }
	       		   output.write(data, 0, count);
	       	   }
	       	   
	       	   //stop current download all is ok!
	       	   output.flush();
	       	   output.close();
	       	   input.close();
	       	   
		       try {
			       Mp3File mp3file = new Mp3File(downloadFile.getAbsolutePath());
			        
			       Log.d("DownloadService", "download cover art");
			       //download bitmap from web
			       Bitmap bmp = ImageLoader.getInstance().loadImageSync(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(musicToDownload.artist+" - "+musicToDownload.title, "UTF-8"), 1);
			       if (bmp==null) 
			    	   bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.music_stub_source);
			          
			       Log.d("DownloadService", "compress bitmap to png");
			       ByteArrayOutputStream stream = new ByteArrayOutputStream();
			       bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				
			       Log.d("DownloadService", "setting tags with cover art");
				
			       Log.d("DownloadService", "create new tags");
			       ID3v2 id3v2Tag = new ID3v24Tag();
			       try {
			    	   if (mp3file.hasId3v2Tag()){
			    		   Log.d("DownloadService", "setting up new tags from existing tags, if they are correct");
			    		   if (mp3file.getId3v2Tag().getAlbum() !=null && !mp3file.getId3v2Tag().getAlbum().contains("vk.com"))
			    			   id3v2Tag.setAlbum(mp3file.getId3v2Tag().getAlbum());
			    		   if (mp3file.getId3v2Tag().getAlbumArtist() !=null && !mp3file.getId3v2Tag().getAlbumArtist().contains("vk.com"))
			    			   id3v2Tag.setAlbumArtist(mp3file.getId3v2Tag().getAlbumArtist());
			    		   if (mp3file.getId3v2Tag().getArtist() !=null && !mp3file.getId3v2Tag().getArtist().contains("vk.com"))
			    			   id3v2Tag.setArtist(mp3file.getId3v2Tag().getArtist());
			    		   if (mp3file.getId3v2Tag().getChapters() !=null && !mp3file.getId3v2Tag().getChapters().contains("vk.com"))
			    			   id3v2Tag.setChapters(mp3file.getId3v2Tag().getChapters());
			    		   if (mp3file.getId3v2Tag().getChapterTOC() !=null && !mp3file.getId3v2Tag().getChapterTOC().contains("vk.com"))
			    			   id3v2Tag.setChapterTOC(mp3file.getId3v2Tag().getChapterTOC());
			    		   if (mp3file.getId3v2Tag().getComment() !=null && !mp3file.getId3v2Tag().getComment().contains("vk.com"))
			    			   id3v2Tag.setComment(mp3file.getId3v2Tag().getComment());
			    		   if (mp3file.getId3v2Tag().getComposer() !=null && !mp3file.getId3v2Tag().getComposer().contains("vk.com"))
			    			   id3v2Tag.setComposer(mp3file.getId3v2Tag().getComposer());
			    		   if (mp3file.getId3v2Tag().getCopyright() !=null && !mp3file.getId3v2Tag().getCopyright().contains("vk.com"))
			    			   id3v2Tag.setCopyright(mp3file.getId3v2Tag().getCopyright());
			    		   if (mp3file.getId3v2Tag().getEncoder() !=null && !mp3file.getId3v2Tag().getEncoder().contains("vk.com"))
			    			   id3v2Tag.setEncoder(mp3file.getId3v2Tag().getEncoder());
//			    		   if (mp3file.getId3v2Tag().getGenre() !=null)
			    			   id3v2Tag.setGenre(mp3file.getId3v2Tag().getGenre());
			    		   if (mp3file.getId3v2Tag().getGenreDescription() !=null && !mp3file.getId3v2Tag().getGenreDescription().contains("vk.com"))
			    			   id3v2Tag.setGenreDescription(mp3file.getId3v2Tag().getGenreDescription());
			    		   if (mp3file.getId3v2Tag().getItunesComment() !=null && !mp3file.getId3v2Tag().getItunesComment().contains("vk.com"))
			    			   id3v2Tag.setItunesComment(mp3file.getId3v2Tag().getItunesComment());
			    		   if (mp3file.getId3v2Tag().getOriginalArtist() !=null && !mp3file.getId3v2Tag().getOriginalArtist().contains("vk.com"))
			    			   id3v2Tag.setOriginalArtist(mp3file.getId3v2Tag().getOriginalArtist());
//			    		   if (mp3file.getId3v2Tag().getPadding() !=null)
			    			   id3v2Tag.setPadding(mp3file.getId3v2Tag().getPadding());
			    		   if (mp3file.getId3v2Tag().getPartOfSet() !=null && !mp3file.getId3v2Tag().getPartOfSet().contains("vk.com"))
			    			   id3v2Tag.setPartOfSet(mp3file.getId3v2Tag().getPartOfSet());
			    		   if (mp3file.getId3v2Tag().getPublisher() !=null && !mp3file.getId3v2Tag().getPublisher().contains("vk.com"))
			    			   id3v2Tag.setPublisher(mp3file.getId3v2Tag().getPublisher());
			    		   if (mp3file.getId3v2Tag().getTitle() !=null && !mp3file.getId3v2Tag().getTitle().contains("vk.com"))
			    			   id3v2Tag.setTitle(mp3file.getId3v2Tag().getTitle());
			    		   if (mp3file.getId3v2Tag().getTrack() !=null && !mp3file.getId3v2Tag().getTrack().contains("vk.com"))
			    			   id3v2Tag.setTrack(mp3file.getId3v2Tag().getTrack());
			    		   if (mp3file.getId3v2Tag().getUrl() !=null)
			    			   id3v2Tag.setUrl(mp3file.getId3v2Tag().getUrl());
			    		   if (mp3file.getId3v2Tag().getYear() !=null && !mp3file.getId3v2Tag().getYear().contains("vk.com"))
			    			   id3v2Tag.setYear(mp3file.getId3v2Tag().getYear());
			    	   }
			       } catch (Exception e){
			    	   e.printStackTrace();
			       }
			       
			       Log.d("DownloadService", "set new tags (image, artist if still not exist, and title if still not exist)");
			       id3v2Tag.setAlbumImage(stream.toByteArray(), "image/png");
			       id3v2Tag.setArtist(id3v2Tag.getArtist()==null ? musicToDownload.artist : id3v2Tag.getArtist());
			       id3v2Tag.setTitle(id3v2Tag.getTitle()==null ? musicToDownload.title : id3v2Tag.getTitle());
			       id3v2Tag.setAlbum(id3v2Tag.getAlbum()==null ? musicToDownload.title : id3v2Tag.getAlbum());
						
			       //fix tags error when try to save (remove unsupported old tags)
//			       mp3file.removeCustomTag();
			       mp3file.removeId3v1Tag();
			       mp3file.removeId3v2Tag();
					
			       //setting up new tags
			       mp3file.setId3v2Tag(id3v2Tag);
					
			       Log.d("DownloadManager", "save .mp3 file");
			       mp3file.save(downloadFile.getAbsolutePath()+".mp3");
			       downloadFile.delete();
			       downloadFile = new File(downloadFile.getAbsolutePath()+".mp3");
		       } catch (Exception e){
		    	   e.printStackTrace();
		    	   Log.d("DownloadService", "save .mp3 file");
		       	   downloadFile.renameTo(new File(downloadFile.getAbsolutePath()+".mp3"));
		       	   downloadFile.delete();
		       	   downloadFile = new File(downloadFile.getAbsolutePath()+".mp3");
		       } catch (OutOfMemoryError e){
		    	   e.printStackTrace();
		    	   Log.d("DownloadService", "save .mp3 file");
		       	   downloadFile.renameTo(new File(downloadFile.getAbsolutePath()+".mp3"));
		       	   downloadFile.delete();
		       	   downloadFile = new File(downloadFile.getAbsolutePath()+".mp3");
		       }
	       	   
	       	   Log.d("DownloadService", "edit modified time");
	       	   downloadFile.setLastModified(System.currentTimeMillis());
	       	   
	       	   Log.d("DownloadManager", "sent intent that new mp3 file added to library");
	       	   Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	       	   intent.setData(Uri.fromFile(downloadFile));
	       	   sendBroadcast(intent);
	       	   
	       	   Log.d("DownloadService", "download ready in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
	       	   
	       	   //show success
	       	   musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOADED;
	   		
	       	   Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
	       	   sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
	       	   getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
	       	   
	       	   //sleep to show success icon
	       	   try {
	       		   Thread.sleep(750);
	       	   } catch (InterruptedException e) {
	       		   e.printStackTrace();
	       	   }
		   		
	       	   //send that song is downloaded
	       	   musicToDownload.url = downloadFile.getAbsolutePath();
	       	   musicToDownload.isDownloaded = Constants.LIST_ACTION_DELETE;
		   		
	       	   sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
	       	   sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
	       	   getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
	    	
	    } catch (Exception e){
	    	handler.post(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.download_unhangled_error) + " " + musicToDownload.artist + " - " +musicToDownload.title, Toast.LENGTH_LONG).show();
				}
	    	});
	    	musicToDownload.isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
			Intent sendChangeSongDownloadPercentage = new Intent(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE);
			sendChangeSongDownloadPercentage.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicToDownload);
			getApplicationContext().sendBroadcast(sendChangeSongDownloadPercentage);
    		return;
	    }
	}
}
