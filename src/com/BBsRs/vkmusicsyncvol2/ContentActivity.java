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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseActivity;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.Fragments.MyMusicFragment;
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
        
        sliderMenu.add(getResources().getStringArray(R.array.menu)[0].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[1], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_my_music).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[2], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_search).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[3], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_popular).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[4], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_recommendations).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[5], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_friends).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[6], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_groups).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[7], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_downloads).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[8].toUpperCase()).setCustomLayout(R.layout.custom_slider_menu_item).clickable(false).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[9], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_settings).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        sliderMenu.add(getResources().getStringArray(R.array.menu)[10], MyMusicFragment.class, new int[]{R.color.menu_selected_color, R.color.menu_selected_color}).setIcon(R.drawable.ic_slider_premium).setCustomLayout(R.layout.custom_slider_menu_item_selectable).setTextAppereance(1);
        
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
	}
	
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
					FadeInBitmapDisplayer.animate(imageView, 300);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
