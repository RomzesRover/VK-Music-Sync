/*Class to desc all data about iconUrls on simple news page!
  Include: year, img path, author's nickyear, number of iconUrls, current rating by user's, and url link to full news page
  Also here supported writeToParcel to save this data after rotate screen on all of the devices!
  Author Roman Gaitbaev writed for AstroNews.ru 
  http://vk.com/romzesrover 
  Created: 18.08.2013 00:58*/

/*Modified to lenfilm at 22 06 2014 */

package com.BBsRs.vkmusicsyncvol2.collections;

import android.os.Parcel;
import android.os.Parcelable;

public class FrGrCollection implements Parcelable {
  
    public long fgid;
    public String friendGroupName;
    public String iconUrl;
    public int online;
  

 public FrGrCollection(long _fgid, String _friendGroupName, String _iconUrl, int _online) {
	fgid = _fgid;
	friendGroupName = _friendGroupName;
	iconUrl = _iconUrl;
	online = _online;
  }


@Override
public int describeContents() {
	return 0;
}

private FrGrCollection(Parcel in) {
	fgid = in.readLong();
    friendGroupName = in.readString();
    iconUrl = in.readString();
    online = in.readInt();
}

@Override
public void writeToParcel(Parcel out, int flags) {
	out.writeLong(fgid);
	out.writeString(friendGroupName);
	out.writeString(iconUrl);
	out.writeInt(online);
}

public static final Parcelable.Creator<FrGrCollection> CREATOR = new Parcelable.Creator<FrGrCollection>() {
    public FrGrCollection  createFromParcel(Parcel in) {
        return new FrGrCollection (in);
    }

    public FrGrCollection [] newArray(int size) {
        return new FrGrCollection [size];
    }
};
}