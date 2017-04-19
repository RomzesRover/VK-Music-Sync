package com.BBsRs.vkmusicsyncvol2.Fragments;

import java.io.File;
import java.util.Calendar;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceClickListener;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;
import org.jsoup.Jsoup;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.LoaderActivity;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BasePreferencesFragment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;

public class SettingsFragment extends BasePreferencesFragment {
	
	SharedPreferences sPref;
	
    //for retrieve data from activity
    Bundle bundle;
    
    Preference stopDownload, logout, downloadDirectory, prep;
    
    AlertDialog alert = null;
    
    int clicks = 0;
    private final static Handler handler = new Handler();
    ProgressDialog progressDialog = null;
    /*----------------------------VK API-----------------------------*/
    Account account=new Account();
    /*----------------------------VK API-----------------------------*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        
        //retrieve bundle
      	bundle = this.getArguments();
      	
        addPreferencesFromResource(R.xml.settings_preferences);
        
    	//init vkapi
	    account.restore(getActivity());
        
        progressDialog = new ProgressDialog(getActivity());
        
        //init
        downloadDirectory = (Preference) findPreference(Constants.PREFERENCES_DOWNLOAD_DIRECTORY);
        stopDownload = (Preference)findPreference("preference:stop_download");
        logout = (Preference)findPreference("preference:logout");
        prep = (Preference)findPreference("preferences:prep_getter");
        
        //pref job
        prep.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
			public boolean onPreferenceClick(Preference preference) {
				clicks++;
				
				if (clicks == 5){
					clicks = 0;
					
			        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			        	new premiumCheckTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, account);
			        } else {
			        	new premiumCheckTask().execute(account);
			        }
				}
				
				handler.removeCallbacks(nullClicks);
				handler.postDelayed(nullClicks, 2000);
				return false;
			}
        });
        
        downloadDirectory.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Context context = getActivity(); 								// create context
		 		AlertDialog.Builder build = new AlertDialog.Builder(context); 				// create build for alert dialog
		    	
		    	LayoutInflater inflater = (LayoutInflater)context.getSystemService
		    		      (Context.LAYOUT_INFLATER_SERVICE);
		    	
		    	//init views
		    	View content = inflater.inflate(R.layout.dialog_edit_text, null);
		    	TextView title = (TextView)content.findViewById(R.id.title);
		    	final EditText directory = (EditText)content.findViewById(R.id.edit_text);
		    	Button cancel = (Button)content.findViewById(R.id.cancel);
		    	Button apply = (Button)content.findViewById(R.id.apply);
//		    	ImageView icon = (ImageView)content.findViewById(R.id.icon);
		    	
		    	//set fonts
		    	SFUIFonts.MEDIUM.apply(context, title);
		    	SFUIFonts.LIGHT.apply(context, cancel);
		    	SFUIFonts.LIGHT.apply(context, apply);
		    	SFUIFonts.LIGHT.apply(context, directory);
		    	
		    	//view job
		    	directory.setText(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, ""));
		    	
		    	apply.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						//check current download directory to availability 
					    File checker = null;
					    try {
				    		checker = new File(directory.getText().toString()+"/1.txt");
				    		checker.mkdirs();
				    		checker.createNewFile();
				    		checker.delete();
				    		sPref.edit().putString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, directory.getText().toString()+"/").commit();
				    		updateSummary();
				    		alert.dismiss();
				    	} catch (Exception e){
				    		Toast.makeText(getActivity(), getActivity().getString(R.string.preferences_download_directory_unavailable), Toast.LENGTH_LONG).show();
				    	}
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
						alert.dismiss();
						getActivity().startActivity(new Intent(getActivity(), LoaderActivity.class));
						getActivity().finish();
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
    
	Runnable nullClicks = new Runnable() {
        public void run() {
            clicks=0;
        }
    };
    
    @Override
    public void onResume() {
        super.onResume();
        //set subtitle for a current fragment with custom font
        setTitle(bundle.getString(Constants.BUNDLE_LIST_TITLE_NAME));
        getListView().setSelector(R.drawable.purple_list_selector_holo_light);
        updateSummary();
        //
        clicks = 0;
    }
    
    public void updateSummary(){
    	downloadDirectory.setSummary(sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, ""));
    	logout.setSummary(sPref.getString(Constants.PREFERENCES_USER_FIRST_NAME, "no value") + " " + sPref.getString(Constants.PREFERENCES_USER_LAST_NAME, "no value"));
    }
    
	class premiumCheckTask extends AsyncTask<Account, String, Boolean>{
		
		@Override
		protected Boolean doInBackground(Account... arg0) {
			
	        handler.post(new Runnable(){
				@Override
				public void run() {
					//show an dialog intermediate 
			        progressDialog.setIndeterminate(true);
			        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			        progressDialog.setMessage(getText(R.string.preferences_checking_prep));
			        progressDialog.setCancelable(false);
			        progressDialog.setCanceledOnTouchOutside(false);
			        try {
			        	progressDialog.show();
			    	} catch (Exception e){
			    		e.printStackTrace();
			    	}
				}
			});
	        
	        Boolean result = sPref.getBoolean(Constants.PREFERENCES_PREP_STATUS, false);
			try {
				int summ = 0;
				long id = arg0[0].user_id;
				
				while (id!=0){
					summ += id % 10;
					id = id / 10;
				}
				
				String premData = Jsoup.connect(String.format(Constants.PREMIUM_GETTER_NEW_EDITION, arg0[0].user_id)).timeout(10000).get().text();
				
				if (Integer.parseInt(premData.split(";")[0])==1){
					Calendar currentDate = Calendar.getInstance();
					currentDate.setTimeInMillis(System.currentTimeMillis());
					
					Calendar UntilDate = Calendar.getInstance();
					UntilDate.setTimeInMillis(System.currentTimeMillis());
					UntilDate.set(Integer.parseInt(premData.split(";")[1].split(",")[0]), Integer.parseInt(premData.split(";")[1].split(",")[1])-1, Integer.parseInt(premData.split(";")[1].split(",")[2]));
					
					
					if (currentDate.before(UntilDate)){
						//user have premium, check license
						result = Integer.parseInt(premData.split(";")[2]) == summ ? true : false;
					} else {
						//user's premium expired
						result = false;
					}
				} else {
					//user with this id isn't exist in base
					result = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				result = sPref.getBoolean(Constants.PREFERENCES_PREP_STATUS, false);
	        }
	        return result;
		}
		
	    @Override
	    protected void onPostExecute(Boolean result) {
	        super.onPostExecute(result);
	        progressDialog.dismiss();
	        sPref.edit().putBoolean(Constants.PREFERENCES_PREP_STATUS, result);
	        if (result){
	        	Toast.makeText(getActivity(), getString(R.string.preferences_prep_ok), Toast.LENGTH_LONG).show();
	        }
	        else {
	        	Toast.makeText(getActivity(), getString(R.string.preferences_prep_notok), Toast.LENGTH_LONG).show();
	        }
	    }
	}
}
