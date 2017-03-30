package com.BBsRs.vkmusicsyncvol2.Fragments;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.AlbumListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.AlbumCollection;
import com.perm.kate.api.Api;
import com.perm.kate.api.AudioAlbum;

public class AlbumsFragment extends BaseFragment {
	
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
	
    AlbumListAdapter albumListAdapter;
    
    //for retrieve data from activity
    Bundle bundle;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_music_frgr_album);
    	
    	//set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	
    	//init vkapi
	    account.restore(getActivity());
        api=new Api(account.access_token, Constants.CLIENT_ID);
        
        //init adapter with null
        albumListAdapter = new AlbumListAdapter(getActivity(), null);
    	
        //init views
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	//init header buttons
    	header = inflater.inflate(R.layout.list_music_header);
    	SFUIFonts.MEDIUM.apply(getActivity(), (TextView)header.findViewById(R.id.errr));
    	((LinearLayout)header.findViewById(R.id.errorLayout)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(getActivity(), getActivity().getString(R.string.content_activity_error_info), Toast.LENGTH_LONG).show();
			}
		});
    	list.addHeaderView(header);
    	list.setAdapter(albumListAdapter);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //retrieve bundle
      	bundle = this.getArguments();
        
		if(bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS) == null) {
	      	handler.postDelayed(new Runnable(){
				@Override
				public void run() {
			        //refresh on open to load data when app first time started
			        mPullToRefreshLayout.setRefreshing(true);
			        customOnRefreshListener.onRefreshStarted(null);
				}
	      	}, 100);
        } else {
        	ArrayList<AlbumCollection> albumCollection = bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
    		albumListAdapter.UpdateList(albumCollection);
        	albumListAdapter.notifyDataSetChanged();
        	
        	setUpHeaderView();
        	list.setVisibility(View.VISIBLE);
        }
		
		//view job
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int _position, long arg3) {
				int position = _position - 1;
				//create bundle to m list
				Bundle albumMusicBundle  = new Bundle();
				
				//set up bundle
				albumMusicBundle.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_ALBUM);
				albumMusicBundle.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, albumListAdapter.getItem(position).owner_id);
				albumMusicBundle.putLong(Constants.BUNDLE_LIST_ALBUM_ID, albumListAdapter.getItem(position).album_id);
				albumMusicBundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME) + " - " + albumListAdapter.getItem(position).title);
		        
		        //create music list fragment
		        MusicFragment musicListFragment = new MusicFragment();
	           	musicListFragment.setArguments(albumMusicBundle);
	           	
	           	//start new music list fragment
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(musicListFragment);
			}
		});
		
    	return contentView;
	}
	
    @Override
    public void onPause() {
        super.onPause();
		if (albumListAdapter != null){
			getArguments().putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, albumListAdapter.getAlbumCollection());
		}
		
		if (loadM != null){
			loadM.cancel(true);
		}
		
		getActivity().unregisterReceiver(forceShowUpdateLine);
		getActivity().unregisterReceiver(forceHideUpdateLine);
    }
	
    @Override
    public void onResume() {
        super.onResume();
        
        getActivity().registerReceiver(forceShowUpdateLine, new IntentFilter(Constants.INTENT_FORCE_SHOW_UPDATE_LINE));
    	getActivity().registerReceiver(forceHideUpdateLine, new IntentFilter(Constants.INTENT_FORCE_HIDE_UPDATE_LINE));
    	
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
    }
    
	private BroadcastReceiver forceShowUpdateLine = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			boolean active = loadM != null && loadM.getStatus() != AsyncTask.Status.FINISHED;
			
			if (active)
				mPullToRefreshLayout.setRefreshing(true);
		}
	};
	
	
	private BroadcastReceiver forceHideUpdateLine = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mPullToRefreshLayout.setRefreshComplete();
		}
	};
    
	public void setUpHeaderView(){
		if (header == null) return;
		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.GONE);
		((LinearLayout)header.findViewById(R.id.wallLayout)).setVisibility(View.GONE);
		((LinearLayout)header.findViewById(R.id.recommendationsLayout)).setVisibility(View.GONE);
		((LinearLayout)header.findViewById(R.id.albumsLayout)).setVisibility(View.GONE);
		
        switch (bundle.getInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR)){
        case Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR:
        	((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.GONE);
        	break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST:
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_no_albums));
    		break;
    	case Constants.BUNDLE_LIST_ERROR_CODE_ANOTHER:
    		((LinearLayout)header.findViewById(R.id.errorLayout)).setVisibility(View.VISIBLE);
    		((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_error));
    		break;
    	}
	}
    
	AsyncTask<Void, Void, Void> loadM;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public class CustomOnRefreshListener  implements OnRefreshListener{
		@Override
		public void onRefreshStarted(View view) {
			loadM = new AsyncTask<Void, Void, Void>() {
				
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
								}
							});
						}
						
						//slep to prevent laggy animations
						Thread.sleep(250);
						
						//null lists
						ArrayList<AlbumCollection> albumCollection = new ArrayList<AlbumCollection>();
						
						//load nesc album list
						for (AudioAlbum one : api.getAudioAlbums(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), 0, 100)){
                	    	albumCollection.add(new AlbumCollection(one.album_id, one.owner_id, one.title));
                	    }
						
						if (albumCollection.isEmpty()){
					       	bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST);
						} else {
							bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR);
						}
						
				        albumListAdapter.UpdateList(albumCollection);
					} catch (Exception e) {
						albumListAdapter.UpdateList(new ArrayList<AlbumCollection>());
						bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_ANOTHER);
						e.printStackTrace();
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
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					handler.post(new Runnable(){
						@Override
						public void run() {
							//
	      					setUpHeaderView();
	                    	albumListAdapter.notifyDataSetChanged();
						}
					});
					
					//slep to prevent laggy animations
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					return null;
				}
				@Override
		        protected void onPostExecute(Void result) {
                    if (getActivity()!=null){
                    	//with fly up animation
                    	list.setVisibility(View.VISIBLE);
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
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
    
    
}
