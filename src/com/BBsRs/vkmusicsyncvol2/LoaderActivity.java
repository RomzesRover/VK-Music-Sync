package com.BBsRs.vkmusicsyncvol2;

import org.holoeverywhere.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;



public class LoaderActivity extends Activity {
	
	Account account = new Account();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account.restore(this);
        
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
