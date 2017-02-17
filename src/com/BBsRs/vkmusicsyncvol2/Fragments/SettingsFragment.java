package com.BBsRs.vkmusicsyncvol2.Fragments;

import android.os.Bundle;

import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BasePreferencesFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;

public class SettingsFragment extends BasePreferencesFragment {
	
    //for retrieve data from activity
    Bundle bundle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //retrieve bundle
      	bundle = this.getArguments();
      	
        addPreferencesFromResource(R.xml.settings_preferences);
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
        getListView().setSelector(R.drawable.purple_list_selector_holo_light);
    }
}
