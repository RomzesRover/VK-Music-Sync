package com.BBsRs.vkmusicsyncvol2.Adapters;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.R;
import com.BBsRs.vkmusicsyncvol2.collections.AlbumCollection;

public class AlbumListAdapter extends BaseAdapter {
	
	LayoutInflater inflater;

	Context context;
	ArrayList<AlbumCollection> albumCollection = new ArrayList<AlbumCollection>();
	
	public AlbumListAdapter(Context _context, ArrayList<AlbumCollection> _albumCollection){
		if (_albumCollection != null)
			albumCollection = _albumCollection;
		context = _context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void UpdateList(ArrayList<AlbumCollection> _albumCollection){
		albumCollection = _albumCollection;
	}

	@Override
	public int getCount() {
		return albumCollection.size();
	}

	@Override
	public AlbumCollection getItem(int position) {
		return albumCollection.get(position);
	}
	
	public ArrayList<AlbumCollection> getAlbumCollection() {
		return albumCollection;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    static class ViewHolder {
    	public boolean needInflate;
        public TextView title;
    }
    
	private void setViewHolder(View rowView) {
		ViewHolder holder = new ViewHolder();
		holder.title = (TextView) rowView.findViewById(R.id.title);
		holder.needInflate = false;
		rowView.setTag(holder);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
        final View rowView;
		if (convertView==null) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_album, parent, false);
			setViewHolder(rowView);
		}
		else if (((ViewHolder)convertView.getTag()).needInflate) {
			rowView = inflater.inflate(R.layout.adapter_simple_element_album, parent, false);
			setViewHolder(rowView);
		}
		else {
			rowView = convertView;
		}
        
		holder = (ViewHolder)rowView.getTag();
		
		//set fonts
		SFUIFonts.MEDIUM.apply(context, holder.title);
		
		//view job
		holder.title.setText(albumCollection.get(position).title);
		
		return rowView;
	}
}
