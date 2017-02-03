package com.BBsRs.vkmusicsyncvol2;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;

import android.content.Intent;
import android.os.Bundle;

import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.CustomEnvironment;



public class LoaderActivity extends Activity {
	
	Account account = new Account();
	SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account.restore(this);
        
        if (account.access_token == null){
        	super.startActivity(new Intent(this, LoginActivity.class));
        	finish();
        }
        else {
        	//set default download folder:
        	sPref = PreferenceManager.getDefaultSharedPreferences(this);
        	if (sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null) == null){
        		sPref.edit().putString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, (new CustomEnvironment(this)).DownloadDirectoryDecide()).commit();
        	}
        	
        	super.startActivity(new Intent(this, ContentActivity.class));
        	finish();
        }
    }
}
