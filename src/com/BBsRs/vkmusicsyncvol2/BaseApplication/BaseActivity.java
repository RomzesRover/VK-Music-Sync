package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import java.util.Calendar;
import java.util.Date;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.TextView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class BaseActivity extends Activity{
	
	AdView adView;

	@Override
	public void onResume() {
		super.onResume();
		if (getSupportActionBar() != null) {
			getSupportActionBar().setSubtitle(null);
			getSupportActionBar().setTitle(null);
		}

		if (adView != null)
			adView.resume();
	}
	
	@Override
	public void onPause() {
		if (adView != null)
			adView.pause();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (adView != null)
			adView.destroy();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		//init views
		actionTitle = getLayoutInflater().inflate(R.layout.action_bar, null);
		maintitle = ((TextView)actionTitle.findViewById(R.id.titleActionBar));
		subtitle = ((TextView)actionTitle.findViewById(R.id.subtitleActionBar));
		
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(actionTitle,
		        new ActionBar.LayoutParams(
		                ActionBar.LayoutParams.WRAP_CONTENT,
		                ActionBar.LayoutParams.MATCH_PARENT,
		                Gravity.CENTER
		        )
		);
		//set font
		SFUIFonts.ULTRALIGHT.apply(BaseActivity.this, maintitle);
		SFUIFonts.ULTRALIGHT.apply(this, subtitle);
		
		//init ad
		initAd();
	}
	
	ActionBar actionBar;
	View actionTitle;
	TextView maintitle;
	TextView subtitle;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public void setTitle(String title){
		//separate text
		final String[] titles = title.split(";");
		
		if (titles.length==1){
			if (subtitle.getText().length() > 0){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					maintitle.setText(titles[0]);
					AnimatorSet animSet = new AnimatorSet();
					//convertDpToPixel means that we move texview by 5dp converted to pixels
		            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", 0f, convertDpToPixel(5f, actionTitle.getContext()));
		            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(subtitle, "alpha", 1f, 0f);
		            animSet.playTogether(transAnim, alphaAnim);
		            animSet.setDuration(250);
		            animSet.addListener(new  AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator arg0) {
							subtitle.setText("");
						}
		            });
		            animSet.start();
		        } else {
		        	maintitle.setText(titles[0]);
		        	subtitle.setText("");
		        	subtitle.setVisibility(View.GONE);
		        }
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					AnimatorSet animSet = new AnimatorSet();
					//convertDpToPixel means that we move texview by 5dp converted to pixels
		            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", convertDpToPixel(5f, actionTitle.getContext()), convertDpToPixel(2f, actionTitle.getContext()));
		            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(maintitle, "alpha", 1f, 0f);
		            animSet.playTogether(transAnim, alphaAnim);
		            animSet.setDuration(250);
		            animSet.addListener(new  AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator arg0) {
							maintitle.setText(titles[0]);
							AnimatorSet animSet = new AnimatorSet();
				            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", convertDpToPixel(7f, actionTitle.getContext()), convertDpToPixel(5f, actionTitle.getContext()));
				            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(maintitle, "alpha", 0f, 1f);
				            animSet.playTogether(transAnim, alphaAnim);
				            animSet.setDuration(250);
				            animSet.start();
						}
		            });
		            animSet.start();
		        } else {
		        	maintitle.setText(titles[0]);
		        	subtitle.setText("");
		        	subtitle.setVisibility(View.GONE);
		        }
			}
		} else {
			if (titles[0].equals(maintitle.getText().toString()) && titles[1].equals(subtitle.getText().toString())) return;
			
			if (!titles[0].equals(maintitle.getText().toString())){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					AnimatorSet animSet = new AnimatorSet();
					//convertDpToPixel means that we move texview by 5dp converted to pixels
		            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", 0f, -convertDpToPixel(2f, actionTitle.getContext()));
		            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(maintitle, "alpha", 1f, 0f);
		            animSet.playTogether(transAnim, alphaAnim);
		            animSet.setDuration(250);
		            animSet.addListener(new  AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator arg0) {
							maintitle.setText(titles[0]);
							AnimatorSet animSet = new AnimatorSet();
				            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", convertDpToPixel(2f, actionTitle.getContext()), 0f);
				            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(maintitle, "alpha", 0f, 1f);
				            animSet.playTogether(transAnim, alphaAnim);
				            animSet.setDuration(250);
				            animSet.start();
						}
		            });
		            animSet.start();
		        } else {
		        	maintitle.setText(titles[0]);
		        	maintitle.setVisibility(View.VISIBLE);
		        }
			} else {
				maintitle.setText(titles[0]);
			}
			
			if (subtitle.getText().length() == 0){
				subtitle.setText(titles[1]);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					AnimatorSet animSet = new AnimatorSet();
					//convertDpToPixel means that we move texview by 5dp converted to pixels
		            ObjectAnimator transAnim = ObjectAnimator.ofFloat(maintitle, "translationY", convertDpToPixel(5f, actionTitle.getContext()), 0f);
		            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
		            animSet.playTogether(transAnim, alphaAnim);
		            animSet.setDuration(250);
		            animSet.start();
		        } else {
		        	subtitle.setVisibility(View.VISIBLE);
		        }
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					AnimatorSet animSet = new AnimatorSet();
					//convertDpToPixel means that we move texview by 5dp converted to pixels
		            ObjectAnimator transAnim = ObjectAnimator.ofFloat(subtitle, "translationY", 0f, -convertDpToPixel(2f, actionTitle.getContext()));
		            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(subtitle, "alpha", 1f, 0f);
		            animSet.playTogether(transAnim, alphaAnim);
		            animSet.setDuration(250);
		            animSet.addListener(new  AnimatorListenerAdapter(){
						@Override
						public void onAnimationEnd(Animator arg0) {
							subtitle.setText(titles[1]);
							AnimatorSet animSet = new AnimatorSet();
				            ObjectAnimator transAnim = ObjectAnimator.ofFloat(subtitle, "translationY", convertDpToPixel(2f, actionTitle.getContext()), 0f);
				            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
				            animSet.playTogether(transAnim, alphaAnim);
				            animSet.setDuration(250);
				            animSet.start();
						}
		            });
		            animSet.start();
		        } else {
		        	subtitle.setText(titles[1]);
		        	subtitle.setVisibility(View.VISIBLE);
		        }
			}
		}
    }
	
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
    
	
	public void initAd(){
		//load banner ad
		Calendar birthday = Calendar.getInstance();
		birthday.setTimeInMillis(System.currentTimeMillis());
		birthday.set(1975, 04, 10);
		
		int gender = AdRequest.GENDER_FEMALE;
		
		Location loc = new Location("dummyprovider");
		loc.setLatitude(55.7522200);
		loc.setLongitude(37.6155600);
		
		AdRequest adRequest = new AdRequest.Builder()
		.setBirthday(new Date(birthday.getTimeInMillis()))
		.setGender(gender)
		.setLocation(loc)
		.build();
		
		adView = new AdView(this);
		adView.setAdSize(AdSize.LARGE_BANNER);
	    adView.setAdUnitId("ca-app-pub-6690318766939525/5352028493");
		adView.loadAd(adRequest);
	}
	
	public void setUpAd(LinearLayout layAd) {
	    // Locate the Banner Ad in activity xml
		if (adView.getParent() != null) {
			ViewGroup tempVg = (ViewGroup) adView.getParent();
			tempVg.removeView(adView);
		}
		
	    layAd.addView(adView);
	}
    
//    TODO Solve if this is really needed
//	@Override
//	public final boolean onMenuItemSelected(int featureId, android.view.MenuItem item) {
//	    // fix android formatted title bug
//	    if (item.getTitleCondensed() != null) {
//	        item.setTitleCondensed(item.getTitleCondensed().toString());
//	    }
//
//	    return super.onMenuItemSelected(featureId, item);
//	}
}
