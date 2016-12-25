package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.FrGrListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.FrGrCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.perm.kate.api.Api;
import com.perm.kate.api.Group;
import com.perm.kate.api.User;

public class FrGrFragment extends BaseFragment {
	
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
    
    FrGrListAdapter frGrListAdapter;
    
    //for retrieve data from activity
    Bundle bundle;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_music_frgr);
    	
    	//set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	
    	//init vkapi
	    account.restore(getActivity());
        api=new Api(account.access_token, Constants.CLIENT_ID);
        
        //init image loader
        options = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.nopic)
        .cacheInMemory(true)					
        .build();
        
        //init adapter with null
        frGrListAdapter = new FrGrListAdapter(getActivity(), null, options);
    	
        //init views
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	list.setAdapter(frGrListAdapter);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //retrieve bundle
      	bundle = this.getArguments();
        
		if(savedInstanceState == null &&  bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS) == null) {
	      	handler.postDelayed(new Runnable(){
				@Override
				public void run() {
			        //refresh on open to load data when app first time started
			        mPullToRefreshLayout.setRefreshing(true);
			        customOnRefreshListener.onRefreshStarted(null);
				}
	      	}, 100);
        } else {
        	if (savedInstanceState != null){
	        	ArrayList<FrGrCollection> frGrCollection = savedInstanceState.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
	        	frGrListAdapter.UpdateList(frGrCollection);
	        	frGrListAdapter.notifyDataSetChanged();
	        	list.setSelection(savedInstanceState.getInt(Constants.EXTRA_LIST_POSX));
        	} else {
        		ArrayList<FrGrCollection> frGrCollection = bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
        		frGrListAdapter.UpdateList(frGrCollection);
	        	frGrListAdapter.notifyDataSetChanged();
	        	list.setSelection(bundle.getInt(Constants.EXTRA_LIST_POSX));
        	}
        	list.setVisibility(View.VISIBLE);
        }
		
		//init this fragment to use in methods
		final Fragment thisFr = this;
		
		//view job
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//create bundle to m list
				Bundle frGrMusicBundle  = new Bundle();
				
				//set up bundle
		        switch (bundle.getInt(Constants.BUNDLE_FRGR_LIST_TYPE)){
			        case Constants.BUNDLE_FRGR_LIST_FRIENDS:
			        	frGrMusicBundle.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, frGrListAdapter.getItem(position).fgid);
			        	break;
			        case Constants.BUNDLE_FRGR_LIST_GROUPS:
			        	frGrMusicBundle.putLong(Constants.BUNDLE_LIST_USRFRGR_ID, -frGrListAdapter.getItem(position).fgid);
			        	break;
		        }
		        frGrMusicBundle.putInt(Constants.BUNDLE_MUSIC_LIST_TYPE, Constants.BUNDLE_MUSIC_LIST_OF_PAGE);
		        frGrMusicBundle.putString(Constants.BUNDLE_LIST_TITLE_NAME, frGrListAdapter.getItem(position).friendGroupName);
		        
		        //create music list fragment
		        MusicFragment musicListFragment = new MusicFragment();
	           	musicListFragment.setArguments(frGrMusicBundle);
	           	
	           	//start new music list fragment
	           	thisFr.onPause();
				
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(musicListFragment);
			}
		});
		
    	return contentView;
	}
	
    @Override
    public void onPause() {
        super.onPause();
		if (frGrListAdapter != null){
			getArguments().putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, frGrListAdapter.getFrGrCollection());
			getArguments().putInt(Constants.EXTRA_LIST_POSX,  list.getFirstVisiblePosition());
		}
    }
	
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
    }
    
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (frGrListAdapter != null){
			outState.putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, frGrListAdapter.getFrGrCollection());
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
								}
							});
						}
						
						//slep to prevent laggy animations
						Thread.sleep(100);
						
						ArrayList<FrGrCollection> frGrCollection = new ArrayList<FrGrCollection>();
						
						//load nesc frgr list
				        switch (bundle.getInt(Constants.BUNDLE_FRGR_LIST_TYPE)){
					        case Constants.BUNDLE_FRGR_LIST_FRIENDS:
					        	ArrayList<User> friendList = new ArrayList<User>();
					        	friendList = api.getFriends(account.user_id, "photo_200,photo_100", null, null, null);
								for (User one : friendList){
									frGrCollection.add(new FrGrCollection(one.uid, one.first_name + " " + one.last_name, ((one.photo_200 == null || one.photo_200.length()<1) ? one.photo_medium_rec : one.photo_200), one.online ? 1 : 0));
								}
					        	break;
					        case Constants.BUNDLE_FRGR_LIST_GROUPS:
					        	ArrayList<Group> groupList = new ArrayList<Group>();
					        	groupList = api.getUserGroups(account.user_id);
								for (Group one : groupList){
									frGrCollection.add(new FrGrCollection(one.gid, one.name, one.photo_big, -1));
								}
					        	break;
				        }
						
				        frGrListAdapter.UpdateList(frGrCollection);
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
                    	frGrListAdapter.notifyDataSetChanged();
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
