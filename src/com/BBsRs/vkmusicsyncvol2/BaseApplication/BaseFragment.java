package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import org.holoeverywhere.app.Fragment;

import com.BBsRs.vkmusicsyncvol2.ContentActivity;

public class BaseFragment extends Fragment{
	
	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().setSubtitle(null);
		getSupportActionBar().setTitle(null);
		
	}     		
	
    public void setTitle(String title){
    	((ContentActivity) getSupportActivity()).setTitle(title);
    }
}
