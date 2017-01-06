package com.BBsRs.vkmusicsyncvol2.BaseApplication;

public class Constants {

	public static final String CLIENT_SECRET = "hHbZxrka2uZ6jB1inYsH";
	public static final String CLIENT_ID = "2274003";
	
	public static final String GOOGLE_IMAGE_REQUEST_URL = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m,iar:s&q=";
	
	public static final String PREFERENCES_USER_AVATAR_URL = "preferences:user_avatar_url";
	public static final String PREFERENCES_USER_FIRST_NAME = "preferences:user_first_name";
	public static final String PREFERENCES_USER_LAST_NAME = "preferences:user_last_name";
	
	public static final String PREFERENCES_UPDATE_OWNER_LIST = "preferences:update_owner_list";
	
	public static final String EXTRA_LIST_COLLECTIONS = "extra:list_collections";
	public static final String EXTRA_LIST_SECOND_COLLECTIONS = "extra:list_second_collections_2";
	
	public static final String BUNDLE_MUSIC_LIST_TYPE = "bundle:music_list_type";
	public static final int BUNDLE_MUSIC_LIST_OF_PAGE = 0;
	public static final int BUNDLE_MUSIC_LIST_POPULAR = 1;
	public static final int BUNDLE_MUSIC_LIST_RECOMMENDATIONS = 2;
	public static final int BUNDLE_MUSIC_LIST_SEARCH = 3;
	public static final int BUNDLE_MUSIC_LIST_DOWNLOADED = 4;
	public static final int BUNDLE_MUSIC_LIST_WALL = 7;
	public static final int BUNDLE_MUSIC_LIST_ALBUM = 8;
	
	public static final String BUNDLE_MUSIC_LIST_SEARCH_REQUEST = "bundle:music_list_search_request";
	
	public static final String BUNDLE_LIST_USRFRGR_ID = "bundle:music_list_usrfrgr_id";
	public static final String BUNDLE_LIST_TITLE_NAME = "bundle:music_list_title_name";
	public static final String BUNDLE_LIST_ALBUM_ID = "bundle:music_list_album_id";
	
	public static final String BUNDLE_FRGR_LIST_TYPE = "bundle:frgr_list_type";
	public static final int BUNDLE_FRGR_LIST_FRIENDS = 0;
	public static final int BUNDLE_FRGR_LIST_GROUPS = 1;
	
	public static final String BUNDLE_LIST_ERROR_CODE = "list:error_code";
	public static final int BUNDLE_LIST_ERROR_CODE_NO_ERROR = -1;
	public static final int BUNDLE_LIST_ERROR_CODE_ACCESS_TO_USER_AUDIO_DENIED = 0;
	public static final int BUNDLE_LIST_ERROR_CODE_GROUP_AUDIO_DISABLED = 1;
	public static final int BUNDLE_LIST_ERROR_CODE_EMPTY_LIST = 2;
	public static final int BUNDLE_LIST_ERROR_CODE_NO_SEARCH_REQUEST = 4;
	public static final int BUNDLE_LIST_ERROR_CODE_PAGE_DEACTIVATED = 5;
	public static final int BUNDLE_LIST_ERROR_CODE_ANOTHER = 3;
	
	public static final String INTENT_ADD_SONG_TO_OWNER_LIST = "intent:add_song_to_owner_list";
	public static final String INTENT_REMOVE_SONG_FROM_OWNER_LIST = "intent:remove_song_from_owner_list";
	public static final String INTENT_EXTRA_ONE_AUDIO = "intent_extra:one_audio";
	public static final String INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST = "intent_extra:one_audio_position_in_list";
	
	public static final int LIST_ADD = 0;
	public static final int LIST_ADDED = 1;
	public static final int LIST_REMOVE = 2;
	public static final int LIST_RESTORE = 3;
	
}
