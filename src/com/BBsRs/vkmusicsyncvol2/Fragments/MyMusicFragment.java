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

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.MusicListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Audio;

public class MyMusicFragment extends BaseFragment {
	
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
	
	//with this options we will load images
    DisplayImageOptions options ;
    
    MusicListAdapter musicListAdapter;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_my_music);
    	
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
    	
        //init views
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        if(savedInstanceState == null) {
	        //refresh on open to load data when app first time started
	        mPullToRefreshLayout.setRefreshing(true);
	        customOnRefreshListener.onRefreshStarted(null);
        } else {
        	ArrayList<MusicCollection> musicCollection = savedInstanceState.getParcelableArrayList(Constants.EXTRA_LIST_MUSIC_COLLECTION);
        	musicListAdapter = new MusicListAdapter(getActivity(), musicCollection, options);
        	list.setAdapter(musicListAdapter);
        	list.setSelection(savedInstanceState.getInt(Constants.EXTRA_LIST_POSX));
        	list.setVisibility(View.VISIBLE);
        }
    	
    	return contentView;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(getResources().getStringArray(R.array.menu)[1]);
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (musicListAdapter != null){
			outState.putParcelableArrayList(Constants.EXTRA_LIST_MUSIC_COLLECTION, musicListAdapter.getMusicCollection());
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
									Animation flyDownAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_down_anim);
			                    	list.startAnimation(flyDownAnimation);
			                    	list.setVisibility(View.INVISIBLE);
								}
							});
						}
						
						//slep to prevent laggy animations
						Thread.sleep(1000);
						
						ArrayList<Audio> musicList = new ArrayList<Audio>();
						ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
						
						musicList = api.getAudio(account.user_id, null, null, null, null, null);
						
						for (Audio one : musicList){
							musicCollection.add(new MusicCollection(one.aid, one.owner_id, one.artist, one.title, one.duration, one.url, one.lyrics_id));
						}
						
						musicListAdapter = new MusicListAdapter(getActivity(), musicCollection, options);
					} catch (Exception e) {
						e.printStackTrace();
						error = true;
					}
					return null;
				}
				@Override
		        protected void onPostExecute(Void result) {
					// Notify PullToRefreshLayout that the refresh has finished
                    mPullToRefreshLayout.setRefreshComplete();
                    
                    if (!error){
                    	list.setAdapter(musicListAdapter);
                    	//with fly up animation
                    	list.setVisibility(View.VISIBLE);
                    	Animation flyUpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fly_up_anim);
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
