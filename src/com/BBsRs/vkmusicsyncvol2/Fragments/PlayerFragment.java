package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

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
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
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
    
    ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
    int currentTrack;
    
    private final static Handler handler = new Handler();
    
	TextView title, subTitle, timeCurrent, timeEnd;
	ImageView albumArt, albumArtBg, shuffle, prev, playPause, next, repeat;
	
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
    	
    	//load list
    	musicCollection = bundle.getParcelableArrayList(Constants.BUNDLE_PLAYER_LIST_COLLECTIONS);
    	currentTrack = bundle.getInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION);
    	
    	//set up current track data
    	updateCurrentTrackInfo(true);
    	
		//view job
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isItPossibleToChangeTrack)
		    		return;
				currentTrack++;
				updateCurrentTrackInfo(true);
			}
		});
		
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!isItPossibleToChangeTrack)
		    		return;
				currentTrack--;
				updateCurrentTrackInfo(false);
			}
		});
		
    	
    	
    	return contentView;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(String.format(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME), currentTrack+1));
    }
    
    boolean isItPossibleToChangeTrack = true;
    public void updateCurrentTrackInfo(boolean plus){
    	isItPossibleToChangeTrack = false;
		try {
			ImageLoader.getInstance().stop();
			ImageLoader.getInstance().resume();
			if (musicCollection.get(currentTrack).isDownloaded == Constants.LIST_ACTION_DELETE)
				ImageLoader.getInstance().displayImage(musicCollection.get(currentTrack).url, albumArt, options, 2, plus ? animateFirstListenerLeft : animateFirstListenerRight);
			else 
				ImageLoader.getInstance().displayImage(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(musicCollection.get(currentTrack).artist+ " - "+musicCollection.get(currentTrack).title, "UTF-8"), albumArt, options, 1, plus ? animateFirstListenerLeft : animateFirstListenerRight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
	    flyUpAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				title.setVisibility(View.INVISIBLE);
				title.setText(musicCollection.get(currentTrack).title);
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
				subTitle.setText(musicCollection.get(currentTrack).artist);
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
	    
		Animation flyUpAnimation3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
	    flyUpAnimation3.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				timeEnd.setVisibility(View.INVISIBLE);
				timeEnd.setText(stringPlusZero(String.valueOf((int)(musicCollection.get(currentTrack).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicCollection.get(currentTrack).duration)%60)));
				timeEnd.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
				timeEnd.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    timeEnd.startAnimation(flyUpAnimation3);
	    
		Animation flyUpAnimation4 = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
	    flyUpAnimation4.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				timeCurrent.setVisibility(View.INVISIBLE);
				timeCurrent.setText("00:00");
				timeCurrent.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
				timeCurrent.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
	    timeCurrent.startAnimation(flyUpAnimation4);
	    
	    //update title
	    setTitle(String.format(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME), currentTrack+1));
	    
	    handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				isItPossibleToChangeTrack = true;
			}
	    }, 500);
	    
		
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
}
