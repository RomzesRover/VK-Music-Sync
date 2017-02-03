package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import java.io.File;
import java.util.Arrays;

import android.content.Context;

import com.BBsRs.vkmusicsyncvol2.R;


public class CustomEnvironment {
	
	Context context;
	
	String androidDataPackageDeffolderTest = null;
	String musicDeffolderTest = null;
	
	public CustomEnvironment(Context context){
		this.context = context;
		androidDataPackageDeffolderTest = "/Android/data/"+context.getPackageName()+"/"+context.getString(R.string.default_folder_name)+"/";
    	musicDeffolderTest = "/Music/"+context.getString(R.string.default_folder_name)+"/";
	}
	
	public String getExternalStorageDirectory(){
		String sSDpath = null;
	    File fileCur = null;
	    File checker = null;
	    boolean canWrite = true;
	    
	    for( String sPathCur : Arrays.asList( "ext_card", "external_sd", "ext_sd", "external", "extSdCard",  "externalSdCard", "extSd", "extsd",  "extsdcard",  "externalsdcard", "sdCard2", "sdcard2", "sdCard1", "sdcard1", "sdCard0", "sdcard0", "sdCard", "sdcard")) // external sdcard
	    {
	      fileCur = new File( "/mnt/", sPathCur);
	      if( fileCur.isDirectory() && fileCur.canWrite())
	        {
	    	  //check if its real writeable
	    	  try {
	      		checker = new File(fileCur.getAbsolutePath()+musicDeffolderTest+"/1.txt");
	      		checker.mkdirs();
	      		checker.createNewFile();
	      		checker.delete();
	      	  } catch (Exception e){
	      		try {
		      		checker = new File(fileCur.getAbsolutePath()+androidDataPackageDeffolderTest+"/1.txt");
		      		checker.mkdirs();
		      		checker.createNewFile();
		      		checker.delete();
		      	  } catch (Exception e1){
		      		  canWrite = false;
		      	  }
	      	  }
	    	  
	    	  if (canWrite){
	    		  sSDpath = fileCur.getAbsolutePath();
	    		  break;
	    	  } else 
	    		  canWrite = true;
	        }
	    }
	    fileCur = null;
	    if( sSDpath == null)  sSDpath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
		return sSDpath;
	}
	
	public String DownloadDirectoryDecide(){
    	File realExt = new File(getExternalStorageDirectory());
    	File defaultExtAndr = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
    	
    	File checker = null;
    	
    	//check realExt/Music/Deffolder/
    	try {
    		checker = new File(realExt.getAbsolutePath()+musicDeffolderTest+"/1.txt");
    		checker.mkdirs();
    		checker.createNewFile();
    		checker.delete();
    		return realExt.getAbsolutePath()+musicDeffolderTest;
    	} catch (Exception e){}
    	
    	//check realExt/Android/data/package/Deffolder/
    	try {
    		checker = new File(realExt.getAbsolutePath()+androidDataPackageDeffolderTest+"/1.txt");
    		checker.mkdirs();
    		checker.createNewFile();
    		checker.delete();
    		return realExt.getAbsolutePath()+androidDataPackageDeffolderTest;
    	} catch (Exception e){}
    	
    	//check defaultExtAndr/Music/Deffolder/
    	try {
    		checker = new File(defaultExtAndr.getAbsolutePath()+musicDeffolderTest+"/1.txt");
    		checker.mkdirs();
    		checker.createNewFile();
    		checker.delete();
    		return defaultExtAndr.getAbsolutePath()+musicDeffolderTest;
    	} catch (Exception e){}
    	
    	//check defaultExtAndr/Android/data/package/Deffolder/
    	try {
    		checker = new File(defaultExtAndr.getAbsolutePath()+androidDataPackageDeffolderTest+"/1.txt");
    		checker.mkdirs();
    		checker.createNewFile();
    		checker.delete();
    		return defaultExtAndr.getAbsolutePath()+androidDataPackageDeffolderTest;
    	} catch (Exception e){}
    	
    	//otherwise return default using path (CONNECT WITH DEVELOPER IF DOWNLOADING IS UNAVAILABLE)
		return android.os.Environment.getExternalStorageDirectory()+"/Music/"+context.getString(R.string.default_folder_name)+"/";
    }
}