package com.ryk.tzandroidutil;
import java.io.PrintWriter;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ryk.tzclientlib.TzPreferences;

public class TzAudioLibrary {
	public TzAudioLibrary() {
		
	}
	
	public void getAllSongs(PrintWriter w) {
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		
		String[] projection = {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.SIZE,
			MediaStore.Audio.Media.YEAR,
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.TRACK
		};
		
		String selection = MediaStore.Audio.Media.IS_MUSIC + " = ?";
		String[] selectionArgs = {"1"};
		
		Cursor cr = TzPreferences.mainCtx.getContentResolver().query(uri, projection, selection, selectionArgs, MediaStore.Audio.Media.TRACK);
		
		w.print("\"songs\":[");
		if (cr != null) {
			while (cr.moveToNext()) {
				w.print("{");
				
				w.print(buildNode("id", cr.getLong(0)) + ",");
				w.print(buildNode("title", cr.getString(1).replaceAll("\"", "")) + ",");
				w.print(buildNode("album", cr.getLong(2)) + ",");
				w.print(buildNode("artist", cr.getLong(3)) + ",");
				w.print(buildNode("duration", cr.getLong(4)) + ",");
				w.print(buildNode("size", cr.getLong(5)) + ",");
				w.print(buildNode("year", cr.getLong(6)) + ",");
				w.print(buildNode("path", cr.getString(7)) + ",");
				w.print(buildNode("track", cr.getLong(8)));
				
				if (cr.isLast()) w.print("}");
				else w.print("},");
				
				w.flush();
			}
			
			cr.close();
		}
		w.print("]");
		w.flush();
	}
	
	public void getAllAlbums(PrintWriter w) {
		Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
		
		String[] projection = {
			MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM,
			MediaStore.Audio.Albums.ALBUM_ART,
			MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.FIRST_YEAR,
			MediaStore.Audio.Albums.NUMBER_OF_SONGS
		};
		
		Cursor cr = TzPreferences.mainCtx.getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Albums.ALBUM);
		
		w.print("\"albums\":[");
		if (cr != null) {
			while (cr.moveToNext()) {
				w.print("{");
				
				w.print(buildNode("id", cr.getLong(0)) + ",");
				w.print(buildNode("album", cr.getString(1).replaceAll("\"", "")) + ",");
				w.print(buildNode("art", cr.getString(2)) + ",");
				w.print(buildNode("artist", cr.getString(3)) + ",");
				w.print(buildNode("year", cr.getLong(4)) + ",");
				w.print(buildNode("count", cr.getLong(5)));
				
				if (cr.isLast()) w.print("}");
				else w.print("},");
				
				w.flush();
			}
			
			cr.close();
		}
		w.print("]");
		w.flush();		
	}

	public void getAllArtists(PrintWriter w) {
		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		
		String[] projection = {
			MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS
		};
		
		Cursor cr = TzPreferences.mainCtx.getContentResolver().query(uri, projection, null, null, MediaStore.Audio.Artists.ARTIST);
		
		w.print("\"artists\":[");
		if (cr != null) {
			while (cr.moveToNext()) {
				w.print("{");
				
				w.print(buildNode("id", cr.getLong(0)) + ",");
				w.print(buildNode("artist", cr.getString(1).replaceAll("\"", "")) + ",");
				w.print(buildNode("albums", cr.getLong(2)) + ",");
				w.print(buildNode("tracks", cr.getLong(3)));
				
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
