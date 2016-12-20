/*Class to desc all data about titles on simple news page!
  Include: year, img path, author's nickyear, number of titles, current rating by user's, and url link to full news page
  Also here supported writeToParcel to save this data after rotate screen on all of the devices!
  Author Roman Gaitbaev writed for AstroNews.ru 
  http://vk.com/romzesrover 
  Created: 18.08.2013 00:58*/

/*Modified to lenfilm at 22 06 2014 */

package com.BBsRs.vkmusicsyncvol2.collections;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumCollection implements Parcelable, Serializable {
  
    /**
	 * 
	 */
	private static final long serialVersionUID = -4356107655512129128L;
	public String title;
	public long album_id;
	public long owner_id;
	

  public AlbumCollection(long _album_id, long _owner_id, String _title) {
	    album_id = _album_id;
	    owner_id = _owner_id;
	    title = _title;
  }
  


@Override
public int describeContents() {
	return 0;
}

private AlbumCollection(Parcel in) {
	album_id = in.readLong();
    owner_id = in.readLong();
    title = in.readString();
}

@Override
public void writeToParcel(Parcel out, int flags) {
	 out.writeLong(album_id);
     out.writeLong(owner_id);
     out.writeString(title);
}

public static final Parcelable.Creator<AlbumCollection> CREATOR = new Parcelable.Creator<AlbumCollection>() {
    public AlbumCollection  createFromParcel(Parcel in) {
        return new AlbumCollection (in);
    }

    public AlbumCollection [] newArray(int size) {
        return new AlbumCollection [size];
    }
};
}