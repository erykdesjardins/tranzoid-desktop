package com.ryk.tzandroidutil;
import java.io.PrintWriter;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ryk.tzclientlib.TzPreferences;

public class TzVideoLibrary {
	public TzVideoLibrary() {
		
	}
	
	public void getAllVideos(PrintWriter w) {
		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		
		String[] projection = {
			MediaStore.Video.Media._ID,
			MediaStore.Video.Media.TITLE,
			MediaStore.Video.Media.DURATION,
			MediaStore.Video.Media.DESCRIPTION,
			MediaStore.Video.Media.RESOLUTION,
			MediaStore.Video.Media.DATE_ADDED,
			MediaStore.Video.Media.SIZE
		};
		
		Cursor cr = TzPreferences.mainCtx.getContentResolver().query(uri, projection, null, null, null);
		
		w.print("\"videos\":[");
		if (cr != null) {
			while (cr.moveToNext()) {
				w.print("{");
				
				w.print(buildNode("id", cr.getLong(0)) + ",");
				w.print(buildNode("title", cr.getString(1)) + ",");
				w.print(buildNode("duration", cr.getLong(2)) + ",");
				w.print(buildNode("description", cr.getString(3)) + ",");
				w.print(buildNode("resolution", cr.getString(4)) + ",");
				w.print(buildNode("date", cr.getLong(5)) + ",");
				w.print(buildNode("size", cr.getLong(6)));
				
				if (cr.isLast()) w.print("}");
				else w.print("},");
				
				w.flush();
			}

			cr.close();
		}
		w.print("]");
		w.flush();		
	}
	
	private String buildNode(String key, String value) {
		return "\"" + key + "\":\"" + value + "\"";
	}
	
	private String buildNode(String key, long value) {
		return "\"" + key + "\":" + value;
	}	
}
