package com.BBsRs.vkmusicsyncvol2.Adapters;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class MusicListAdapter extends BaseAdapter {
	
	LayoutInflater inflater;

	Context context;
	ArrayList<MusicCollection> musicCollection = new ArrayList<MusicCollection>();
	//with this options we will load images
    DisplayImageOptions options ;
	
	public MusicListAdapter(Context _context, ArrayList<MusicCollection> _musicCollection, DisplayImageOptions _options){
		musicCollection = _musicCollection;
		context = _context;
		options = _options;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public int getCount() {
		return musicCollection.size()-1;
	}

	@Override
	public MusicCollection getItem(int position) {
		return musicCollection.get(position);
	}
	
	public ArrayList<MusicCollection> getMusicCollection() {
		return musicCollection;
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

}
