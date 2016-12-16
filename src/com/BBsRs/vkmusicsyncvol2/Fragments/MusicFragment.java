package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;

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
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
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
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.MusicListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;

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
    
    //for retrieve data from activity
    Bundle bundle;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_music_frgr);
    	
    	//set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    
		//enable menu
    	setHasOptionsMenu(true);
    	
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
    	
        //init views
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	header = inflater.inflate(R.layout.list_music_header);
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.albums));
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.wall));
    	((LinearLayout)header.findViewById(R.id.wallLayout)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(getActivity(), "open wall", Toast.LENGTH_LONG).show();
			}
		});
    	list.addHeaderView(header);
    	list.setAdapter(musicListAdapter);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //retrieve bundle
      	bundle = this.getArguments();
        
        if(savedInstanceState == null) {
          	handler.postDelayed(new Runnable(){
    			@Override
    			public void run() {
    				//refresh on open to load data when app first time started
    		        mPullToRefreshLayout.setRefreshing(true);
    		        customOnRefreshListener.onRefreshStarted(null);
    			}
          	}, 100);
        } else {
        	ArrayList<MusicCollection> musicCollection = savedInstanceState.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
        	musicListAdapter.UpdateList(musicCollection);
        	musicListAdapter.notifyDataSetChanged();
        	list.setSelection(savedInstanceState.getInt(Constants.EXTRA_LIST_POSX));
        	list.setVisibility(View.VISIBLE);
        }
    	
    	return contentView;
	}
	
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
  				}
  				return false;
  			}
  			@Override
  			public boolean onQueryTextChange(String newText) {
  				if (musicListAdapter != null && musicListAdapter.getCountNonFiltered() !=0){
  					musicListAdapter.getFilter().filter(newText);
  					list.setSelection(0);
  				}
  				return false;
  			}});
  		
  		setSearchStyles();
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
	        closeBtn.setBackground(null);
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
            View searchSrcText = (View) searchView.findViewById(R.id.search_src_text);   
            LayoutParams layoutParams = searchSrcText.getLayoutParams();
            layoutParams.height = (int) (29 * getActivity().getResources().getDisplayMetrics().density + 0.5f);
            searchSrcText.setLayoutParams(layoutParams);
            //set text font
            final SearchAutoComplete mQueryTextView = (SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
            SFUIFonts.ULTRALIGHT.apply(getActivity(), mQueryTextView);
            mQueryTextView.setHint("");
            mQueryTextView.setTextColor(getActivity().getResources().getColor(R.color.white_color));
            mQueryTextView.setTextSize((float)17);
		} catch (Exception e) {
			Log.e("SearchView", e.getMessage(), e);
		}
	}
	
	private BroadcastReceiver hideSearchKeyboard = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
        	//hide search, keyboard if its opened
        	if (searchView != null){
  				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
  				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
				}
	    }
	};
	
    @Override
    public void onResume() {
        super.onResume();
        //reg receivers
        getActivity().registerReceiver(hideSearchKeyboard, new IntentFilter(Constants.BROADCAST_RECEIVER_HIDE_SEARCH_KEYBOARD));
        
        //set subtitle for a current fragment with custom font
        switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
	        default: case Constants.BUNDLE_MUSIC_LIST_MY_MUSIC:
	        	setTitle(getResources().getStringArray(R.array.menu)[1]);
	        	break;
	        case Constants.BUNDLE_MUSIC_LIST_POPULAR:
	        	setTitle(getResources().getStringArray(R.array.menu)[3]);
	        	break;
	        case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
	        	setTitle(getResources().getStringArray(R.array.menu)[4]);
	        	break;
	        case Constants.BUNDLE_MUSIC_LIST_SEARCH:
	        	setTitle(getResources().getStringArray(R.array.menu)[2]);
	        	break;
	        case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
	        	setTitle(getResources().getStringArray(R.array.menu)[7]);
	        	break;
	        case Constants.BUNDLE_MUSIC_LIST_FRIEND: case Constants.BUNDLE_MUSIC_LIST_GROUP:
	        	setTitle(bundle.getString(Constants.BUNDLE_MUSIC_LIST_FRGR_NAME));
	        	break;
        }
    }
    
	@Override
	public void onPause() {
		super.onPause();
		//unreg receivers
		getActivity().unregisterReceiver(hideSearchKeyboard);
	}
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (musicListAdapter != null){
			outState.putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, musicListAdapter.getMusicCollectionNonFiltered());
			outState.putInt(Constants.EXTRA_LIST_POSX,  list.getFirstVisiblePosition());
		}
	}
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public class CustomOnRefreshListener  implements OnRefreshListener{
		@Override
		public void onRefreshStarted(View view) {
			AsyncTask<Void, Void, Void> loadM = new AsyncTask<Void, Void, Void>() {
				
				boolean error = false;
				
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
			                    	list.setVisibility(View.INVISIBLE);
			                    	
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
						Thread.sleep(100);
						
						ArrayList<Audio> musicList = new ArrayList<Audio>();
						ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
						
						//load nesc music
				        switch (bundle.getInt(Constants.BUNDLE_MUSIC_LIST_TYPE)){
					        default: case Constants.BUNDLE_MUSIC_LIST_MY_MUSIC:
					        	musicList = api.getAudio(account.user_id, null, null, null, null, null);
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_POPULAR:
					        	musicList = api.getAudioPopular(0, null, null, null);
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_RECOMMENDATIONS:
					        	musicList = api.getAudioRecommendations();
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_SEARCH:
					        	//TODO
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_FRIEND:
					        	musicList = api.getAudio(bundle.getLong(Constants.BUNDLE_MUSIC_LIST_FRGR_ID), null, null, null, null, null);
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_GROUP:
					        	musicList = api.getAudio(null, bundle.getLong(Constants.BUNDLE_MUSIC_LIST_FRGR_ID), null, null, null, null);
					        	break;
					        case Constants.BUNDLE_MUSIC_LIST_DOWNLOADED:
					        	//TODO
					        	break;
				        }
						
						
						
						for (Audio one : musicList){
							musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id));
						}
						
						musicListAdapter.UpdateList(musicCollection);
					} catch (Exception e) {
						e.printStackTrace();
						error = true;
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
						Thread.sleep(150);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return null;
				}
				@Override
		        protected void onPostExecute(Void result) {
                    if (!error){
                    	musicListAdapter.notifyDataSetChanged();
                    	//with fly up animation
                    	list.setVisibility(View.VISIBLE);
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
                    	list.startAnimation(flyUpAnimation);
                    } else {
                    	//TODO SHOW error message
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
    
    
}
