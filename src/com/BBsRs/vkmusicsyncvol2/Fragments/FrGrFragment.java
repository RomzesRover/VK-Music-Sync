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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.ContentActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.Adapters.FrGrListAdapter;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.FrGrCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
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
	View header;
	
	//with this options we will load images
    DisplayImageOptions options ;
    
    FrGrListAdapter frGrListAdapter;
    
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
    	list.setAdapter(frGrListAdapter);
    	
		//view job
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int _position, long arg3) {
				int position = _position - 1;
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
				((ContentActivity) getSupportActivity()).addonSlider().obtainSliderMenu().replaceFragment(musicListFragment);
			}
		});
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
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					handler.removeCallbacks(resuming);
					ImageLoader.getInstance().pause();
					break;
				}
			}
		});
    	
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
        	ArrayList<FrGrCollection> frGrCollection = bundle.getParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS);
    		frGrListAdapter.UpdateList(frGrCollection);
        	frGrListAdapter.notifyDataSetChanged();
        	
        	setUpHeaderView();
        	list.setVisibility(View.VISIBLE);
        }
		
    	return contentView;
	}
	
	final Runnable resuming = new Runnable(){
		@Override
		public void run() {
			//resume update image
			ImageLoader.getInstance().resume();
		}
	};
	
    @Override
    public void onPause() {
        super.onPause();
		//pause image loads
		handler.removeCallbacks(resuming);
		ImageLoader.getInstance().stop();
		
		if (frGrListAdapter != null){
			getArguments().putParcelableArrayList(Constants.EXTRA_LIST_COLLECTIONS, frGrListAdapter.getFrGrCollection());
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
        //resume loads images
		handler.removeCallbacks(resuming);
		handler.postDelayed(resuming, 500);
		
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
    		switch (bundle.getInt(Constants.BUNDLE_FRGR_LIST_TYPE)){
	        case Constants.BUNDLE_FRGR_LIST_FRIENDS:
	        	((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_no_friends));
	        	break;
	        case Constants.BUNDLE_FRGR_LIST_GROUPS:
	        	((TextView)header.findViewById(R.id.errr)).setText(getActivity().getResources().getString(R.string.content_activity_no_groups));
	        	break;
    		}
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
				        
				        if (frGrCollection.isEmpty()){
				        	bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_EMPTY_LIST);
						} else {
							bundle.putInt(Constants.BUNDLE_LIST_ERROR_CODE, Constants.BUNDLE_LIST_ERROR_CODE_NO_ERROR);
						}
				        
				        frGrListAdapter.UpdateList(frGrCollection);
					} catch (Exception e) {
						frGrListAdapter.UpdateList(new ArrayList<FrGrCollection>());
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
							//pause image loads
	      					handler.removeCallbacks(resuming);
	      					ImageLoader.getInstance().pause();
							//
	      					setUpHeaderView();
	                    	frGrListAdapter.notifyDataSetChanged();
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
                    	flyUpAnimation.setAnimationListener(new AnimationListener(){
							@Override
							public void onAnimationEnd(Animation arg0) {
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
    
    
}
