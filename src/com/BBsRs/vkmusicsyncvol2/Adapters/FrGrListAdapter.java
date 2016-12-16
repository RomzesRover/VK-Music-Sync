package com.BBsRs.vkmusicsyncvol2.Adapters;

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
import com.BBsRs.vkmusicsyncvol2.collections.FrGrCollection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class FrGrListAdapter extends BaseAdapter {
	
	LayoutInflater inflater;

	Context context;
	ArrayList<FrGrCollection> frGrCollection = new ArrayList<FrGrCollection>();
	//with this options we will load images
    DisplayImageOptions options ;
	
	public FrGrListAdapter(Context _context, ArrayList<FrGrCollection> _frGrCollection, DisplayImageOptions _options){
		if (_frGrCollection != null)
			frGrCollection = _frGrCollection;
		context = _context;
		options = _options;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	
	public void UpdateList(ArrayList<FrGrCollection> _frGrCollection){
		frGrCollection = _frGrCollection;
	}

	@Override
	public int getCount() {
		return frGrCollection.size();
	}

	@Override
	public FrGrCollection getItem(int position) {
		return frGrCollection.get(position);
	}
	
	public ArrayList<FrGrCollection> getFrGrCollection() {
		return frGrCollection;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    static class ViewHolder {
    	public boolean needInflate;
        public TextView title;
        public ImageView albumArt;
        public ImageView onlineStatus;
    }
    
	private void setViewHolder(View rowView) {
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView) rowView.findViewById(R.id.title);
		holder.albumArt = (ImageView)rowView.findViewById(R.id.cover_art);
		holder.onlineStatus = (ImageView)rowView.findViewById(R.id.online_status);
		holder.needInflate = false;
		rowView.setTag(holder);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
        final View rowView;
		if (convertView==null) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_frgr, parent, false);
			setViewHolder(rowView);
		}
		else if (((ViewHolder)convertView.getTag()).needInflate) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_frgr, parent, false);
			setViewHolder(rowView);
		}
		else {
			rowView = convertView;
		}
        
		holder = (ViewHolder)rowView.getTag();
		
		//set fonts
		SFUIFonts.MEDIUM.apply(context, holder.title);
		
		//view job
		holder.title.setText(frGrCollection.get(position).friendGroupName);
		
		if (frGrCollection.get(position).online != -1)
			holder.onlineStatus.setImageResource(frGrCollection.get(position).online == 1 ? R.drawable.ic_online : R.drawable.ic_offline);
		else 
			holder.onlineStatus.setVisibility(View.GONE);
		
		try {
			ImageLoader.getInstance().displayImage(frGrCollection.get(position).iconUrl, holder.albumArt, options, 0, animateFirstListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rowView;
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
