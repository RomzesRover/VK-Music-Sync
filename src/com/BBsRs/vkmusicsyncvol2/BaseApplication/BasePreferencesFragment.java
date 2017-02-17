package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import org.holoeverywhere.preference.PreferenceFragment;

import com.BBsRs.vkmusicsyncvol2.ContentActivity;

public class BasePreferencesFragment extends PreferenceFragment{
	
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
