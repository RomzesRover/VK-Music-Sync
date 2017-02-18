package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.io.File;

import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;

import com.BBsRs.vkmusicsyncvol2.LoaderActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BasePreferencesFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;

public class SettingsFragment extends BasePreferencesFragment {
	
	SharedPreferences sPref;
	
    //for retrieve data from activity
    Bundle bundle;
    
    EditTextPreference downloadDirectory;
    Preference stopDownload, logout;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        //retrieve bundle
      	bundle = this.getArguments();
      	
        addPreferencesFromResource(R.xml.settings_preferences);
        
        //init
        downloadDirectory = (EditTextPreference) findPreference(Constants.PREFERENCES_DOWNLOAD_DIRECTORY);
        stopDownload = (Preference)findPreference("preference:stop_download");
        logout = (Preference)findPreference("preference:logout");
        
        //pref job
        downloadDirectory.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				//check current download directory to availability 
			    File checker = null;
			    try {
		    		checker = new File(newValue.toString()+"/1.txt");
		    		checker.mkdirs();
		    		checker.createNewFile();
		    		checker.delete();
		    		sPref.edit().putString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, newValue.toString()+"/").commit();
		    		updateSummary();
		    		return true;
		    	} catch (Exception e){
		    		Toast.makeText(getActivity(), getActivity().getString(R.string.preferences_download_directory_unavailable), Toast.LENGTH_LONG).show();
		    		return false;
		    	}
			}
        });
        
        stopDownload.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent stopDownloadInt = new Intent(Constants.INTENT_STOP_DOWNLOAD);
				getActivity().sendBroadcast(stopDownloadInt);
				Toast.makeText(getActivity(), getActivity().getString(R.string.preferences_stop_download_success), Toast.LENGTH_LONG).show();
				return false;
			}
        });
        
        logout.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				sPref.edit().clear().commit();
				getActivity().startActivity(new Intent(getActivity(), LoaderActivity.class));
				getActivity().finish();
				return false;
			}
        });
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
        getListView().setSelector(R.drawable.purple_list_selector_holo_light);
        updateSummary();
    }
    
    public void updateSummary(){
    	downloadDirectory.setSummary(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, ""));
    	logout.setSummary(sPref.getString(Constants.PREFERENCES_USER_FIRST_NAME, "no value") + " " + sPref.getString(Constants.PREFERENCES_USER_LAST_NAME, "no value"));
    }
}
