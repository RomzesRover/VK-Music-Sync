package com.BBsRs.vkmusicsyncvol2;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.Dialog;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.preference.PreferenceManager;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Button;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.TextView;
import org.holoeverywhere.widget.Toast;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

import com.BBsRs.SFUIFontsEverywhere.SFUIFonts;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Account;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.BaseActivity;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.Constants;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.CustomEnvironment;
import com.BBsRs.vkmusicsyncvol2.BaseApplication.MySSLSocketFactory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.perm.kate.api.Api;
import com.perm.kate.api.Auth;
import com.perm.kate.api.User;
import com.perm.utils.Utils;

public class LoginActivity extends BaseActivity {
	
	Dialog alert = null;
    ProgressDialog progressDialog = null;
    private final Handler handler = new Handler();
    EditText username, password;
    
    //with this options we will load images
    DisplayImageOptions options ;
    
	Account account = new Account();
	Api api;
	
	SharedPreferences sPref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    super.setContentView(R.layout.activity_login);
	    super.setTitle(getString(R.string.login_activity_authorization));
	    super.getSupportActionBar().setIcon(R.drawable.logo_vk);
	    
	    progressDialog = new ProgressDialog(this);
	    
	    //set up preferences
	    sPref = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    //init views
	    final TextView textMaskName = (TextView)super.findViewById(R.id.textMaskName);
	    final TextView textMaskPass = (TextView)super.findViewById(R.id.textMaskPass);
	    username = (EditText)super.findViewById(R.id.username);
	    password = (EditText)super.findViewById(R.id.password);
	    Button login = (Button)super.findViewById(R.id.login);
	    
        //init image loader
        options = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .showImageOnLoading(R.drawable.nopic)
        .cacheInMemory(true)					
        .build();
	    
	    //set fonts
	    SFUIFonts.ULTRALIGHT.apply(this, textMaskName);
	    SFUIFonts.ULTRALIGHT.apply(this, textMaskPass);
	    SFUIFonts.LIGHT.apply(this, username);
	    SFUIFonts.LIGHT.apply(this, password);
	    SFUIFonts.ULTRALIGHT.apply(this, login);
	    
	    //hide, show hint text on edit
	    username.addTextChangedListener(new TextWatcher() {
	    	   public void afterTextChanged(Editable s) {
	    		   if (s.length()<=0){
	    			   textMaskName.setVisibility(View.VISIBLE);
	    		   } else {
	    		   }
	    	   }
	    	   public void beforeTextChanged(CharSequence s, int start, 
	    	     int count, int after) {
	    	   }
	    	   public void onTextChanged(CharSequence s, int start, 
	    	     int before, int count) {
	    		   textMaskName.setVisibility(View.GONE);
	    	   }
	    });
	    password.addTextChangedListener(new TextWatcher() {
	    	   public void afterTextChanged(Editable s) {
	    		   if (s.length()<=0){
	    			   textMaskPass.setVisibility(View.VISIBLE);
	    		   } else {
	    		   }
	    	   }
	    	   public void beforeTextChanged(CharSequence s, int start, 
	    	     int count, int after) {
	    	   }
	    	   public void onTextChanged(CharSequence s, int start, 
	    	     int before, int count) {
	    		   textMaskPass.setVisibility(View.GONE);
	    	   }
	    });
	    
	    password.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
    	        if (actionId == EditorInfo.IME_ACTION_GO) {
    	        	if (String.valueOf(username.getText()).length()>0 && String.valueOf(password.getText()).length()>0){
    	        		//hide keyboard
    	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    					imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
    					//
    					login();
    				}
    	            handled = true;
    	        }
    	        return handled;
			}
    	});
	    
	    login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
	        	if (String.valueOf(username.getText()).length()>0 && String.valueOf(password.getText()).length()>0){
	        		//hide keyboard
	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
					//
					login();
				}
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void login(){
		try {
			//fix possible bug with transfer data on web
			String usernameS = URLEncoder.encode(String.valueOf(username.getText()), "UTF-8");
			String passwordS = URLEncoder.encode(String.valueOf(password.getText()), "UTF-8");
			String request = "https://oauth.vk.com/token?grant_type=password&client_id="+Constants.CLIENT_ID+"&client_secret="+Constants.CLIENT_SECRET+"&username="+usernameS+"&password="+passwordS;
	        
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	        	new AuthTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
	        } else {
	        	new AuthTask().execute(request);
	        }
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	class AuthTask extends AsyncTask<String, String, String>{
		
		public HttpClient _getNewHttpClient() {
		    try {
		        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		        trustStore.load(null, null);

		        SSLSocketFactory sf = new MySSLSocketFactory(trustStore, this, LoginActivity.this);
		        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		        
		        HttpParams params = new BasicHttpParams();
		        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		        SchemeRegistry registry = new SchemeRegistry();
		        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		        registry.register(new Scheme("https", sf, 443));

		        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

		        DefaultHttpClient http = new DefaultHttpClient(ccm, params);
		        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("jk", "jk");
		        AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
		        http.getCredentialsProvider().setCredentials(authScope, credentials);

		        return http;
		    } catch (Exception e) {
		    	e.printStackTrace();
		        return new DefaultHttpClient();
		    }
		}  
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			progressDialog.dismiss();
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			
	        handler.post(new Runnable(){
				@Override
				public void run() {
					//show an dialog intermediate 
			        progressDialog.setIndeterminate(true);
			        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			        progressDialog.setMessage(getText(R.string.login_activity_wait));
			        progressDialog.setCancelable(false);
			        progressDialog.setCanceledOnTouchOutside(false);
			        try {
			        	progressDialog.show();
			    	} catch (Exception e){
			    		e.printStackTrace();
			    	}
				}
			});
	        
			HttpClient httpclient = _getNewHttpClient();
			HttpResponse response;
	        String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(arg0[0]));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				responseString = out.toString();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
	        	return null;
	        }
	        return responseString;
		}
		
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
	        
	        // error in connection to server (no connection, no response)
	        if (result==null){
	        	progressDialog.dismiss();
	        	Toast.makeText(getApplicationContext(), getString(R.string.login_activity_error), Toast.LENGTH_LONG).show();
	        	return;
	        }
	        
	        //wrong pass or username
        	if (result.contains("Username or password is incorrect")){
        		progressDialog.dismiss();
        		//wrong password or username, try again
        		Toast.makeText(getApplicationContext(), getString(R.string.login_activity_wrong), Toast.LENGTH_LONG).show();
        		password.setText("");
        		return;
        	} 
        	
        	//redirect (if user use 2step verification
	        if (result.contains("redirect_uri")){
	        	showRedirectDialog(Utils.extractPattern(result, "\"redirect_uri\":\"(.*?)\""));
	        	return;
	        }
	        
	        //redirect to captcha image
        	if(result.contains("need_captcha")) {
        		progressDialog.dismiss();
	        	//else parse captcha
	        	String captcha_sid = Utils.extractPattern(result, "\"captcha_sid\":\"(.*?)\"");
	        	String captcha_img = Utils.extractPattern(result, "\"captcha_img\":\"(.*?)\"");
	        	showCaptchaDialog(captcha_sid, captcha_img);
        		return;
        	}
        	
        	if (!result.contains("error")){
        		String access_token=Utils.extractPattern(result, "\"access_token\":\"(.*?)\"");
        		String user_id=Utils.extractPattern(result, "\"user_id\":(\\d*)");
        		
        		perceed(access_token, user_id);
        	}
	    }
	    
	    @SuppressLint("InflateParams") 
	    public void showCaptchaDialog(final String captcha_sid, final String captcha_img){
	    	final Context context = LoginActivity.this;
	    	AlertDialog.Builder build = new AlertDialog.Builder(context);
	    	LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	//init views
	    	View content = inflater.inflate(R.layout.dialog_content_captcha, null);
	    	TextView title = (TextView)content.findViewById(R.id.title);
	    	Button cancel = (Button)content.findViewById(R.id.cancel);
	    	Button apply = (Button)content.findViewById(R.id.apply);
	    	ImageView icon = (ImageView)content.findViewById(R.id.icon);
	    	ImageView capthcaImage = (ImageView)content.findViewById(R.id.captcha_image);
	    	final EditText captchaText = (EditText)content.findViewById(R.id.captcha_result);
	    	
	    	//set fonts
	    	SFUIFonts.LIGHT.apply(context, captchaText);
	    	SFUIFonts.MEDIUM.apply(context, title);
	    	SFUIFonts.LIGHT.apply(context, cancel);
	    	SFUIFonts.LIGHT.apply(context, apply);
	    	
	    	//view job
	    	icon.setImageResource(R.drawable.ic_launcher);
	    	ImageLoader.getInstance().denyNetworkDownloads(false);
	    	ImageLoader.getInstance().resume();
	    	ImageLoader.getInstance().displayImage(captcha_img.replace("\\/", "/"), capthcaImage, options);
	    	
	    	captchaText.setOnEditorActionListener(new OnEditorActionListener(){
				@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
				public boolean onEditorAction(android.widget.TextView arg0, int arg1, KeyEvent arg2) {
					boolean handled = false;
	    	        if (arg1 == EditorInfo.IME_ACTION_GO) {
	    	        	
		        		//hide keyboard
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(captchaText.getWindowToken(), 0);
						
						try {
							//fix possible bug with transfer data on web
							String usernameS = URLEncoder.encode(String.valueOf(username.getText()), "UTF-8");
							String passwordS = URLEncoder.encode(String.valueOf(password.getText()), "UTF-8");
							String request = "https://oauth.vk.com/token?grant_type=password&client_id="+Constants.CLIENT_ID+"&client_secret="+Constants.CLIENT_SECRET+"&username="+usernameS+"&password="+passwordS+"&captcha_sid="+URLEncoder.encode(captcha_sid, "UTF-8")+"&captcha_key="+URLEncoder.encode(String.valueOf(captchaText.getText()), "UTF-8");
					        
					        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					        	new AuthTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
					        } else {
					        	new AuthTask().execute(request);
					        }
							
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						alert.dismiss();
	    	            handled = true;
	    	        }
	    	        return handled;
				}
	    	});
	    	
	    	apply.setOnClickListener(new View.OnClickListener() {
				@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
				public void onClick(View v) {
					try {
						//fix possible bug with transfer data on web
						String usernameS = URLEncoder.encode(String.valueOf(username.getText()), "UTF-8");
						String passwordS = URLEncoder.encode(String.valueOf(password.getText()), "UTF-8");
						String request = "https://oauth.vk.com/token?grant_type=password&client_id="+Constants.CLIENT_ID+"&client_secret="+Constants.CLIENT_SECRET+"&username="+usernameS+"&password="+passwordS+"&captcha_sid="+URLEncoder.encode(captcha_sid, "UTF-8")+"&captcha_key="+URLEncoder.encode(String.valueOf(captchaText.getText()), "UTF-8");
				        
				        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				        	new AuthTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
				        } else {
				        	new AuthTask().execute(request);
				        }
						
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					alert.dismiss();
				}
			});
	    	
	    	cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					alert.dismiss();
				}
			});
	    	
	    	// show dialog
	    	build.setView(content);
	    	alert = build.create();
	    	try {
	    		alert.show();
	    	} catch (Exception e){
	    		e.printStackTrace();
	    	}
	    }
	    
	    @SuppressLint({ "SetJavaScriptEnabled", "InflateParams" }) 
	    public void showRedirectDialog(final String url){
	    	final Context context = LoginActivity.this;
	    	alert = new Dialog(context);
	    	LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    	
	    	//init views
	    	View content = inflater.inflate(R.layout.dialog_content_redirect, null);
	    	TextView title = (TextView)content.findViewById(R.id.title);
	    	ImageView icon = (ImageView)content.findViewById(R.id.icon);
	    	WebView webview = (WebView)content.findViewById(R.id.vkontakteview);
	    	
	    	//set fonts
	    	SFUIFonts.MEDIUM.apply(context, title);
	    	
	    	//view job
	    	icon.setImageResource(R.drawable.ic_launcher);
	    	
	    	//webview reclass
		   	webview.getSettings().setJavaScriptEnabled(true);
	        webview.clearCache(true);
	        
	        webview.setWebViewClient(new VkontakteWebViewClient());
	                
	        CookieSyncManager.createInstance(context);
	        
	        CookieManager cookieManager = CookieManager.getInstance();
	        cookieManager.removeAllCookie();
	        
	        webview.loadUrl(url);
	        
	        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        alert.setContentView(content);
	        
	        alert.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					try {
						progressDialog.dismiss();
				   	} catch (Exception e){
				   		e.printStackTrace();
				   	}
				}
	        });
	        
	        // show dialog
		   	try {
		   		alert.show();
		   	} catch (Exception e){
		   		e.printStackTrace();
		   	}
	    }
	    
	    class VkontakteWebViewClient extends WebViewClient {
	        @Override
	        public void onPageStarted(WebView view, String url, Bitmap favicon) {
	            super.onPageStarted(view, url, favicon);
	            parseUrl(url);
	        }
	    }
	    
	    private void parseUrl(String url) {
	        try {
	            if(url==null)
	                return;
	            if(url.startsWith(Auth.redirect_url))
	            {
	                if(!url.contains("error=")){
	                    String[] auth=Auth.parseRedirectUrl(url);
	    	        	perceed(auth[0], auth[1]);
	                } else {
	                	try {
							progressDialog.dismiss();
					   	} catch (Exception e){
					   		e.printStackTrace();
					   	}
	                }
	                try {
	                	alert.dismiss();
	    		   	} catch (Exception e){
	    		   		e.printStackTrace();
	    		   	}
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    
	    //what todo when we signed in
	    public void perceed(final String token, final String id){
            account.access_token=token;
            account.user_id=Long.valueOf(id);
            account.save(LoginActivity.this);
            api=new Api(account.access_token, Constants.CLIENT_ID);
            
            new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						//save user first name and avatar
						Collection<Long> u = new ArrayList<Long>();
			            u.add(account.user_id);
			            Collection<String> d = new ArrayList<String>();
			            d.add("");
			            
			            User userOne = api.getProfiles(u, d, "photo_200,photo_100,sex,bdate", "", "", "").get(0);
			            sPref.edit().putString(Constants.PREFERENCES_USER_AVATAR_URL, ((userOne.photo_200 == null || userOne.photo_200.length()<1) ? userOne.photo_medium_rec : userOne.photo_200)).commit();
						sPref.edit().putString(Constants.PREFERENCES_USER_FIRST_NAME, userOne.first_name).commit();
						sPref.edit().putString(Constants.PREFERENCES_USER_LAST_NAME, userOne.last_name).commit();
						sPref.edit().putString(Constants.PREFERENCES_USER_BIRTHDAY, userOne.birthdate).commit();
						sPref.edit().putInt(Constants.PREFERENCES_USER_GENDER, userOne.sex).commit();
						
						//set default download folder:
						if (sPref.getString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, null) == null){
							sPref.edit().putString(Constants.PREFERENCES_DOWNLOAD_DIRECTORY, (new CustomEnvironment(getApplicationContext())).DownloadDirectoryDecide()).commit();
						}
						
						handler.post(new Runnable(){
							@Override
							public void run() {
								if ((progressDialog != null) && progressDialog.isShowing()) {
						        	progressDialog.dismiss();
						        }
								progressDialog = null;
								
								Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
						        startActivity(intent);
						        // stop curr activity
								finish();
							}
						});
					} catch(Exception e){
						handler.post(new Runnable(){
							@Override
							public void run() {
								if ((progressDialog != null) && progressDialog.isShowing()) {
						        	progressDialog.dismiss();
						        }
								progressDialog = null;
							}
						});
						e.printStackTrace();
					}
				}
            }).start();
	    }
	}
}
