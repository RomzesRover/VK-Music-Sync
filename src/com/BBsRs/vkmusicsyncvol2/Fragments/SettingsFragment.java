package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.io.File;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
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
    
    AlertDialog alert = null;
    
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
				final Context context = getActivity(); 								// create context
		 		AlertDialog.Builder build = new AlertDialog.Builder(context); 				// create build for alert dialog
		    	
		    	LayoutInflater inflater = (LayoutInflater)context.getSystemService
		    		      (Context.LAYOUT_INFLATER_SERVICE);
		    	
		    	//init views
		    	View content = inflater.inflate(R.layout.dialog_yes_no, null);
		    	TextView title = (TextView)content.findViewById(R.id.title);
		    	TextView summary = (TextView)content.findViewById(R.id.summary);
		    	Button cancel = (Button)content.findViewById(R.id.cancel);
		    	Button apply = (Button)content.findViewById(R.id.apply);
//		    	ImageView icon = (ImageView)content.findViewById(R.id.icon);
		    	
		    	//set fonts
		    	SFUIFonts.MEDIUM.apply(context, title);
		    	SFUIFonts.LIGHT.apply(context, cancel);
		    	SFUIFonts.LIGHT.apply(context, apply);
		    	SFUIFonts.LIGHT.apply(context, summary);
		    	
		    	//view job
		    	apply.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						sPref.edit().clear().commit();
						getActivity().startActivity(new Intent(getActivity(), LoaderActivity.class));
						getActivity().finish();
						alert.dismiss();
					}
				});
		    	
		    	cancel.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alert.dismiss();
					}
				});
		    	
		    	build.setView(content);
		    	alert = build.create();															// show dialog
		    	alert.show();
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
