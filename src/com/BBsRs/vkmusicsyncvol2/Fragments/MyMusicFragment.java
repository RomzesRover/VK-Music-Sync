package com.BBsRs.vkmusicsyncvol2.Fragments;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseFragment;

public class MyMusicFragment extends BaseFragment {
	
    //custom refresh listener where in new thread will load job doing, need to customize for all kind of data
    CustomOnRefreshListener customOnRefreshListener = new CustomOnRefreshListener();
	PullToRefreshLayout mPullToRefreshLayout;
	ListView list;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View contentView = inflater.inflate(R.layout.fragment_my_music);
    	
    	mPullToRefreshLayout = (PullToRefreshLayout) contentView.findViewById(R.id.ptr_layout);
    	list = (ListView)contentView.findViewById(R.id.list);
    	
        //init pull to refresh module
        ActionBarPullToRefresh.from(getActivity())
          .allChildrenArePullable()
          .listener(customOnRefreshListener)
          .setup(mPullToRefreshLayout);
        
        //refresh on open to load data when app first time started
        mPullToRefreshLayout.setRefreshing(true);
        customOnRefreshListener.onRefreshStarted(null);
    	
    	return contentView;
	}
	
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(getResources().getStringArray(R.array.menu)[1]);
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public class CustomOnRefreshListener  implements OnRefreshListener{
		@Override
		public void onRefreshStarted(View view) {
			AsyncTask<Void, Void, Void> loadM = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}
				@Override
		        protected void onPostExecute(Void result) {
					// Notify PullToRefreshLayout that the refresh has finished
                    mPullToRefreshLayout.setRefreshComplete();
					
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
