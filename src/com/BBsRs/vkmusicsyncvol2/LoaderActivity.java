package com.BBsRs.vkmusicsyncvol2;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.Toast;

import android.content.Intent;
import android.os.Bundle;

import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;



public class LoaderActivity extends Activity {
	
	Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account.restore(this);
        
        if (System.currentTimeMillis()>=1487530800000L){
        	Toast.makeText(getApplicationContext(), "Alpha test of this version has end. Download new version, Thank you", Toast.LENGTH_LONG).show();
        } else {
	        if (account.access_token == null){
	        	super.startActivity(new Intent(this, LoginActivity.class));
	        	finish();
	        }
	        else {
	        	super.startActivity(new Intent(this, ContentActivity.class));
	        	finish();
	        }
        }
    }
}
