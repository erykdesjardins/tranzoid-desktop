package com.ryk.tzandroidutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.ryk.tzclientlib.TzPreferences;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;

// Provides easy access to camera photos 
public class TzGalleryManager {
	File photoFolder;
	private static Bitmap imageBitmap;
	private static String[] ThumbCols = {MediaStore.Images.Thumbnails.DATA};
	
	// Find folder with camera pics
	public TzGalleryManager() {
		photoFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	}
	
	// Returns a photo using the name of a file
	// Is it not necessary to use an absolute path since the class knows where the pics are
	public File getPhoto(String name) {
		String absPath = photoFolder.getAbsolutePath() + "/" + name;
		return new File(absPath);
	}
	
	// Gets all photos filename in a json array
	public void getPhotosJsonFormat(PrintWriter w) {
		w.println("{photos:[");
		
		// File browser and list of files
		TzFileBrowser broswer = new TzFileBrowser();
		
		// Get all photos recursively into string
		broswer.listFiles(photoFolder.getAbsolutePath(), "thumb", w);
		
		w.println("'']}");
	}
	
	public static int getPhotosCount() {
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
		Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		int numberOfContacts = cur.getCount();		
		cur.close();
		
		return numberOfContacts;
	}	
	
	// Gets all photos from media scanner in a json array
	@SuppressLint("InlinedApi")
	public void getSdCardImages(PrintWriter w) {
		String[] Cols = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.DATE_TAKEN };
		w.print("\"photos\":[");

		// Request to media scanner database
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Cols, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");

        // If at least one item was found
        if (cursor != null) {  
        	if (cursor.moveToFirst()) {
                do {
                	// Get the absolute path
                	long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                    long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                     
                    // Print the path with the separator
                    w.print("{\"path\":\"" + path + "\",\"id\":\""+id+"\",\"w\":\"" + width + "\",\"h\":\"" + height + "\", \"date\":\""+ TzMessenger.getFormattedDate(date, "dd/MM/yyyy") +"\"}");
                    if (!cursor.isLast()) w.print(",");
                    w.flush();
                } while (cursor.moveToNext());
            }
            cursor.close();
        }	
        
        w.print("]");
        w.flush();
	}
	
	public static String getThumbnailFilename(long id) {
		/*
        Cursor thumbcr = TzPreferences.mainCtx.getContentResolver().query(
            	MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, 
            	ThumbCols, 
            	MediaStore.Images.Thumbnails.IMAGE_ID + " = ?", 
            	new String[]{String.valueOf(id)}, 
            	null
            );
        */  
        
		Cursor thumbcr = MediaStore.Images.Thumbnails.queryMiniThumbnail(TzPreferences.mainCtx.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, ThumbCols);
        String thumb = "";
        
        if (thumbcr != null && thumbcr.moveToFirst()) {
        	thumb = thumbcr.getString(0);
        	thumbcr.close();
        }	
        
        return thumb;
	}

	public static void sendResizedImage(File filename, int thumbSize, OutputStream baos) {
		try
		{
			final int THUMBNAIL_SIZE = thumbSize;
	
			FileInputStream fis = new FileInputStream(filename);
		    BitmapFactory.Options o = new BitmapFactory.Options();
		    o.inJustDecodeBounds = true;			
			imageBitmap = BitmapFactory.decodeStream(fis, null, o);
	
			float width = Float.valueOf(o.outWidth);
			float height = Float.valueOf(o.outHeight);
			//float ratio = width/height;
			int newSize = 1;
			
	        while(!(width/2<THUMBNAIL_SIZE || height/2<THUMBNAIL_SIZE)){
	            width/=2;
	            height/=2;
	            newSize*=2;
	        }			
			
			o = new BitmapFactory.Options();
			o.inSampleSize = newSize*2;
			o.inJustDecodeBounds = false;
			
			fis.close();
			fis = new FileInputStream(filename);
			
			imageBitmap = BitmapFactory.decodeStream(fis, null, o);
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	
			baos.flush();
			baos.close();
			fis.close();
			
			imageBitmap.recycle();
			System.gc();
		}
		catch(Exception ex) {
			return;
		} 
    }

}
