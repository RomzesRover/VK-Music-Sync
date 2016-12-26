package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

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
        	
        	list.setVisibility(View.VISIBLE);
        }
		
		//view job
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
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
    }
	
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
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
						Thread.sleep(100);
						
						ArrayList<AlbumCollection> albumCollection = new ArrayList<AlbumCollection>();
						
						//load nesc frgr list
						for (AudioAlbum one : api.getAudioAlbums(bundle.getLong(Constants.BUNDLE_LIST_USRFRGR_ID), 0, 100)){
                	    	albumCollection.add(new AlbumCollection(one.album_id, one.owner_id, one.title));
                	    }
						
				        albumListAdapter.UpdateList(albumCollection);
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
                    if (!error && getActivity()!=null){
                    	albumListAdapter.notifyDataSetChanged();
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
