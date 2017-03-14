package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.SeekBar;
import org.holoeverywhere.widget.SeekBar.OnSeekBarChangeListener;
import org.holoeverywhere.widget.TextView;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.Services.PlayerService;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PlayerFragment extends BaseFragment {
	
    //for retrieve data from activity
    Bundle bundle;
    
	//with this options we will load images
    DisplayImageOptions options ;
    
    private final static Handler handler = new Handler();
    
	TextView title, subTitle, timeCurrent, timeEnd;
	ImageView albumArt, albumArtBg, shuffle, prev, playPause, next, repeat;
	SeekBar seekBar;
	boolean updateSeek = true;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_player);
    	
        //retrieve bundle
      	bundle = this.getArguments();
    	
    	//init views
    	title = (TextView)contentView.findViewById(R.id.title);
    	subTitle = (TextView)contentView.findViewById(R.id.subtitle);
    	timeCurrent = (TextView)contentView.findViewById(R.id.time_current);
    	timeEnd = (TextView)contentView.findViewById(R.id.time_end);
    	albumArtBg = (ImageView)contentView.findViewById(R.id.albumArtBg);
    	albumArt = (ImageView)contentView.findViewById(R.id.albumArt);
    	shuffle = (ImageView)contentView.findViewById(R.id.shuffle);
    	prev = (ImageView)contentView.findViewById(R.id.prev);
    	playPause = (ImageView)contentView.findViewById(R.id.play_pause);
    	next = (ImageView)contentView.findViewById(R.id.next);
    	repeat = (ImageView)contentView.findViewById(R.id.repeat);
    	seekBar = (SeekBar)contentView.findViewById(R.id.seekBar1);
    	
        //init image loader
        options = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .showImageForEmptyUri(R.drawable.music_stub_source)
        .showImageOnFail(R.drawable.music_stub_source)
        .showImageOnLoading(R.color.transparent_color)
        .cacheInMemory(true)					
        .build();
    	
    	//set fonts
    	SFUIFonts.MEDIUM.apply(getActivity(), title);
    	SFUIFonts.LIGHT.apply(getActivity(), subTitle);
    	SFUIFonts.MEDIUM.apply(getActivity(), timeCurrent);
    	SFUIFonts.MEDIUM.apply(getActivity(), timeEnd);
    	
		//start play current tracklist
		if (!isMyServiceRunning(PlayerService.class)){
			//start service with audio in Intent
			Intent startPlayer = new Intent(getActivity(), PlayerService.class);
			startPlayer.putExtras(bundle);
			getActivity().startService(startPlayer);
		} else {
			if (bundle!=null && bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME)!=null){
				Intent restartPlayer = new Intent(Constants.INTENT_PLAYER_RESTART);
				restartPlayer.putExtras(bundle);
				getActivity().sendBroadcast(restartPlayer);
			}
		}
		
		shuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent a = new Intent(Constants.INTENT_PLAYER_SHUFFLE);
				getActivity().sendBroadcast(a);
			}
		});
		
		repeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent a = new Intent(Constants.INTENT_PLAYER_REPEAT);
				getActivity().sendBroadcast(a);
			}
		});
    	
		//view job
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isMyServiceRunning(PlayerService.class)){
					Intent next = new Intent(Constants.INTENT_PLAYER_NEXT);
					next.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, true);
					getActivity().sendBroadcast(next);
					
					//pause update for seeks info
					updateSeek=false;
					handler.removeCallbacks(updateSeekEnable);
					handler.postDelayed(updateSeekEnable, 500);
				} else {
					//start service with audio in Intent
					Intent startPlayer = new Intent(getActivity(), PlayerService.class);
					
			    	//determine next pos
					int cr = bundle.getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION);
			    	if (bundle.getInt(Constants.BUNDLE_PLAYER_LIST_SIZE)-1<=cr)
			    		cr = 0; 
			    	else
			    		cr++;
			    	bundle.putInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, cr);
			    	
					startPlayer.putExtras(bundle);
					getActivity().startService(startPlayer);
					
			        //request back update, to fit to current list playing (Update cover art, titles, etc)
			        handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							Intent requestBackSwitchInfo = new Intent(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO);
							getActivity().sendBroadcast(requestBackSwitchInfo);
					}}, 250);
				}
			}
		});
		
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isMyServiceRunning(PlayerService.class)){
					Intent prev = new Intent(Constants.INTENT_PLAYER_PREV);
					prev.putExtra(Constants.INTENT_PLAYER_BACK_SWITCH_FITS, true);
					getActivity().sendBroadcast(prev);
					
					//pause update for seeks info
					updateSeek=false;
					handler.removeCallbacks(updateSeekEnable);
					handler.postDelayed(updateSeekEnable, 500);
				} else {
					//start service with audio in Intent
					Intent startPlayer = new Intent(getActivity(), PlayerService.class);
					
			    	//determine prev pos
					int cr = bundle.getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION);
			    	if (cr==0)
			    		cr = bundle.getInt(Constants.BUNDLE_PLAYER_LIST_SIZE)-1; 
			    	else
			    		cr--;
			    	bundle.putInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, cr);
			    	
					startPlayer.putExtras(bundle);
					getActivity().startService(startPlayer);
					
			        //request back update, to fit to current list playing (Update cover art, titles, etc)
			        handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							Intent requestBackSwitchInfo = new Intent(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO);
							getActivity().sendBroadcast(requestBackSwitchInfo);
					}}, 250);
				}
			}
		});
		
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isMyServiceRunning(PlayerService.class)){
					Intent playPause = new Intent(Constants.INTENT_PLAYER_PLAY_PAUSE);
					playPause.putExtra(Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE, Constants.INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY);
					getActivity().sendBroadcast(playPause);
				} else {
					//start service with audio in Intent
					Intent startPlayer = new Intent(getActivity(), PlayerService.class);
					startPlayer.putExtras(bundle);
					getActivity().startService(startPlayer);
					
			        //request back update, to fit to current list playing (Update cover art, titles, etc)
			        handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							Intent requestBackSwitchInfo = new Intent(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO);
							getActivity().sendBroadcast(requestBackSwitchInfo);
					}}, 250);
				}
			}
		});
		
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				timeCurrent.setText(stringPlusZero(String.valueOf((int)(progress/1000/60)))+":"+stringPlusZero(String.valueOf((int)(progress/1000%60))));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				updateSeek=false;
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Intent i = new Intent(Constants.INTENT_PLAYER_SEEK_CHANGE);
				i.putExtra(Constants.INTENT_PLAYER_SEEK_TO, seekBar.getProgress());
				getActivity().sendBroadcast(i);
				handler.removeCallbacks(updateSeekEnable);
				handler.postDelayed(updateSeekEnable, 500);
			}
        });
		
    	return contentView;
	}
	
	Runnable updateSeekEnable = new Runnable() {
        public void run() {
        	updateSeek=true;
        }
    };
	
    @Override
    public void onResume() {
        super.onResume();
        //request back update, to fit to current list playing (Update cover art, titles, etc)
        handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				if (!isMyServiceRunning(PlayerService.class)){
					//switch to main list scree
					((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().setCurrentPage(1);
				} else {
					Intent requestBackSwitchInfo = new Intent(Constants.INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO);
					getActivity().sendBroadcast(requestBackSwitchInfo);
				}
		}}, 250);
        
        //enable receivers
    	getActivity().registerReceiver(updatePlayback, new IntentFilter(Constants.INTENT_UPDATE_PLAYBACK));
    	getActivity().registerReceiver(backSwitchInfo, new IntentFilter(Constants.INTENT_PLAYER_BACK_SWITCH_TRACK_INFO));
    	getActivity().registerReceiver(playPauseStatus, new IntentFilter(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE));
    	getActivity().registerReceiver(repeatStatus, new IntentFilter(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_REPEAT));
    	getActivity().registerReceiver(shuffleStatus, new IntentFilter(Constants.INTENT_PLAYER_PLAYBACK_CHANGE_SHUFFLE));
    }
    
	@Override
	public void onPause() {
		super.onPause();
		//disable receivers
		getActivity().unregisterReceiver(updatePlayback);
		getActivity().unregisterReceiver(backSwitchInfo);
		getActivity().unregisterReceiver(playPauseStatus);
		getActivity().unregisterReceiver(repeatStatus);
		getActivity().unregisterReceiver(shuffleStatus);
		
		//kill service if no song in player
		Intent i = new Intent(Constants.INTENT_PLAYER_KILL_SERVICE_ON_PAUSE);
		getActivity().sendBroadcast(i);
	}
	
	private BroadcastReceiver shuffleStatus = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, final Intent intent) {
			
			if (shuffle.getTag().toString().equals(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS) ? "dis" : "en"))
				return;
			
	    	Animation flyUpAnimation6 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
		    flyUpAnimation6.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					shuffle.setVisibility(View.INVISIBLE);
					shuffle.setImageResource(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS) ? R.drawable.ic_music_shuffle_dis : R.drawable.ic_music_shuffle);
					shuffle.setTag(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS) ? "dis" : "en");
					shuffle.setVisibility(View.VISIBLE);
					
					Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
					shuffle.startAnimation(flyDownAnimation);
				}
				@Override
				public void onAnimationRepeat(Animation arg0) { }
				@Override
				public void onAnimationStart(Animation arg0) { }
	    	});
		    shuffle.startAnimation(flyUpAnimation6);
		}
	};
	
	private BroadcastReceiver repeatStatus = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, final Intent intent) {
			
			if (repeat.getTag().toString().equals(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_REPEAT_STATUS) ? "all" : "one"))
				return;
			
	    	Animation flyUpAnimation6 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
		    flyUpAnimation6.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					repeat.setVisibility(View.INVISIBLE);
					repeat.setImageResource(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_REPEAT_STATUS) ? R.drawable.ic_music_repeat_all : R.drawable.ic_music_repeat_one);
					repeat.setTag(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_REPEAT_STATUS) ? "all" : "one");
					repeat.setVisibility(View.VISIBLE);
					
					Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
					repeat.startAnimation(flyDownAnimation);
				}
				@Override
				public void onAnimationRepeat(Animation arg0) { }
				@Override
				public void onAnimationStart(Animation arg0) { }
	    	});
		    repeat.startAnimation(flyUpAnimation6);
		}
	};
	
	private BroadcastReceiver playPauseStatus = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, final Intent intent) {
			
			if (playPause.getTag().toString().equals(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS) ? "play" : "pause"))
				return;
			
	    	Animation flyUpAnimation6 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
		    flyUpAnimation6.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					playPause.setVisibility(View.INVISIBLE);
					playPause.setImageResource(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS) ? R.drawable.ic_music_play : R.drawable.ic_music_pause);
					playPause.setTag(!intent.getExtras().getBoolean(Constants.INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS) ? "play" : "pause");
					playPause.setVisibility(View.VISIBLE);
					
					Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
					playPause.startAnimation(flyDownAnimation);
				}
				@Override
				public void onAnimationRepeat(Animation arg0) { }
				@Override
				public void onAnimationStart(Animation arg0) { }
	    	});
		    playPause.startAnimation(flyUpAnimation6);
		}
	};
	
	private BroadcastReceiver backSwitchInfo = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			//set up current track data
	    	updateCurrentTrackInfo(
	    			intent.getExtras().getBoolean(Constants.INTENT_PLAYER_BACK_SWITCH_DIRECTION), 
	    			intent.getExtras().getBoolean(Constants.INTENT_PLAYER_BACK_SWITCH_FITS), 
	    			intent.getExtras().getInt(Constants.INTENT_PLAYER_BACK_SWITCH_POSITION), 
	    			intent.getExtras().getInt(Constants.INTENT_PLAYER_BACK_SWITCH_SIZE), 
	    			intent.getExtras().getString(Constants.INTENT_PLAYER_LIST_TITLE_NAME), 
	    			(MusicCollection) intent.getExtras().getParcelable(Constants.INTENT_PLAYER_BACK_SWITCH_ONE_AUDIO));
		}
	};
    
	private BroadcastReceiver updatePlayback = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (!updateSeek)
				return;
			timeCurrent.setText(stringPlusZero(String.valueOf((int)(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, 0)/1000/60)))+":"+stringPlusZero(String.valueOf((int)(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, 0)/1000%60))));
			timeEnd.setText(stringPlusZero(String.valueOf((int)(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, 0)/1000/60)))+":"+stringPlusZero(String.valueOf((int)(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, 0)/1000%60))));
			seekBar.setMax(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_LENGTH, 0));
    		seekBar.setProgress(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT, 0));
    		seekBar.setSecondaryProgress(intent.getIntExtra(Constants.INTENT_UPDATE_PLAYBACK_CURRENT_BUFFERING, 0));
		}
	};
    
    public void updateCurrentTrackInfo(boolean plus, boolean fits, int currentTrack, int size, String abTitle, final MusicCollection currentInPlayer){
    	
    	//do not autoupdate if current displayed track is equals to new
    	if (title.getText().equals(currentInPlayer.title) && subTitle.getText().equals(currentInPlayer.artist)) return;
    	
		try {
			ImageLoader.getInstance().stop();
			ImageLoader.getInstance().resume();
			if (currentInPlayer.isDownloaded == Constants.LIST_ACTION_DELETE)
				ImageLoader.getInstance().displayImage(currentInPlayer.url, albumArt, options, 2, plus ? animateFirstListenerLeft : animateFirstListenerRight);
			else 
				ImageLoader.getInstance().displayImage(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(currentInPlayer.artist+ " - "+currentInPlayer.title, "UTF-8"), albumArt, options, 1, plus ? animateFirstListenerLeft : animateFirstListenerRight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
	    flyUpAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				title.setVisibility(View.INVISIBLE);
				title.setText(currentInPlayer.title);
				title.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
				title.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    title.startAnimation(flyUpAnimation);
	    
		Animation flyUpAnimation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
	    flyUpAnimation2.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				subTitle.setVisibility(View.INVISIBLE);
				subTitle.setText(currentInPlayer.artist);
				subTitle.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
				subTitle.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    subTitle.startAnimation(flyUpAnimation2);
	    
		Animation flyUpAnimation3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
	    flyUpAnimation3.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				timeEnd.setVisibility(View.INVISIBLE);
				timeEnd.setText(stringPlusZero(String.valueOf((int)(currentInPlayer.duration)/60))+":"+stringPlusZero(String.valueOf((int)(currentInPlayer.duration)%60)));
				timeEnd.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
				timeEnd.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    timeEnd.startAnimation(flyUpAnimation3);
	    
		Animation flyUpAnimation4 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
	    flyUpAnimation4.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				timeCurrent.setVisibility(View.INVISIBLE);
				timeCurrent.setText("00:00");
				timeCurrent.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
				timeCurrent.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    timeCurrent.startAnimation(flyUpAnimation4);
	    
	    //update title
	    setTitle(String.format(abTitle, currentTrack+1, size));
	    bundle.putInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, currentTrack);
	    bundle.putInt(Constants.BUNDLE_PLAYER_LIST_SIZE, size);
	    
	    if (!fits) return;
	    
	    if (plus){
	    	Animation flyUpAnimation5 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
		    flyUpAnimation5.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					next.setVisibility(View.INVISIBLE);
					next.setVisibility(View.VISIBLE);
					
					Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
					next.startAnimation(flyDownAnimation);
				}
				@Override
				public void onAnimationRepeat(Animation arg0) { }
				@Override
				public void onAnimationStart(Animation arg0) { }
	    	});
		    next.startAnimation(flyUpAnimation5);
	    } else {
	    	Animation flyUpAnimation6 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim_small);
		    flyUpAnimation6.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation arg0) {
					prev.setVisibility(View.INVISIBLE);
					prev.setVisibility(View.VISIBLE);
					
					Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim_small);
					prev.startAnimation(flyDownAnimation);
				}
				@Override
				public void onAnimationRepeat(Animation arg0) { }
				@Override
				public void onAnimationStart(Animation arg0) { }
	    	});
		    prev.startAnimation(flyUpAnimation6);
	    }
    }
    
	//this func adds zeros to string
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}
    
	//animation for universal image loader
	private ImageLoadingListener animateFirstListenerRight = new AnimateFirstDisplayListenerRight();
	private class AnimateFirstDisplayListenerRight extends SimpleImageLoadingListener {

		final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				
				Animation flyRightOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_right_anim_out);
				albumArtBg.startAnimation(flyRightOutAnimation);
				
				Animation flyRightAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_right_anim);
				flyRightAnimation.setAnimationListener(new AnimationListener(){
					@Override
					public void onAnimationEnd(Animation arg0) {
						albumArtBg.setImageBitmap(loadedImage);
					}
					@Override
					public void onAnimationRepeat(Animation arg0) { }
					@Override
					public void onAnimationStart(Animation arg0) { }
				});
				imageView.startAnimation(flyRightAnimation);
				
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					displayedImages.add(imageUri);
				}
			}
		}
	}
	private ImageLoadingListener animateFirstListenerLeft = new AnimateFirstDisplayListenerLeft();
	private class AnimateFirstDisplayListenerLeft extends SimpleImageLoadingListener {

		final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				
				Animation flyLeftOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_left_anim_out);
				albumArtBg.startAnimation(flyLeftOutAnimation);
				
				Animation flyLeftAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_left_anim);
				flyLeftAnimation.setAnimationListener(new AnimationListener(){
					@Override
					public void onAnimationEnd(Animation arg0) {
						albumArtBg.setImageBitmap(loadedImage);
					}
					@Override
					public void onAnimationRepeat(Animation arg0) { }
					@Override
					public void onAnimationStart(Animation arg0) { }
				});
				imageView.startAnimation(flyLeftAnimation);
				
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					displayedImages.add(imageUri);
				}
			}
		}
	}
	
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
