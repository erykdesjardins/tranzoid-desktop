package com.ryk.tzandroidutil;

import java.io.File;

import com.ryk.tzclientlib.TzPreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class TzDevice {
	private static TzDevice _this = new TzDevice();
	private String jsonFormat;
	
	private TzDevice() { 
		jsonFormat = "";
	}
	
	public static TzDevice getDevice() {
		return _this;
	}
	
	public String getJSON() {
		jsonFormat = "{" + 
				buildNode("board", Build.BOARD) + "," +
				buildNode("bootloader", Build.BOOTLOADER) + "," +
				buildNode("brand", Build.BRAND) + "," +
				buildNode("cpuabi", Build.CPU_ABI) + "," +
				buildNode("cpuabi2", Build.CPU_ABI2) + "," +
				buildNode("device", Build.DEVICE) + "," +
				buildNode("display", Build.DISPLAY) + "," +
				buildNode("hardware", Build.HARDWARE) + "," +
				buildNode("host", Build.HOST) + "," +
				buildNode("id", Build.ID) + "," +
				buildNode("manufacturer", Build.MANUFACTURER) + "," +
				buildNode("model", Build.MODEL) + "," +
				buildNode("product", Build.PRODUCT) + "," +
				buildNode("sdkversion", Build.VERSION.SDK_INT) + "," +
				buildNode("phonenumber", getUserNumber()) + "," +
				buildNode("smscount", TzMessenger.getMessageCount()) + "," +
				buildNode("photocount", TzGalleryManager.getPhotosCount()) + "," +
				buildNode("contactcount", TzContactUtil.getContactCount()) + "," +
				buildNode("resolution", getResolution(), false) + "," +
				buildNode("internalstorage", getInternalStorageSize(), false) + "," +
				buildNode("mountedstorage", getExternalStorageSizeDEBUG(), false) + 
	 		"}";		
		
		return jsonFormat;
	}
	
	private String buildNode(String title, String data) {
		return buildNode(title, data, true);
	}
	
	private String buildNode(String title, int data) {
		return buildNode(title, String.valueOf(data), false);
	}
	
	private String buildNode(String title, String data, Boolean withQuotes) {
		return withQuotes ? ("\"" + title + "\":\"" + data + "\"") : ("\"" + title + "\":" + data);
	}
	
	@SuppressLint("NewApi")
	private String getResolution() {
		WindowManager wm = (WindowManager) TzPreferences.mainCtx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = 0;
		int height = 0;
		
		if ((Build.VERSION.SDK_INT >= 13)) {
			Point size = new Point();
			display.getSize(size);
			
			width = size.x;
			height = size.y;
		} else {
			DisplayMetrics size = new DisplayMetrics();
			display.getMetrics(size);
			
			width = size.widthPixels;
			height = size.heightPixels;			
		}
		
		return "{" + buildNode("width", width) + "," + buildNode("height", height) + "}";
	}
	
	private String getUserNumber() {
		TelephonyManager tm = (TelephonyManager)TzPreferences.mainCtx.getSystemService(Context.TELEPHONY_SERVICE); 
		String num = tm.getLine1Number();
		
		return num == null ? "#" : num;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private String getInternalStorageSize() {
	    File path = Environment.getExternalStorageDirectory();
	    StatFs stat = new StatFs(path.getPath());

	    long availBlocks;
	    long blockSize;
	    long total_memory;	   	    
	    
	    if ((Build.VERSION.SDK_INT >= 16)) {
		    availBlocks = (long)stat.getAvailableBlocksLong();
		    blockSize = (long)stat.getBlockSizeLong();
		    
		    total_memory = blockSize * (long)stat.getBlockCountLong();	    	
	    } else {
		    availBlocks = (long)stat.getAvailableBlocks();
		    blockSize = (long)stat.getBlockSize();
		    
		    total_memory = blockSize * (long)stat.getBlockCount();
		}
	    
	    long free_memory = availBlocks * blockSize;

	    return "{\"total\":"+total_memory+",\"free\":"+free_memory+"}";		
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private String getExternalStorageSize() {
		String[] external = TzFileBrowser.getStorageDirectories();
		
		if (external != null && external.length > 0) {
		    File path = new File(external[0]);
		    StatFs stat = new StatFs(path.getPath());
			   
		    long availBlocks;
		    long blockSize;
		    long total_memory;	 		    
		    
		    if ((Build.VERSION.SDK_INT >= 16)) {
			    availBlocks = (long)stat.getAvailableBlocksLong();
			    blockSize = (long)stat.getBlockSizeLong();
			    
			    total_memory = blockSize * (long)stat.getBlockCountLong();	    	
		    } else {
			    availBlocks = (long)stat.getAvailableBlocks();
			    blockSize = (long)stat.getBlockSize();
			    
			    total_memory = blockSize * (long)stat.getBlockCount();
			}
		    
		    long free_memory = availBlocks * blockSize;
	
		    return "{\"total\":"+total_memory+",\"free\":"+free_memory+"}";			
		} else {
			return "\"unavailable\"";
		}
	}	
	
	private String getExternalStorageSizeDEBUG() {
		return "{\"total\":1,\"free\":1}";	
	}
}
