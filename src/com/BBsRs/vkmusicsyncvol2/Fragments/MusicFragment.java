package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.SearchAutoComplete;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter.FilterListener;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.MusicListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.Services.DownloadService;
import com.BBsRs.vkmusicsyncvol2.collections.AlbumCollection;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.perm.kate.api.Api;
import com.perm.kate.api.Attachment;
import com.perm.kate.api.Audio;
import com.perm.kate.api.AudioAlbum;
import com.perm.kate.api.User;
import com.perm.kate.api.WallMessage;

public class MusicFragment extends BaseFragment {
	
	SharedPreferences sPref;
	
	private final Handler handler = new Handler();
	
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    Api api;
    /*----------------------------VK API-----------------------------*/
	
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
	PullToRefreshLayout mPullToRefreshLayout;
	ListView list;
	View header;
	
	//with this options we will load images
    DisplayImageOptions options ;
    
    MusicListAdapter musicListAdapter;
    ArrayList<AlbumCollection> albumCollection = new ArrayList<AlbumCollection>();
    
    ArrayList<MusicCollection> musicCollectionToDelete = new ArrayList<MusicCollection>();
    
    //for retrieve data from activity
    Bundle bundle;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_music_frgr_album);
    	
    	//init hide slider menu (hide search)
        try {
        	((ContentActivity) getSupportActivity()).addonSlider().setDrawerListener(new DrawerListener(){
        		@Override
        		public void onDrawerClosed(View arg0) {}
        		@Override
        		public void onDrawerOpened(View arg0) {
        			//hide search, keyboard if its opened
                	if (searchView != null && getActivity() != null){
          				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
          				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        				}
        		}
        		@Override
        		public void onDrawerSlide(View arg0, float arg1) {}
        		@Override
        		public void onDrawerStateChanged(int arg0) {}
        	});
        } catch (Exception e){
        	e.printStackTrace();
        	//Error on tablets !!
        }
    	
    	//set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    
    	//init vkapi
	    account.restore(getActivity());
        api=new Api(account.access_token, Constants.CLIENT_ID);
        
        //init image loader
        options = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.music_stub)
        .cacheInMemory(true)					
        .build();
        
        //init adapter with null
    	musicListAdapter = new MusicListAdapter(getActivity(), null, options);
    	
        //retrieve bundle
      	bundle = this.getArguments();
    	
        //init views
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	//init header buttons
    	header = inflater.inflate(R.layout.list_music_header);
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.albums));
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.wall));
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.recc));
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.errr));
    	((LinearLayout)header.findViewById(R.id.wallLayout)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//create bundle to m list
				Bundle wallMusicBundle  = new Bundle();
				
				//set up bundle
				wallMusicBundle.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_WALL);
	        	wallMusicBundle.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID));
	        	wallMusicBundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME)+";"+getResources().getString(R.string.content_activity_wall));

		        //create music list fragment
		        MusicFragment musicListFragment = new MusicFragment();
	           	musicListFragment.setArguments(wallMusicBundle);
	           	
	           	//start new music list fragment
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(musicListFragment);
			}
		});
    	((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//create bundle to m list
		        Bundle recommendations  = new Bundle();
				
				//set up bundle
		        recommendations.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS);
		        recommendations.putString(Constants.BUNDLE_LIST_TITLE_NAME, bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME)+";"+getResources().getString(R.string.content_activity_recommendations));
		        recommendations.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID));
		        
		        //create music list fragment
		        MusicFragment musicListFragment = new MusicFragment();
	           	musicListFragment.setArguments(recommendations);
	           	
	           	//start new music list fragment
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(musicListFragment);
			}
		});
    	((LinearLayout)header.findViewById(R.id.albumsLayout)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//create bundle to m list
		        Bundle albums  = new Bundle();
				
				//set up bundle
		        albums.putString(Constants.BUNDLE_LIST_TITLE_NAME, bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME)+";"+getResources().getString(R.string.content_activity_albums));
		        albums.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID));
		        albums.putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, albumCollection);
		        
		        //create music list fragment
		        AlbumsFragment albumsFragment = new AlbumsFragment();
		        albumsFragment.setArguments(albums);
		        
		        //start new music list fragment
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(albumsFragment);
			}
		});
    	list.addHeaderView(header);
    	list.setAdapter(musicListAdapter);
    	
		list.setOnScrollListener(new OnScrollListener(){
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) { }
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState){
				case OnScrollListener.SCROLL_STATE_IDLE:
					handler.removeCallbacks(resuming);
					handler.postDelayed(resuming, 500);
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					handler.removeCallbacks(resuming);
					ImageLoader.getInstance().pause();
					if (musicListAdapter != null){
						musicListAdapter.updateQuality = false;
					}
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					handler.removeCallbacks(resuming);
					ImageLoader.getInstance().pause();
					if (musicListAdapter != null){
						musicListAdapter.updateQuality = false;
					}
					break;
				}
			}
		});
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int _position, long arg3) {
				
				//null list of music to delete to prevent possible play error
		    	musicCollectionToDelete = new ArrayList<MusicCollection>();
		    	
				//create bundle to player list
				Bundle playerBundle  = new Bundle();
				playerBundle.putParcelableArrayList(Constants.BUNDLE_PLAYER_LIST_COLLECTIONS, musicListAdapter.getMusicCollection());
				playerBundle.putInt(Constants.BUNDLE_PLAYER_CURRENT_SELECTED_POSITION, _position-1);
				playerBundle.putInt(Constants.BUNDLE_PLAYER_LIST_SIZE, musicListAdapter.getCount());
				playerBundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME).split(";")[bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME).split(";").length-1]+";%s "+getActivity().getString(R.string.player_of)+" %s");
				
				//create music list fragment
		        PlayerFragment playerFragment = new PlayerFragment();
		        playerFragment.setArguments(playerBundle);
	           	
	           	//start new music list fragment
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(playerFragment, Constants.FRAGMENT_PLAYER_TAG, true);
			}
		});
		
    	musicListAdapter.bindListView(list);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        if(bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS) == null) {
        	updateList();
        } else {
        	if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false) && (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_OF_PAGE)){
        		//stop force update owner list
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false).commit();

				updateList();
        	} else {
        		if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false) && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_SEARCH){
        			//stop force update
        			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false).commit();

        			updateList();
        		} else {
        			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false) && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_POPULAR){
            			//stop force update
            			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false).commit();

            			updateList();
            		} else {
            			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false) && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS){
                			//stop force update
                			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false).commit();

                			updateList();
                		} else {
                			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false) && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_DOWNLOADED){
                    			//stop force update
                    			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false).commit();
                    			
                    			updateList();
                    		} else {
					        	ArrayList<MusicCollection> musicCollection = bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
					        	albumCollection = bundle.getParcelableArrayList(Constants.EXTRA_LIST_SECOND_COLLECTIONS);
					        	musicListAdapter.UpdateList(musicCollection);
					        	musicListAdapter.notifyDataSetChanged();
					        	
					        	setUpHeaderView();
					        	list.setVisibility(View.VISIBLE);
                    		}
                		}
            		}
        		}
        	}
        }
        
    	//stop update all to prevent @double autoupdate
    	switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
		case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false).commit();
			break;
		case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false).commit();
			break;
		case Constants.BUNDLE_MUSIC_LIST_POPULAR:
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false).commit();
			break;
		case Constants.BUNDLE_MUSIC_LIST_SEARCH:
			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false).commit();
			break;
		case Constants.BUNDLE_MUSIC_LIST_OF_PAGE:
			if (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id){
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false).commit();
			}
			break;
		}
        
    	return contentView;
	}
	
	public void updateList(){
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				//refresh on open to load data when app first time started
		        mPullToRefreshLayout.setRefreshing(true);
		        customOnRefreshListener.onRefreshStarted(null);
			}
      	}, 100);
	}
	
	final Runnable resuming = new Runnable(){
		@Override
		public void run() {
			//resume update image
			ImageLoader.getInstance().resume();
			
			if (musicListAdapter != null){
				musicListAdapter.updateQuality = true;
				musicListAdapter.updateQualities();
			}
		}
	};
	
	//init search
	SearchView searchView;
	SearchManager searchManager;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_music, menu);
		
		searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
  		//Create the search view
  		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
  		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
  		
  		searchView.setOnQueryTextListener(new OnQueryTextListener(){
  			@Override
  			public boolean onQueryTextSubmit(String query) {
  				if (searchView != null){
	  				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
	  				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	  				if (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_SEARCH){
	  					bundle.putString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST, query);
	  					bundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, getResources().getStringArray(R.array.menu)[2] + ": " + query);
	  					setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
	    				//refresh search results
	    		        mPullToRefreshLayout.setRefreshing(true);
	    		        customOnRefreshListener.onRefreshStarted(null);
	  				}
  				}
  				return false;
  			}
  			@Override
  			public boolean onQueryTextChange(String newText) {
  				if (musicListAdapter != null && musicListAdapter.getMusicCollectionNonFiltered()!=null && !musicListAdapter.getMusicCollectionNonFiltered().isEmpty() && bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) != Constants.BUNDLE_MUSIC_LIST_SEARCH){
  					//pause image loads
  					handler.removeCallbacks(resuming);
  					ImageLoader.getInstance().pause();
  					if (musicListAdapter != null){
  						musicListAdapter.updateQuality = false;
  					}
  					
  					bundle.putString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST, newText);
  					musicListAdapter.getFilter().filter(newText, new FilterListener(){
						@Override
						public void onFilterComplete(int arg0) {
							if (musicListAdapter.getMusicCollection().isEmpty()){
		  						bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST);
							} else {
								bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR);
							}
		  					setUpHeaderView();
		  					list.setSelection(0);
						}
  					});
  					
  					handler.removeCallbacks(resuming);
  					handler.postDelayed(resuming, 1000);
  				}
  				return false;
  			}});
  		
  		setSearchStyles();
  		
    	//resume search
    	if (bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST) != null && bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST).length()>0 && searchView != null){
    		searchView.setQuery(bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST), false);
    	} else {
    		if (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_SEARCH)
    			searchView.setIconified(false);
    	}
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
	public void setSearchStyles(){
		Field searchField;
		try {
			searchField = SearchView.class.getDeclaredField("mCloseButton");
			searchField.setAccessible(true);
			//set close button icon
	        ImageView closeBtn = (ImageView) searchField.get(searchView);
	        closeBtn.setImageResource(R.drawable.ic_search_cancel);
	        closeBtn.setBackgroundColor(getResources().getColor(R.color.transparent_color));
	        //set search icon
	        ImageView searchButton = (ImageView) searchView.findViewById(R.id.search_button);            
            searchButton.setImageResource(R.drawable.ic_menu_search);
            //set search textfield bg
            LinearLayout searchPlate = (LinearLayout) searchView.findViewById(R.id.search_plate);    
            if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN){
            	searchPlate.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_menu_search_textfield_bg));
	        } else {
	        	searchPlate.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_menu_search_textfield_bg));
	        }
            //set edit text height
            final SearchAutoComplete mQueryTextView = (SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
            LayoutParams layoutParams = mQueryTextView.getLayoutParams();
            layoutParams.height = (int) (29 * getActivity().getResources().getDisplayMetrics().density + 0.5f);
            mQueryTextView.setLayoutParams(layoutParams);
            //set text font
            SFUIFonts.ULTRALIGHT.apply(getActivity(), mQueryTextView);
            mQueryTextView.setHint(getString(R.string.content_activity_search_hint));
            mQueryTextView.setHintTextColor(getActivity().getResources().getColor(R.color.gray_three_color));
            mQueryTextView.setTextColor(getActivity().getResources().getColor(R.color.white_color));
            mQueryTextView.setTextSize((float)17);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void onResume() {
        super.onResume();
        //resume loads images
		handler.removeCallbacks(resuming);
		handler.postDelayed(resuming, 500);
		
        //enable menu
    	setHasOptionsMenu(true);
        //set subtitle for a current fragment with custom font
    	setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
    	//enable receivers
    	getActivity().registerReceiver(addSongToOwnerList, new IntentFilter(Constants.INTENT_ADD_SONG_TO_OWNER_LIST));
    	getActivity().registerReceiver(removeSongFromOwnerList, new IntentFilter(Constants.INTENT_REMOVE_SONG_FROM_OWNER_LIST));
    	getActivity().registerReceiver(downloadSongToStorage, new IntentFilter(Constants.INTENT_DOWNLOAD_SONG_TO_STORAGE));
    	getActivity().registerReceiver(changeSongDownloadPercentage, new IntentFilter(Constants.INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE));
    	getActivity().registerReceiver(deleteSongFromStorage, new IntentFilter(Constants.INTENT_DELETE_SONG_FROM_STORAGE));
    	
    	//null list of music to delete
    	musicCollectionToDelete = new ArrayList<MusicCollection>();
    	
    	//update all lists if necessary
    	switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
		case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false)){
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false).commit();
				updateList();
			}
			break;
		case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false)){
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false).commit();
				updateList();
			}
			break;
		case Constants.BUNDLE_MUSIC_LIST_POPULAR:
			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false)){
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false).commit();
				updateList();
			}
			break;
		case Constants.BUNDLE_MUSIC_LIST_SEARCH:
			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false)){
				sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false).commit();
				updateList();
			}
			break;
		case Constants.BUNDLE_MUSIC_LIST_OF_PAGE:
			if (sPref.getBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false))
				if (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id){
					sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false).commit();
					updateList();
				}
			break;
		}
    }
    
	@Override
	public void onPause() {
		super.onPause();
		//pause image loads
		handler.removeCallbacks(resuming);
		ImageLoader.getInstance().stop();
		if (musicListAdapter != null){
			musicListAdapter.updateQuality = false;
		}
		
		//disable receivers
		getActivity().unregisterReceiver(addSongToOwnerList);
		getActivity().unregisterReceiver(removeSongFromOwnerList);
		getActivity().unregisterReceiver(downloadSongToStorage);
		getActivity().unregisterReceiver(changeSongDownloadPercentage);
		getActivity().unregisterReceiver(deleteSongFromStorage);
		
		//call update for current list for second open
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
		} else {
			if (musicListAdapter != null){
				getArguments().putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, musicListAdapter.getMusicCollectionNonFiltered());
				getArguments().putParcelableArrayList(Constants.EXTRA_LIST_SECOND_COLLECTIONS, albumCollection);
			}
		}
	}
	
	private BroadcastReceiver addSongToOwnerList = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			if (musicListAdapter == null) return;

			final int position = arg1.getIntExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, 0);
			final MusicCollection audioToAdd = musicListAdapter.getItem(position);
			
			new Thread (new Runnable(){
				@Override
				public void run() {
					try {
						long oid = audioToAdd.owner_id != -1 ? audioToAdd.owner_id : account.user_id;
						if ((bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id && (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_OF_PAGE || bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_ALBUM)))
							api.restoreAudio(audioToAdd.aid, oid, null, null);
						else
							api.addAudio(audioToAdd.aid, oid, null, null, null);
						//notify list with v sign
						handler.post(new Runnable(){
							@Override
							public void run() {
								if (musicListAdapter != null){
									if ((bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id && (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_OF_PAGE || bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_ALBUM)))
										musicListAdapter.getItem(position).isInOwnerList = Constants.LIST_ACTION_REMOVE;
									else {
										musicListAdapter.getItem(position).isInOwnerList = Constants.LIST_ACTION_ADDED;
										//force update owner list
										sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, true).commit();
									}
									musicListAdapter.updateIsInOwnerListState(position);
								}
							}
						});
					} catch (Exception e){
						e.printStackTrace();
						handler.post(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(getActivity(), String.format(getString(R.string.content_activity_error_on_adding_to_owner_list), audioToAdd.artist + " - " + audioToAdd.title), Toast.LENGTH_LONG).show();
							}
						});
					}
				}
			}).start();
		}
	};
	
	private BroadcastReceiver removeSongFromOwnerList = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			if (musicListAdapter == null) return;

			final int position = arg1.getIntExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, 0);
			final MusicCollection audioToRemove = musicListAdapter.getItem(position);
			
			new Thread (new Runnable(){
				@Override
				public void run() {
					try {
						long oid = audioToRemove.owner_id != -1 ? audioToRemove.owner_id : account.user_id;
						api.deleteAudio(audioToRemove.aid, oid);
						//notify list with + sign
						handler.post(new Runnable(){
							@Override
							public void run() {
								if (musicListAdapter != null){
									musicListAdapter.getItem(position).isInOwnerList = Constants.LIST_ACTION_RESTORE;
									musicListAdapter.updateIsInOwnerListState(position);
								}
								if ((bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id && (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_ALBUM))){
									//force update owner list
									sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, true).commit();
								}
							}
						});
					} catch (Exception e){
						e.printStackTrace();
						handler.post(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(getActivity(), String.format(getString(R.string.content_activity_error_on_remove_from_owner_list), audioToRemove.artist + " - " + audioToRemove.title), Toast.LENGTH_LONG).show();
							}
						});
					}
				}
			}).start();
		}
	};
	
	private BroadcastReceiver downloadSongToStorage = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			if (musicListAdapter == null) return;
			
			final int position = arg1.getIntExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, 0);
			MusicCollection AudioToDownloadToStorage = musicListAdapter.getItem(position);
			AudioToDownloadToStorage.isDownloaded = Constants.LIST_ACTION_DOWNLOAD_STARTED;
			
			//delete song from deleteList
			int index = 0;
			for (MusicCollection one : musicCollectionToDelete){
				if (one.aid == AudioToDownloadToStorage.aid && one.owner_id == AudioToDownloadToStorage.owner_id && one.artist.equals(AudioToDownloadToStorage.artist) && one.title.equals(AudioToDownloadToStorage.title)){
					musicCollectionToDelete.remove(index);
					break;
				}
				index++;
			}
			
			try {
				if (!isMyServiceRunning(DownloadService.class)){
					//start service with audio in Intent
					Intent startDownload = new Intent(getActivity(), DownloadService.class);
					startDownload.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)AudioToDownloadToStorage);
					getActivity().startService(startDownload);
				} else {
					Intent addSongToDownloadQueue = new Intent(Constants.INTENT_ADD_SONG_TO_DOWNLOAD_QUEUE);
					addSongToDownloadQueue.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)AudioToDownloadToStorage);
					getActivity().sendBroadcast(addSongToDownloadQueue);
				}
			} finally{
				if (musicListAdapter != null){
					musicListAdapter.updateIsDownloaded(position);
				}
			}
		}
	};
	
	private BroadcastReceiver deleteSongFromStorage = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			if (musicListAdapter == null) return;
			
			final int position = arg1.getIntExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, 0);
			MusicCollection AudioToDeleteFromStorage = musicListAdapter.getItem(position);
			AudioToDeleteFromStorage.isDownloaded = Constants.LIST_ACTION_DOWNLOAD;
			
			musicCollectionToDelete.add(AudioToDeleteFromStorage);
			
			musicListAdapter.updateIsDownloaded(position);
		}
	};
	
	private BroadcastReceiver changeSongDownloadPercentage = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			final MusicCollection audioToChangeDownloadPercentage = arg1.getParcelableExtra(Constants.INTENT_EXTRA_ONE_AUDIO);
			Log.v("here", audioToChangeDownloadPercentage.isDownloaded+"");
			
			if (musicListAdapter != null && musicListAdapter.getMusicCollection() != null){
				int position = 0;
				for (MusicCollection one : musicListAdapter.getMusicCollection()){
					if (one.aid == audioToChangeDownloadPercentage.aid && one.owner_id == audioToChangeDownloadPercentage.owner_id && one.artist.equals(audioToChangeDownloadPercentage.artist) && one.title.equals(audioToChangeDownloadPercentage.title)){
						one.url = audioToChangeDownloadPercentage.url;
						one.isDownloaded = audioToChangeDownloadPercentage.isDownloaded;
						musicListAdapter.updateIsDownloaded(position);
						//do not autoupdate current list
						if (one.isDownloaded == Constants.LIST_ACTION_DELETE){
							switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
							case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
								sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false).commit();
								break;
							case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
								sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false).commit();
								break;
							case Constants.BUNDLE_MUSIC_LIST_POPULAR:
								sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false).commit();
								break;
							case Constants.BUNDLE_MUSIC_LIST_SEARCH:
								sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false).commit();
								break;
							case Constants.BUNDLE_MUSIC_LIST_OF_PAGE:
								if (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id)
									sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false).commit();
								break;
							} 
						}
					}
					position++;
				}
			}
		}
	};
	
	public void setUpHeaderView(){
		if (header == null) return;
		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.GONE);
		((LinearLayout)header.findViewById(R.id.wallLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.VISIBLE);
		((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.VISIBLE);
		
        switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
        case Constants.BUNDLE_MUSIC_LIST_POPULAR: case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS: case Constants.BUNDLE_MUSIC_LIST_SEARCH: case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED: case Constants.BUNDLE_MUSIC_LIST_WALL: case Constants.BUNDLE_MUSIC_LIST_ALBUM:
        	((LinearLayout)header.findViewById(R.id.wallLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.GONE);
        	break;
        case Constants.BUNDLE_MUSIC_LIST_OF_PAGE:
        	if (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id || bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) < 0)
        		((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
        	if (albumCollection.isEmpty())
        		((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.GONE);
        	break;
        }
        
        switch (bundle.getInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR)){
        case Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR:
        	((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.GONE);
        	break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_ACCESS_TO_USER_AUDIO_DENIED:
    		((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_access_to_users_audio_denied));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_GROUP_AUDIO_DISABLED:
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_group_audio_is_disabled));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST:
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_empty_audio_list));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_ANOTHER:
    		((LinearLayout)header.findViewById(R.id.wallLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.GONE);
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_error));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_NO_SEARCH_REQUEST:
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_no_search_request));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_PAGE_DEACTIVATED:
    		((LinearLayout)header.findViewById(R.id.wallLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
			((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.GONE);
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_page_deactivated));
    		break;
    	}
	}
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public class CustomOnRefreshListener  implements OnRefreshListener{
		@Override
		public void onRefreshStarted(View view) {
			AsyncTask<Void, Void, Void> loadM = new AsyncTask<Void, Void, Void>() {
				
				@Override
				protected Void doInBackground(Void... params) {
					try {
						//show animataion only if list is already visible
						if (list.getVisibility() == View.VISIBLE){
							handler.post(new Runnable(){
								@Override
								public void run() {
									Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
			                    	list.startAnimation(flyDownAnimation);
			                    	flyDownAnimation.setAnimationListener(new AnimationListener(){
			        					@Override
			        					public void onAnimationEnd(Animation arg0) {
			        						list.setVisibility(View.INVISIBLE);
			        					}
			        					@Override
			        					public void onAnimationRepeat(Animation arg0) { }
			        					@Override
			        					public void onAnimationStart(Animation arg0) { }
			                    	});
			                    	
			                    	//hide search, keyboard if its opened
			                    	if (searchView != null){
			                    		searchView.setIconified(true);
			                    		searchView.onActionViewCollapsed();
			        	  				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
			        	  				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
			          				}
								}
							});
						}
						
						//slep to prevent laggy animations
						Thread.sleep(250);
						
						//delete music to delete
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
						
						ArrayList<Audio> musicList = new ArrayList<Audio>();
						ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
						//null lists
						albumCollection = new ArrayList<AlbumCollection>();
						//null list of music to delete
				    	musicCollectionToDelete = new ArrayList<MusicCollection>();
						
						//load nesc music
				        switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
					        case Constants.BUNDLE_MUSIC_LIST_OF_PAGE:
					        	musicList = api.getAudio(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), null, null, null, null, null);
					        	for (AudioAlbum one : api.getAudioAlbums(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), 0, 100)){
                        	    	albumCollection.add(new AlbumCollection(one.album_id, one.owner_id, one.title));
                        	    }
					        	if (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id){
					        		//update avatar and user name
					        		Thread.sleep(100);
					        		Collection<Long> u = new ArrayList<Long>();
						            u.add(account.user_id);
						            Collection<String> d = new ArrayList<String>();
						            d.add("");
						            
						            User userOne = api.getProfiles(u, d, "photo_200,photo_100", "", "", "").get(0);
						            sPref.edit().putString(Constants.PREFERENCES_USER_AVATAR_URL, ((userOne.photo_200 == null || userOne.photo_200.length()<1) ? userOne.photo_medium_rec : userOne.photo_200)).commit();
									sPref.edit().putString(Constants.PREFERENCES_USER_FIRST_NAME, userOne.first_name).commit();
									sPref.edit().putString(Constants.PREFERENCES_USER_LAST_NAME, userOne.last_name).commit();
									
									sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_OWNER_LIST, false).commit();
					        	}
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_POPULAR:
					        	musicList = api.getAudioPopular(0, null, null, null);
					        	sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_POPULAR_LIST, false).commit();
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
					        	musicList = api.getAudioRecommendations(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), null, null, null);
					        	sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_RECC_LIST, false).commit();
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_SEARCH:
					        	if (bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST) != null && bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST).length()>0){
					        		musicList = api.searchAudio(bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST), null, null, (long) 300, null, null, null);
					        		if (searchView != null)
						        		searchView.setQuery(bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST), false);
					        	}
					        	sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_SEARCH_LIST, false).commit();
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_WALL:
					        	ArrayList<WallMessage> wallMessageList = new ArrayList<WallMessage>();
					        	while (true){
                        			ArrayList<WallMessage> wallMessageListTemp = api.getWallMessages(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), 100, wallMessageList.size(), null);
                        			Thread.sleep(250);
                        			wallMessageList.addAll(wallMessageListTemp);
                        			if (wallMessageListTemp.size()<100 || wallMessageList.size()>=300)
                        				break;
                        		}
                        		int index=0;
                        		for (WallMessage one : wallMessageList){
                        			if (one.post_type == 1)
	                        			for (Attachment oneA : one.copy_history.get(0).attachments){
	                        				if (oneA.audio != null){
	                        					musicList.add(oneA.audio);
	                        					index++;
	                        					if (index>=2000){
	                        						break;
	                        					}
	                        				}
	                        			} 
                        			else
	                        			for (Attachment oneA : one.attachments){
	                        				if (oneA.audio != null){
	                        					musicList.add(oneA.audio);
	                        					index++;
	                        					if (index>=2000){
	                        						break;
	                        					}
	                        				}
	                        			}
                        			if (index>=2000){
                						break;
                					}
                        		}
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_ALBUM:
					        	musicList = api.getAudio(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), null, bundle.getLong(Constants.BUNDLE_LIST_ALBUM_ID), null, null, null);
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
					        	ArrayList<File> files = new ArrayList<File>();
                    			listf(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, "")+"/", files);
                    			//sort by old date
                    			Collections.sort(files, new Comparator<File>() {
									@Override
									public int compare(File f1, File f2) {
										if (f1.lastModified() > f2.lastModified()) return -1;
										if (f1.lastModified() < f2.lastModified()) return 1;
										return 0;
									}
                    			});
                    			String name = "", subname="";
                    			for (File oneMusicFile : files){
                    				if (oneMusicFile.getName().contains(" - ")){
                    					name = oneMusicFile.getName().substring(0, oneMusicFile.getName().indexOf(" - "));
                    					subname = oneMusicFile.getName().substring(oneMusicFile.getName().indexOf(" - ")+3, oneMusicFile.getName().length()-4);
                    				} else {
                        				if (oneMusicFile.getName().contains(" ")){
                        					name = oneMusicFile.getName().substring(0, oneMusicFile.getName().indexOf(" "));
                        					subname = oneMusicFile.getName().substring(oneMusicFile.getName().indexOf(" ")+1, oneMusicFile.getName().length()-4);
                        				} else {
                        					name = oneMusicFile.getName().substring(0, oneMusicFile.getName().length()-4);
                        					subname = "noname";
                        				}
                    				}
                    				musicList.add(new Audio((long)0, account.user_id, name, subname, Constants.LIST_APAR_NaN, oneMusicFile.getAbsolutePath(), null));
                    			}
                    			sPref.edit().putBoolean(Constants.PREFERENCES_UPDATE_DOWNLOADED_LIST, false).commit();
					        	break;
				        }
						
				        File f;
				        int idx=0;
						for (Audio one : musicList){
							f = new File(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, "")+"/"+(one.artist+" - "+one.title+".mp3").replaceAll("[\\/:*?\"<>|]", ""));
							musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, f.exists() ? f.getAbsolutePath() : one.url, one.lyrics_id, (bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID) == account.user_id && (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_OF_PAGE || bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_ALBUM)) ? Constants.LIST_ACTION_REMOVE : Constants.LIST_ACTION_ADD, !f.exists() ? Constants.LIST_ACTION_DOWNLOAD : Constants.LIST_ACTION_DELETE, Constants.LIST_APAR_NaN, null));
							idx++;
							Log.e("HEREZAZA", idx +" : " + one.artist + " " + one.title);
						}
						
						if (musicCollection.isEmpty()){
							if (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE) == Constants.BUNDLE_MUSIC_LIST_SEARCH && (bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST) == null || bundle.getString(Constants.BUNDLE_MUSIC_LIST_SEARCH_REQUEST).length()<=0))
								bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_SEARCH_REQUEST);
							else
								bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST);
						} else {
							bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR);
						}
						
						musicListAdapter.UpdateList(musicCollection);
					} catch (Exception e) {
						if (e.getMessage().contains("Access denied: access to users audio is denied")){
							bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_ACCESS_TO_USER_AUDIO_DENIED);
						} else {
							if (e.getMessage().contains("Access denied: group audio is disabled")){
								bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_GROUP_AUDIO_DISABLED);
							} else {
								if (e.getMessage().contains("Access denied: user deactivated")){
									bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_PAGE_DEACTIVATED);
								} else {
									bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_ANOTHER);
									e.printStackTrace();
								}
							}
						}
					}
					
					handler.post(new Runnable(){
						@Override
						public void run() {
							// Notify PullToRefreshLayout that the refresh has finished
		                    mPullToRefreshLayout.setRefreshComplete();
						}
					});
					
					//slep to prevent laggy animations
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					return null;
				}
				@Override
		        protected void onPostExecute(Void result) {
                    if (getActivity()!=null){
                    	//pause image loads
      					handler.removeCallbacks(resuming);
      					ImageLoader.getInstance().pause();
      					if (musicListAdapter != null){
      						musicListAdapter.updateQuality = false;
      					}
      					
                    	setUpHeaderView();
                    	musicListAdapter.notifyDataSetChanged();
                    	//with fly up animation
                    	list.setVisibility(View.VISIBLE);
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
                    	flyUpAnimation.setAnimationListener(new AnimationListener(){
							@Override
							public void onAnimationEnd(Animation arg0) {
								//update songs percentage
		        				if (isMyServiceRunning(DownloadService.class)){
		        					Intent requestDownloadStatus = new Intent(Constants.INTENT_REQUEST_DOWNLOAD_STATUS);
		        					getActivity().sendBroadcast(requestDownloadStatus);
		        				}
		        				
		        				handler.removeCallbacks(resuming);
		      					handler.postDelayed(resuming, 500);
							}
							@Override
							public void onAnimationRepeat(Animation arg0) { }
							@Override
							public void onAnimationStart(Animation arg0) { }
                    		
                    	});
                    	list.startAnimation(flyUpAnimation);
                    }
				}
			};
			
			//start async
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	        	loadM.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	        } else {
	        	loadM.execute();
	        }
		}
		
    }
    
    //get the list of files in directory
    public void listf(String directoryName, ArrayList<File> files) {
    	File directory = new File(directoryName);
    	// get all the files from a directory
    	File[] fList = directory.listFiles();
    	for (File file : fList) {
    	    if (file.isFile() && file.getName().endsWith(".mp3")) {
    	        files.add(file);
    	    } else if (file.isDirectory()) {
    	        listf(file.getAbsolutePath(), files);
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
