package com.BBsRs.vkmusicsyncvol2.BaseApplication;

public class Constants {

	public static final String CLIENT_SECRET = "hHbZxrka2uZ6jB1inYsH";
	public static final String CLIENT_ID = "2274003";
	
	public static final String GOOGLE_IMAGE_REQUEST_URL = "https://www.google.ru/search?&safe=off&tbm=isch&tbs=isz:m,iar:s&q=";
	
	public static final String PREFERENCES_USER_AVATAR_URL = "preferences:user_avatar_url";
	public static final String PREFERENCES_USER_FIRST_NAME = "preferences:user_first_name";
	public static final String PREFERENCES_USER_LAST_NAME = "preferences:user_last_name";
	
	public static final String PREFERENCES_UPDATE_OWNER_LIST = "preferences:update_owner_list";
	public static final String PREFERENCES_UPDATE_SEARCH_LIST = "preferences:update_search_list";
	public static final String PREFERENCES_UPDATE_POPULAR_LIST = "preferences:update_popular_list";
	public static final String PREFERENCES_UPDATE_RECC_LIST = "preferences:update_recc_list";
	public static final String PREFERENCES_UPDATE_DOWNLOADED_LIST = "preferences:update_downloaded_list";
	
	public static final String PREFERENCES_DOWNLOAD_DIRECTORY = "preferences:download_directory";
	
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
	public static final String INTENT_DOWNLOAD_SONG_TO_STORAGE = "intent:download_song_to_storage";
	public static final String INTENT_DELETE_SONG_FROM_STORAGE = "intent:delete_song_from_storage";
	/*--*/
	public static final String INTENT_EXTRA_ONE_AUDIO = "intent_extra:one_audio";
	public static final String INTENT_EXTRA_ONE_AUDIO_POSITION_IN_LIST = "intent_extra:one_audio_position_in_list";
	/*--*/
	public static final String INTENT_ADD_SONG_TO_DOWNLOAD_QUEUE = "intent:add_song_to_download_queue";
	public static final String INTENT_REMOVE_SONG_FROM_DOWNLOAD_QUEUE = "intent:remove_song_from_download_queue";
	public static final String INTENT_CHANGE_SONG_DOWNLOAD_PERCENTAGE = "intent:change_song_download_percentage";
	/*--*/
	public static final String INTENT_STOP_DOWNLOAD = "intent:stop_download";
	public static final String INTENT_REQUEST_DOWNLOAD_STATUS = "intent:request_download_status";
	/*--*/
	public static final String INTENT_FORCE_HIDE_UPDATE_LINE = "intent:force_hide_update_line";
	public static final String INTENT_FORCE_SHOW_UPDATE_LINE = "intent:force_show_update_line";
	public static final String INTENT_FORCE_CLOSE_SEARCH_KEYBOARD = "intent:force_close_search_keyboard";
	/*--PLAYER VALUES--*/
	public static final String FRAGMENT_PLAYER_TAG = "fragment:player_tag";
	public static final String INTENT_UPDATE_PLAYBACK = "intent:update_playback";
	public static final String INTENT_UPDATE_PLAYBACK_CURRENT = "intent:update_playback_current";
	public static final String INTENT_UPDATE_PLAYBACK_CURRENT_BUFFERING = "intent:update_playback_current_buffering";
	public static final String INTENT_UPDATE_PLAYBACK_LENGTH = "intent:update_playback_length";
	/*--*/
	public static final String PREFERENCES_PLAYER_LIST_COLLECTIONS = "preferences:player_list_collections_";
	public static final String BUNDLE_PLAYER_LIST_CURR_HLENGTH = "bundle:player_list_curr_hlength";
	public static final String BUNDLE_PLAYER_CURRENT_SELECTED_POSITION = "bundle:player_current_selected_position";
	public static final String BUNDLE_PLAYER_LIST_SIZE = "bundle:player_list_size";
	/*--*/
	public static final String INTENT_PLAYER_RESTART = "intent:player_restart";
	public static final String INTENT_PLAYER_NEXT = "intent:player_next";
	public static final String INTENT_PLAYER_PREV = "intent:player_prev";
	public static final String INTENT_PLAYER_PLAY_PAUSE = "intent:player_play_pause";
	public static final String INTENT_PLAYER_REPEAT = "intent:player_repeat";
	public static final String INTENT_PLAYER_SHUFFLE = "intent:player_shuffle";
	public static final String INTENT_PLAYER_OPEN_ACTIVITY = "intent:player_open_activity";
	public static final String INTENT_PLAYER_OPEN_ACTIVITY_PLAYER_FRAGMENT = "intent:player_open_player_fragment";
	public static final String INTENT_PLAYER_OPEN_ACTIVITY_LAST_FRAGMENT = "intent:player_open_activity_last_fragment";
	public static final String INTENT_PLAYER_KILL_SERVICE_ON_PAUSE = "intent:player_kill_service_on_pause";
	/*--*/
	public static final String INTENT_PLAYER_PLAY_PAUSE_STRICT_MODE = "intent:player_play_pause_strict_mode";
	public static final int INTENT_PLAYER_PLAY_PAUSE_STRICT_ANY = 1;
	public static final int INTENT_PLAYER_PLAY_PAUSE_STRICT_PAUSE_ONLY = 3;
	public static final int INTENT_PLAYER_PLAY_PAUSE_STRICT_PLAY_ONLY = 5;
	/*--*/
	public static final String INTENT_PLAYER_SEEK_CHANGE = "intent:player_seek_change";
	public static final String INTENT_PLAYER_SEEK_TO = "intent:player_seek_to";
	/*--*/
	public static final String INTENT_PLAYER_BACK_SWITCH_TRACK_INFO = "intent:player_back_switch_track_info";
	public static final String INTENT_PLAYER_BACK_SWITCH_DIRECTION = "intent:player_back_switch_direction";
	public static final String INTENT_PLAYER_BACK_SWITCH_FITS = "intent:player_back_switch_fits";
	public static final String INTENT_PLAYER_BACK_SWITCH_POSITION = "intent:player_back_switch_position";
	public static final String INTENT_PLAYER_BACK_SWITCH_SIZE = "intent:player_back_switch_size";
	public static final String INTENT_PLAYER_LIST_TITLE_NAME = "intent:player_list_title_name";
	public static final String INTENT_PLAYER_BACK_SWITCH_ONE_AUDIO = "intent:player_back_switch_one_audio";
	/*---*/
	public static final String INTENT_PLAYER_REQUEST_BACK_SWITCH_INFO = "intent:player_request_back_switch_info";
	/*--*/
	public static final String INTENT_PLAYER_PLAYBACK_PLAY_PAUSE = "intent:player_playback_play_pause";
	public static final String INTENT_PLAYER_PLAYBACK_PLAY_PAUSE_STATUS = "intent:player_playback_play_pause_status";
	/*--*/
	public static final String INTENT_PLAYER_PLAYBACK_CHANGE_REPEAT = "intent:player_playback_change_repeat";
	public static final String INTENT_PLAYER_PLAYBACK_REPEAT_STATUS = "intent:player_playback_repeat_status";
	/*--*/
	public static final String INTENT_PLAYER_PLAYBACK_CHANGE_SHUFFLE = "intent:player_playback_change_shuffle";
	public static final String INTENT_PLAYER_PLAYBACK_SHUFFLE_STATUS = "intent:player_playback_shuffle_status";
	/*--*/
	public static final int LIST_ACTION_ADD = 0;
	public static final int LIST_ACTION_ADDED = 1;
	public static final int LIST_ACTION_REMOVE = 2;
	public static final int LIST_ACTION_RESTORE = 3;

	public static final int LIST_ACTION_DOWNLOAD = -1;
	public static final int LIST_ACTION_DOWNLOAD_STARTED = 0;
	public static final int LIST_ACTION_DOWNLOADED = 100;
	public static final int LIST_ACTION_DELETE = 101;
	
	public static final int LIST_APAR_NaN = -1;
	public static final int LIST_APAR_IN_PROCESS = 0;
	
	public static final int NOTIFICATION_DOWNLOAD = 13;
	public static final int NOTIFICATION_MESSAGE = 14;
	public static final int NOTIFICATION_PLAYER = 12;
	
	
}
