package com.BBsRs.vkmusicsyncvol2.Adapters;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.ListView;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.collections.MusicCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MusicListAdapter extends BaseAdapter implements Filterable{
	
	LayoutInflater inflater;

	Context context;
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	ArrayList<MusicCollection> musicCollectionNonFiltered = new ArrayList<MusicCollection>();
	//with this options we will load images
    DisplayImageOptions options ;
    
    ListView list;
	
	public MusicListAdapter(Context _context, ArrayList<MusicCollection> _musicCollection, DisplayImageOptions _options){
		if (_musicCollection != null)
			musicCollection = _musicCollection;
		musicCollectionNonFiltered.addAll(musicCollection);
		context = _context;
		options = _options;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	
	public void bindListView(ListView _list){
		list = _list;
	}
	
	public void UpdateList(ArrayList<MusicCollection> _musicCollection){
		if (_musicCollection != null)
			musicCollection = _musicCollection;
		musicCollectionNonFiltered = new ArrayList<MusicCollection>();
		musicCollectionNonFiltered.addAll(musicCollection);
	}

	@Override
	public int getCount() {
		return musicCollection.size();
	}
	
	public ArrayList<MusicCollection> getMusicCollection() {
		return musicCollection;
	}

	@Override
	public MusicCollection getItem(int position) {
		return musicCollection.get(position);
	}
	
	public int getCountNonFiltered() {
		return musicCollectionNonFiltered.size();
	}

	public ArrayList<MusicCollection> getMusicCollectionNonFiltered() {
		return musicCollectionNonFiltered;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    static class ViewHolder {
    	public boolean needInflate;
        public TextView length;
        public TextView title;
        public TextView subtitle;
        public ImageView albumArt;
        public ImageView albumArtMask;
        public ImageView isInOwnerList;
    }
    
	private void setViewHolder(View rowView) {
		ViewHolder holder = new ViewHolder();
		holder.length = (TextView) rowView.findViewById(R.id.length);
		holder.title = (TextView) rowView.findViewById(R.id.title);
		holder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
		holder.albumArt = (ImageView)rowView.findViewById(R.id.albumArt);
		holder.albumArtMask = (ImageView)rowView.findViewById(R.id.albumArtMask);
		holder.isInOwnerList = (ImageView)rowView.findViewById(R.id.isInOwnerListAction);
		holder.needInflate = false;
		rowView.setTag(holder);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	public void updateIsInOwnerListState(final int position){
		View v;
		try {
			//+1 cuz we have header in list view
			v = list.getChildAt(position + 1 - list.getFirstVisiblePosition());
		} catch(Exception e) {
			v = null;
		}

	    if(v == null)
	       return;
	    
	    if (position >= musicCollection.size())
	    	return;
	    
	    setViewHolder(v);
	    final ViewHolder holder = (ViewHolder)v.getTag();
	    
	    Animation flyDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fly_up_anim);
	    holder.isInOwnerList.startAnimation(flyDownAnimation);
    	flyDownAnimation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				holder.isInOwnerList.setVisibility(View.INVISIBLE);
				
				switch (musicCollection.get(position).isInOwnerList){
				case Constants.LIST_ADD:
					holder.isInOwnerList.setImageResource(R.drawable.ic_add_normal);
					break;
				case Constants.LIST_RESTORE:
					holder.isInOwnerList.setImageResource(R.drawable.ic_add_normal);
					break;
				case Constants.LIST_ADDED:
					holder.isInOwnerList.setImageResource(R.drawable.ic_added_normal);
					break;
				case Constants.LIST_REMOVE:
					holder.isInOwnerList.setImageResource(R.drawable.ic_remove_normal);
					break;
				}
				
				holder.isInOwnerList.setVisibility(View.VISIBLE);
				
				Animation flyDownAnimation = AnimationUtils.loadAnimation(context, R.anim.fly_down_anim);
			    holder.isInOwnerList.startAnimation(flyDownAnimation);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			@Override
			public void onAnimationStart(Animation arg0) { }
    	});
    	
		switch (musicCollection.get(position).isInOwnerList){
		case Constants.LIST_RESTORE:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				ObjectAnimator colorAnim = ObjectAnimator.ofInt(holder.title, "textColor", context.getResources().getColor(R.color.black_color), context.getResources().getColor(R.color.gray_four_color));
			    colorAnim.setEvaluator(new ArgbEvaluator());
			    colorAnim.setDuration(250);
			    
			    ObjectAnimator colorAnim2 = ObjectAnimator.ofInt(holder.subtitle, "textColor", context.getResources().getColor(R.color.gray_two_color), context.getResources().getColor(R.color.gray_four_color));
			    colorAnim2.setEvaluator(new ArgbEvaluator());
			    colorAnim2.setDuration(250);
			    
			    ObjectAnimator colorAnim3 = ObjectAnimator.ofInt(holder.length, "textColor", context.getResources().getColor(R.color.black_color), context.getResources().getColor(R.color.gray_four_color));
			    colorAnim3.setEvaluator(new ArgbEvaluator());
			    colorAnim3.setDuration(250);
			    
			    if (holder.albumArtMask.getVisibility() == View.INVISIBLE){
			    	holder.albumArtMask.setVisibility(View.VISIBLE);
			    	FadeInBitmapDisplayer.animate(holder.albumArtMask, 250);
			    }
			    
			    colorAnim3.start();
			    colorAnim2.start();
			    colorAnim.start();
			    
			} else {
				holder.albumArtMask.setVisibility(View.VISIBLE);
				holder.title.setTextColor(context.getResources().getColor(R.color.gray_four_color));
				holder.subtitle.setTextColor(context.getResources().getColor(R.color.gray_four_color));
				holder.length.setTextColor(context.getResources().getColor(R.color.gray_four_color));
			}
			break;
		case Constants.LIST_REMOVE:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				ObjectAnimator colorAnim = ObjectAnimator.ofInt(holder.title, "textColor", context.getResources().getColor(R.color.gray_four_color), context.getResources().getColor(R.color.black_color));
			    colorAnim.setEvaluator(new ArgbEvaluator());
			    colorAnim.setDuration(250);
			    
			    ObjectAnimator colorAnim2 = ObjectAnimator.ofInt(holder.subtitle, "textColor", context.getResources().getColor(R.color.gray_four_color), context.getResources().getColor(R.color.gray_two_color));
			    colorAnim2.setEvaluator(new ArgbEvaluator());
			    colorAnim2.setDuration(250);
			    
			    ObjectAnimator colorAnim3 = ObjectAnimator.ofInt(holder.length, "textColor", context.getResources().getColor(R.color.gray_four_color), context.getResources().getColor(R.color.black_color));
			    colorAnim3.setEvaluator(new ArgbEvaluator());
			    colorAnim3.setDuration(250);
			    
			    if (holder.albumArtMask.getVisibility() == View.VISIBLE){
			    	AlphaAnimation fadeImage = new AlphaAnimation(1, 0);
					fadeImage.setDuration(250);
					fadeImage.setInterpolator(new DecelerateInterpolator());
					
					fadeImage.setAnimationListener(new AnimationListener(){
						@Override
						public void onAnimationEnd(Animation arg0) {
							holder.albumArtMask.setVisibility(View.INVISIBLE);
						}
						@Override
						public void onAnimationRepeat(Animation arg0) { }
						@Override
						public void onAnimationStart(Animation arg0) { }
					});
					
					holder.albumArtMask.startAnimation(fadeImage);
			    }
			    
			    colorAnim3.start();
			    colorAnim2.start();
			    colorAnim.start();
			    
			} else {
				holder.albumArtMask.setVisibility(View.INVISIBLE);
				holder.title.setTextColor(context.getResources().getColor(R.color.black_color));
				holder.subtitle.setTextColor(context.getResources().getColor(R.color.gray_two_color));
				holder.length.setTextColor(context.getResources().getColor(R.color.black_color));
			}
			break;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
        final View rowView;
		if (convertView==null) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_music, parent, false);
			setViewHolder(rowView);
		}
		else if (((ViewHolder)convertView.getTag()).needInflate) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_music, parent, false);
			setViewHolder(rowView);
		}
		else {
			rowView = convertView;
		}
        
		holder = (ViewHolder)rowView.getTag();
		
		//set fonts
		SFUIFonts.LIGHT.apply(context, holder.subtitle);
		SFUIFonts.MEDIUM.apply(context, holder.title);
		SFUIFonts.MEDIUM.apply(context, holder.length);
		
		//view job
		holder.title.setText(musicCollection.get(position).artist);
		holder.subtitle.setText(musicCollection.get(position).title);
		holder.length.setText(musicCollection.get(position).duration == -1 || musicCollection.get(position).duration == 0 ? "" : stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)/60))+":"+stringPlusZero(String.valueOf((int)(musicCollection.get(position).duration)%60)));
		
		
		try {
			ImageLoader.getInstance().displayImage(Constants.GOOGLE_IMAGE_REQUEST_URL + URLEncoder.encode(musicCollection.get(position).artist+ " - "+musicCollection.get(position).title, "UTF-8"), holder.albumArt, options, 1, animateFirstListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		switch (musicCollection.get(position).isInOwnerList){
		case Constants.LIST_ADD:
			holder.isInOwnerList.setImageResource(R.drawable.ic_add_normal);
			break;
		case Constants.LIST_ADDED:
			holder.isInOwnerList.setImageResource(R.drawable.ic_added_normal);
			break;
		case Constants.LIST_REMOVE:
			holder.isInOwnerList.setImageResource(R.drawable.ic_remove_normal);
			holder.title.setTextColor(context.getResources().getColor(R.color.black_color));
			holder.subtitle.setTextColor(context.getResources().getColor(R.color.gray_two_color));
			holder.length.setTextColor(context.getResources().getColor(R.color.black_color));
			holder.albumArtMask.setVisibility(View.INVISIBLE);
			break;
		case Constants.LIST_RESTORE:
			holder.title.setTextColor(context.getResources().getColor(R.color.gray_four_color));
			holder.subtitle.setTextColor(context.getResources().getColor(R.color.gray_four_color));
			holder.length.setTextColor(context.getResources().getColor(R.color.gray_four_color));
			holder.albumArtMask.setVisibility(View.VISIBLE);
			break;
		}
		
		holder.isInOwnerList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (musicCollection.get(position).isInOwnerList == Constants.LIST_ADD || musicCollection.get(position).isInOwnerList == Constants.LIST_RESTORE){
					Intent sendAddSongRequest = new Intent(Constants.INTENT_ADD_SONG_TO_OWNER_LIST);
					sendAddSongRequest.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicCollection.get(position));
					sendAddSongRequest.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, position);
					context.sendBroadcast(sendAddSongRequest);
				}
				if (musicCollection.get(position).isInOwnerList == Constants.LIST_REMOVE){
					Intent sendRemoveSongRequest = new Intent(Constants.INTENT_REMOVE_SONG_FROM_OWNER_LIST);
					sendRemoveSongRequest.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO, (Parcelable)musicCollection.get(position));
					sendRemoveSongRequest.putExtra(Constants.INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST, position);
					context.sendBroadcast(sendRemoveSongRequest);
				}
			}
		});
		
		return rowView;
	}
	
	//this func adds zeros to string
	public String stringPlusZero(String arg1) {
		if (arg1.length() == 1)
			return "0" + arg1;
		else
			return arg1;
	}
	
	//animation for universal image loader
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 300);
					displayedImages.add(imageUri);
				}
			}
		}
	}

    @SuppressLint("DefaultLocale") 
    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            	musicCollection = (ArrayList<MusicCollection>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<MusicCollection> FilteredArray = new ArrayList<MusicCollection>();

                if (musicCollection == null)    {
                	musicCollection = musicCollectionNonFiltered;
                }
                if (constraint == null || constraint.length() == 0) {
                    results.count = musicCollectionNonFiltered.size();
                    results.values = musicCollectionNonFiltered;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < musicCollectionNonFiltered.size(); i++) {
                    	String[] arrayOfKeyWordsArtist = musicCollectionNonFiltered.get(i).artist.toLowerCase().split(" ");
                    	String[] arrayOfKeyWordsTitle = musicCollectionNonFiltered.get(i).title.toLowerCase().split(" ");
                    	String[] arrayOfRequestWords = constraint.toString().split(" ");
                    	
                    	boolean isCurrentAlreadyAdded = false;
                    	int index = 0;
                    	int index2 = 0;
                		for (String oneWordArtist : arrayOfKeyWordsArtist){
                			oneWordArtist="";
                			index2 = 0;
                			for (String requestWords : arrayOfRequestWords){
                				if (index+index2<arrayOfKeyWordsArtist.length)
                					oneWordArtist+=arrayOfKeyWordsArtist[index+index2];
                				index2++;
                			}
                    		if (oneWordArtist.startsWith(constraint.toString().replace(" ", ""))){
                    			FilteredArray.add(musicCollectionNonFiltered.get(i));
                    			isCurrentAlreadyAdded = true;
                    			break;
                    		}
                    		index++;
                    	}
                    	
                    	
                    	
                    	if (!isCurrentAlreadyAdded){
                    		index = 0;
                    		for (String oneWordTitle : arrayOfKeyWordsTitle){
                    			oneWordTitle="";
                    			index2 = 0;
                    			for (String requestWords : arrayOfRequestWords){
                    				if (index+index2<arrayOfKeyWordsTitle.length)
                    					oneWordTitle+=arrayOfKeyWordsTitle[index+index2];
                    				index2++;
                    			}
                        		if (oneWordTitle.startsWith(constraint.toString().replace(" ", ""))){
                        			FilteredArray.add(musicCollectionNonFiltered.get(i));
                        			isCurrentAlreadyAdded = true;
                        			break;
                        		}
                        		index++;
                        	}
                    	}
                    	
                    	if (!isCurrentAlreadyAdded)
                    		if (musicCollectionNonFiltered.get(i).artist.toLowerCase().startsWith(constraint.toString()) || musicCollectionNonFiltered.get(i).title.toLowerCase().startsWith(constraint.toString()))  {
                        			FilteredArray.add(musicCollectionNonFiltered.get(i));
                        			isCurrentAlreadyAdded = true;
                    		}
                    }

                    results.count = FilteredArray.size();
                    results.values = FilteredArray;
                }
                
                if (results.count==0){
                	//TODO SEND RECEIVER NO RESULT
                } 
                
                return results;
            }
        };

        return filter;
    }

}
