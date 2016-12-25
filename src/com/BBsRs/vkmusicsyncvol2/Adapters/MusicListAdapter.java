package com.BBsRs.vkmusicsyncvol2.Adapters;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
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
	
	public MusicListAdapter(Context _context, ArrayList<MusicCollection> _musicCollection, DisplayImageOptions _options){
		if (_musicCollection != null)
			musicCollection = _musicCollection;
		musicCollectionNonFiltered.addAll(musicCollection);
		context = _context;
		options = _options;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
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
    }
    
	private void setViewHolder(View rowView) {
		ViewHolder holder = new ViewHolder();
		holder.length = (TextView) rowView.findViewById(R.id.length);
		holder.title = (TextView) rowView.findViewById(R.id.title);
		holder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
		holder.albumArt = (ImageView)rowView.findViewById(R.id.albumArt);
		holder.needInflate = false;
		rowView.setTag(holder);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
