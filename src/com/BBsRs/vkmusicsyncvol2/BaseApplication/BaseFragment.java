package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.TextView;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.View;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.R;

public class BaseFragment extends Fragment{
	
	@Override
	public void onResume() {
		super.onResume();
		
		getSupportActionBar().setSubtitle(null);
		getSupportActionBar().setTitle(null);
		
	}     		
	
	
    @SuppressLint("InflateParams") 
    public void setTitle(String title){
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		View actionTitle = getLayoutInflater().inflate(R.layout.action_bar, null);
		//set font
		SFUIFonts.ULTRALIGHT.apply(getActivity(), ((TextView)actionTitle.findViewById(R.id.titleActionBar)));
		SFUIFonts.ULTRALIGHT.apply(getActivity(), ((TextView)actionTitle.findViewById(R.id.subtitleActionBar)));
		//separate text
		String[] titles = title.split(";");
		if (titles.length==1){
			((TextView)actionTitle.findViewById(R.id.titleActionBar)).setText(titles[0]);
			((TextView)actionTitle.findViewById(R.id.subtitleActionBar)).setVisibility(View.GONE);
		} else {
			((TextView)actionTitle.findViewById(R.id.titleActionBar)).setText(titles[0]);
			((TextView)actionTitle.findViewById(R.id.subtitleActionBar)).setText(titles[1]);
			((TextView)actionTitle.findViewById(R.id.subtitleActionBar)).setVisibility(View.VISIBLE);
		}
		actionBar.setCustomView(actionTitle,
		        new ActionBar.LayoutParams(
		                ActionBar.LayoutParams.WRAP_CONTENT,
		                ActionBar.LayoutParams.MATCH_PARENT,
		                Gravity.CENTER
		        )
		);
    }

}
