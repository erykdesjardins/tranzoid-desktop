package com.ryk.tzandroidutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.ryk.facebook.FB;
import com.ryk.tzclientlib.TzPreferences;

// Utility providing contact basic functionnalities
public class TzContactUtil {
	public TzContactUtil() {
		
	}
	
	@SuppressLint("InlinedApi")
	public void getContactDetail(String selectID, PrintWriter w) {
		// Initialize contact JSON
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();

		// Main currsor used for database
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[] {
			ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
			ContactsContract.Contacts.STARRED
		}, ContactsContract.Contacts._ID + "=?", new String[]{selectID}, null);
		
        // At least one contact must exist
        if (cur.getCount() > 0) 
        {
        	// For every returned value
		    if (cur.moveToNext()) 
		    {
		    	// Contact ID
			    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			    
			    // Complete name 
			    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
			    
			    // Phone Number
			    String phone = "[]";
			    
			    // Email
			    String email = "[]";
			    
			    // Home address
			    String address = "{}";
			    
			    // Facebook picture link
			    String facebookpic = "";
			    
			    // Starred
			    String starred = String.valueOf(cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.STARRED)));
			    		
			    // Append only if contact has a phone number
		 		{
		 			// Second request in number database to find a relationship
		 			// between a contact and its phone number
		            Cursor pCur = cr.query
		            (
		            	ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		            	new String[] {
		            		ContactsContract.CommonDataKinds.Phone.NUMBER,
		            		ContactsContract.CommonDataKinds.Phone.TYPE
		            	}, 
		            	ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		            	new String[]{id}, null
		            );
		            
		            // For every number associated with contact
		            phone = "[";
		            Boolean first = true;
		  	        while (pCur.moveToNext()) 
		  	        {
		  	        	if (first) first = false;
		  	        	else phone += ",";
		  	        	
		  	        	phone += "{\"number\":\"+1" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("(", "").replace(")", "").replace("-", "").replace("+1", "") + "\",";
		  	        	phone += "\"type\":"+String.valueOf(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)))+"}";
		  	        }
		            phone += "]";
		  	        
		            // Close phone cursors
		  	        pCur.close();
		 	    }
		 		
		 		// Find email addresses
		 		{
		 			// Query all email addresses
		            Cursor pCur = cr.query
		            (
		            	ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
		            	new String[] {
		            		ContactsContract.CommonDataKinds.Email.ADDRESS,
		            		ContactsContract.CommonDataKinds.Email.TYPE
		            	}, 
		            	ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = ?", 
		            	new String[]{id}, null
		            );	 			
		            
		            // Loop until cursor's end
		            email = "[";
		            Boolean first = true;
		  	        while (pCur.moveToNext()) 
		  	        {
		  	        	if (first) first = false;
		  	        	else email += ",";
		  	        	
		  	        	email += "{\"address\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)) + "\",";
		  	        	email += "\"type\":" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)) + "}";
		  	        }
		  	        email += "]";
		  	        
		  	        // Close email cursor
		  	        pCur.close();		            
		 		}
		 		
		 		// Find home address
		 		{
		 			// Query all addresses
		            Cursor pCur = cr.query
		            (
		            	ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, 
		            	new String[] {
		            		ContactsContract.CommonDataKinds.StructuredPostal.STREET, 
		            		ContactsContract.CommonDataKinds.StructuredPostal.CITY,
		            		ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, 
		            		ContactsContract.CommonDataKinds.StructuredPostal.REGION, 
		            		ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
		            		ContactsContract.CommonDataKinds.StructuredPostal.TYPE
		            	}, 
		            	ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID +" = ?", 
		            	new String[]{id}, null
		            );
		            
		            address = "{";
		            if (pCur.moveToNext()) {
		            	address += "\"street\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) + "\",";
		            	address += "\"city\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) + "\",";
		            	address += "\"postcode\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) + "\",";
		            	address += "\"region\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) + "\",";
		            	address += "\"country\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) + "\",";
		            	address += "\"type\":\"" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)) + "\"";
		            }
		            address += "}";
		 		}
		 		
		 		// Look for a facebook pic
		 		facebookpic = FB.getFacebookPictureFromName(name);
		 		
		 		// Send contact JSON
  	        	w.print("{\"id\":\""+id+"\",\"name\":\""+name+"\",\"fbpic\":\""+facebookpic+"\",\"address\":"+address+",\"starred\":"+starred+",\"number\":"+phone+",\"email\":"+email+"}");
  	        	w.flush();		 		
		    }
		    // Fermeture du curseur de contacts
		    cur.close();
        }

        w.flush();
	}	
	
	@SuppressLint("InlinedApi")
	public void getAllContacts(PrintWriter w) {
		// Initialize contact JSON
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
		w.print("{\"contacts\":[");
		w.flush();
		
		// Main currsor used for database
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[] {
			ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
			ContactsContract.Contacts.STARRED
		}, null, null, null);
		
        // At least one contact must exist
        if (cur.getCount() > 0) 
        {
        	// For every returned value
		    while (cur.moveToNext()) 
		    {
		    	// Contact ID
			    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
			    
			    // Complete name 
			    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
			    
			    // Phone Number
			    String phone = "[]";
			    
			    // Starred
			    String starred = String.valueOf(cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.STARRED)));
			    		
			    // Facebook picture link
			    String facebookpic = "";
			    
			    // Append only if contact has a phone number
		 		{
		 			// Second request in number database to find a relationship
		 			// between a contact and its phone number
		            Cursor pCur = cr.query
		            (
		            	ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		            	new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER}, 
		            	ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		            	new String[]{id}, null
		            );
		            
		            // For every number associated with contact
		            phone = "[";
		            Boolean first = true;
		  	        while (pCur.moveToNext()) 
		  	        {
		  	        	if (first) first = false;
		  	        	else phone += ",";
		  	        	
		  	        	phone += "\"+1" + pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace(" ", "").replace("(", "").replace(")", "").replace("-", "").replace("+1", "") + "\"";
		  	        }
		            phone += "]";
		  	        
		            // Close phone cursors
		  	        pCur.close();
		 	    }
		 		
		 		// Fetch facebook pic link
		 		facebookpic = FB.getFacebookPictureFromName(name);
		 		
		 		// Send contact JSON
  	        	w.print("{\"id\":\""+id+"\",\"name\":\""+name+"\",\"facebookpic\":\""+facebookpic+"\",\"starred\":"+starred+",\"number\":"+phone+"},");
  	        	w.flush();		 		
		    }
		    // Fermeture du curseur de contacts
		    cur.close();
        }

        w.print("{\"name\":\"Unknown\",\"number\":\"??????????\"}]}");
        w.flush();
	}
	
	@SuppressLint("InlinedApi")
	public String updateExistingContact(String id, String name, String[] address, String[] phone, String[] email) {
		try {
			ContentValues values = new ContentValues();
			
			// Common information
			values.put(ContactsContract.Contacts.DISPLAY_NAME, name);
			
			TzPreferences.mainCtx.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI, 
				values, 
				ContactsContract.Contacts._ID + " = ?", 
				new String[] {id}
			);
			
			// Address
			values.clear();
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address[0]);
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, address[1]);
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION, address[2]);
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address[3]);
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, address[4]);
			values.put(ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			
			TzPreferences.mainCtx.getContentResolver().update(
				ContactsContract.Data.CONTENT_URI, 
				values, 
				ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.StructuredPostal.TYPE + " = ?", 
				new String[] {id, "1"}
			);		
			
			// Emails
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, email[0]);	
			values.put(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);	
			TzPreferences.mainCtx.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Email.TYPE + " = ?", new String[] {id, "1"});				
			
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, email[1]);		
			values.put(ContactsContract.CommonDataKinds.Email.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);	
			TzPreferences.mainCtx.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Email.TYPE + " = ?", new String[] {id, "2"});					
			
			// Phone numbers
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone[0]);	
			values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);		
			TzPreferences.mainCtx.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?", new String[] {id, "1"});				
			
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone[1]);
			values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);		
			TzPreferences.mainCtx.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?", new String[] {id, "2"});				
			
			values.clear();
			values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone[2]);
			values.put(ContactsContract.CommonDataKinds.Phone.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);		
			TzPreferences.mainCtx.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?", new String[] {id, "3"});				
			
			return "ok";
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return ex.getMessage();
		}
	}
	
	public InputStream getContactThumbnailStream(int id) throws IOException {
	    final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
	    final Cursor cursor = TzPreferences.mainCtx.getContentResolver().query(uri, new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
	    InputStream byteArray = null;
	    
	    try {
	        if (cursor.moveToFirst()) {
	        	byteArray = new ByteArrayInputStream(cursor.getBlob(0));	
	        } 
	    } catch (NullPointerException ne) {
	    	byteArray = TzPreferences.mainCtx.getAssets().open("mediaimg/users.png");
	    } finally {
	        cursor.close();
	    }
	    
	    return byteArray;
	}
	
	// Returns a stream reading a contact thumbnail
	public InputStream getContactThumbnail(int id) throws IOException {
		// Stream reading contact image
		InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(
			TzPreferences.mainCtx.getContentResolver(), 
			ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, id
			)
		);
		
		// If image is null, try to read Facebook image
		if (stream == null) {
			stream = TzPreferences.mainCtx.getAssets().open("mediaimg/users.png");
		}
		
		return stream;
	}
	
	// Returns the number of contacts
	public static int getContactCount() {
		// Get unique content resolver
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
		
		// Query contact count
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		int numberOfContacts = cur.getCount();		
		cur.close();
		
		return numberOfContacts;
	}
	
	// Gets facebook photo as byte stream
    @SuppressWarnings("unused")
    @Deprecated
	private byte[] getFacebookPhoto(int id) {
	    Uri phoneUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
	    Uri photoUri = null;
	    ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
	    Cursor contact = cr.query(phoneUri, new String[] { ContactsContract.Contacts._ID }, null, null, null);
	
	    if (contact.moveToFirst()) {
	        long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
	        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
	
	    }
	    else {
	        Bitmap defaultPhoto = BitmapFactory.decodeResource(TzPreferences.mainCtx.getResources(), android.R.drawable.ic_menu_report_image);
	        return picToBytes(defaultPhoto);
	    }
	    if (photoUri != null) {
	        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
	                cr, photoUri);
	        if (input != null) {
	            return picToBytes(BitmapFactory.decodeStream(input));
	        }
	    } else {
	        Bitmap defaultPhoto = BitmapFactory.decodeResource(TzPreferences.mainCtx.getResources(), android.R.drawable.ic_menu_report_image);
	        return picToBytes(defaultPhoto);
	    }
	    Bitmap defaultPhoto = BitmapFactory.decodeResource(TzPreferences.mainCtx.getResources(), android.R.drawable.ic_menu_report_image);

	    
	    return picToBytes(defaultPhoto);
	}	
    
    private byte[] picToBytes(Bitmap bmp) {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
	    return stream.toByteArray(); 	
    }
    
}
