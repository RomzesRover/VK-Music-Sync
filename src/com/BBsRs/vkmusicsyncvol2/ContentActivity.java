package com.BBsRs.vkmusicsyncvol2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.addon.AddonSlider;
import org.holoeverywhere.addon.Addons;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.slider.SliderMenu;
import org.holoeverywhere.widget.TextView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseActivity;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.Fragments.FrGrFragment;
import com.BBsRs.vkmusicsyncvol2.Fragments.MusicFragment;
import com.BBsRs.vkmusicsyncvol2.Fragments.PlayerFragment;
import com.BBsRs.vkmusicsyncvol2.Fragments.SettingsFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

@Addons(AddonSlider.class)
public class ContentActivity extends BaseActivity {
	public AddonSlider.AddonSliderA addonSlider() {
	      return addon(AddonSlider.class);
	}
	
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    /*----------------------------VK API-----------------------------*/
	
	//with this options we will load images
    DisplayImageOptions options ;
	
	// some data to slider menu
	SliderMenu sliderMenu;
	
	SharedPreferences sPref;

	/** Called when the activity is first created. */
	@SuppressLint("DefaultLocale") 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    	//init vkapi
	    account.restore(this);
	    
        //init image loader
        options = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.nopic)
        .cacheInMemory(true)					
        .build();
	    
	    //set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    //init slider menu
        sliderMenu = addonSlider().obtainDefaultSliderMenu(R.layout.menu);
        sliderMenu.setInverseTextColorWhenSelected(false);
        addonSlider().setOverlayActionBar(true);
        
        //init bundles
        Bundle myMusic  = new Bundle();
        myMusic.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_OF_PAGE);
        myMusic.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, account.user_id);
        myMusic.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[1]);
        
        Bundle search  = new Bundle();
        search.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_SEARCH);
        search.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[2]);
        
        Bundle downloaded  = new Bundle();
        downloaded.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_DOWNLOADED);
        downloaded.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[7]);
        
        Bundle popular  = new Bundle();
        popular.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_POPULAR);
        popular.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[3]);
        
        Bundle recommendations  = new Bundle();
        recommendations.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS);
        recommendations.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[4]);
        recommendations.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, account.user_id);
        
        Bundle friends  = new Bundle();
        friends.putInt(Constants.BUNDLE_FRGR_LIST_TYPE, Constants.BUNDLE_FRGR_LIST_FRIENDS);
        friends.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[5]);
        
        Bundle groups = new Bundle();
        groups.putInt(Constants.BUNDLE_FRGR_LIST_TYPE, Constants.BUNDLE_FRGR_LIST_GROUPS);
        groups.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[6]);
        
        Bundle settings = new Bundle();
        settings.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[9]);
        
        //init slider menu with spec bundles
        sliderMenu.add(getResources().getStringArray(R.array.menu)[0].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[1], MusicFragment.class, myMusic, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_my_music).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[2], MusicFragment.class, search, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_search).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[3], MusicFragment.class, popular, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_popular).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[4], MusicFragment.class, recommendations, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_recommendations).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[5], FrGrFragment.class, friends, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_friends).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[6], FrGrFragment.class, groups, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_groups).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[7], MusicFragment.class, downloaded, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_downloads).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[8].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[9], SettingsFragment.class, settings, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_settings).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        
        if(savedInstanceState == null)
        	sliderMenu.setCurrentPage(1);
        
        //set up user circle
       	SFUIFonts.ULTRALIGHT.apply(getApplicationContext(), ((TextView) findViewById(R.id.title)));
       	((TextView) findViewById(R.id.title)).setText(sPref.getString(Constants.PREFERENCES_USER_FIRST_NAME, "no value") + " " + sPref.getString(Constants.PREFERENCES_USER_LAST_NAME, "no value"));
       	ImageLoader.getInstance().resume();
       	ImageLoader.getInstance().displayImage(sPref.getString(Constants.PREFERENCES_USER_AVATAR_URL, "http://vk.com/images/deactivated_100.gif"), ((ImageView) findViewById(R.id.cover_art)), options, animateFirstListener);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getSupportActionBar().setIcon(R.drawable.ic_menu);
		
		//
		registerReceiver(openPlayerFragment, new IntentFilter(Constants.INTENT_PLAYER_OPEN_ACTIVITY_PLAYER_FRAGMENT));
	}
	
	@Override
	public void onPause(){
		super.onPause();
		//
		unregisterReceiver(openPlayerFragment);
	}

	private BroadcastReceiver openPlayerFragment = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	//check is playerFragment current in use
	    	if (getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_PLAYER_TAG) != null){
	    		return;
	    	}
	    	
			//create bundle to player list
			Bundle playerBundle  = new Bundle();
			
			//create music list fragment
	        PlayerFragment playerFragment = new PlayerFragment();
	        playerFragment.setArguments(playerBundle);
           	
           	//start new music list fragment
			addonSlider().obtainSliderMenu().replaceFragment(playerFragment, Constants.FRAGMENT_PLAYER_TAG, true);
	    }
	};
	
	//animation for universal image loader
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
