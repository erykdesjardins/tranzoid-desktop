package com.ryk.tzandroidutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.ryk.tzclientlib.TzPreferences;
import com.ryk.tzviewswidget.ContactListRow;
import com.ryk.customproviders.CpMessenger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

// Utility providing SMS management such as Read, Receive and Send
public class TzMessenger extends BroadcastReceiver {
	private static final Boolean stopPropagation = true;
	//private static final String mmsHeader = "application/vnd.wap.multipart.related";
	
	public TzMessenger() {
		
	}
	
	// Construct a JSON containing text messages from a contact
	// starting from startIndex, until smsCount is reached
	// If formattedDate is true, date will not be in a number format but formatted according to user's language
    @SuppressLint("SimpleDateFormat")
	public static void GetAllSMS(String ContactID, int startIndex, int smsCount, PrintWriter w, Boolean formattedDate)
    {
    	w.print("{\"messages\":[");
    	w.flush();
    	
    	// Main context and Uri to SMS database
    	Context ctx = TzPreferences.mainCtx;
    	Uri sms = CpMessenger.CONTENT_URI;
    	
    	// Query into inbox
        Cursor cursor;
        
        // Should we get all the sms or only those associated with a contact?
        if (ContactID.equals(""))
        	cursor = ctx.getContentResolver().query(sms, 
        		new String[] { CpMessenger._ID, CpMessenger.ADDRESS, CpMessenger.DATE, CpMessenger.BODY, CpMessenger.TYPE, CpMessenger.READ }, 
        		null, null, 
        		CpMessenger.DATE + " DESC, "+CpMessenger.PERSON+" DESC, "+CpMessenger.DATE+" DESC");
        else
        	cursor = ctx.getContentResolver().query(sms, 
        		new String[] { CpMessenger._ID, CpMessenger.ADDRESS, CpMessenger.DATE, CpMessenger.BODY, CpMessenger.TYPE, CpMessenger.READ }, 
        		CpMessenger.PERSON + " = ?", new String[]{ContactID}, 
        		CpMessenger.DATE + " DESC, "+CpMessenger.PERSON+" DESC, "+CpMessenger.DATE+" DESC");
        
        // If something was returned
        if (cursor != null) 
        {
        	// Build empty json
            for (int i = 0; i < cursor.getCount(); i++) 
            {
                cursor.moveToPosition(i);

                // Get the address, the timestamp, the content and if the message was received or sent
                String identificator = cursor.getString(0);
                String person = cursor.getString(1);
                long timestamp = cursor.getLong(2);
                String body = TzEmotsParser.getParser().parse(
                				cursor.getString(3)
                				.replace("\\","/")
                				.replace("\"", "\\\"")
                				.replace(System.getProperty("line.separator"), "<br />")
                				.replace("\n", "<br />")
                			);
                String type = cursor.getInt(4) == 1 ? "in" : "out";
                
                // Format date according to timestamp
                String date = formattedDate ? getFormattedDate(timestamp) : String.valueOf(timestamp);
                
                // 0 if unread, 1 if read
                String read = String.valueOf(cursor.getInt(5));
                
                // Format the SMS in JSON
                w.print(formatSMSString(identificator, person, date, body, type, read));
                w.flush();
            }
            // Send json
        }
        
        // Will be netrilized
        // appendMMS(w);
        
        // Close cursor to database and close JSON string
        cursor.close();
        w.print("{}]}");
        w.flush();
    }
    
    public static void appendMMS(PrintWriter w) {
    	Context ctx = TzPreferences.mainCtx;
    	ContentResolver contentResolver = ctx.getContentResolver();
    	
    	final String[] projection = new String[]{"_id"};
    	Uri uri = Uri.parse("content://mms");
    	Cursor query = contentResolver.query(uri, projection, null, null, null);
    	
    	if (query.moveToFirst()) {
    	    do {
    	        String id = query.getString(0);
    	        w.println(readMms(id));
    	        
    	        w.flush();
    	    } while (query.moveToNext());
    	} 	   	
    }
    
    public static void getAllMMS(PrintWriter w) {
    	w.println("{\"messages\":[");
    	w.flush();    	
    	
    	Context ctx = TzPreferences.mainCtx;
    	ContentResolver contentResolver = ctx.getContentResolver();
    	
    	final String[] projection = new String[]{"_id"};
    	Uri uri = Uri.parse("content://mms");
    	Cursor query = contentResolver.query(uri, projection, null, null, null);
    	
    	if (query.moveToFirst()) {
    	    do {
    	        String id = query.getString(0);
    	        w.println(readMms(id));
    	        
    	        w.flush();
    	    } while (query.moveToNext());
    	} 	
    	
    	query.close();
        w.println("{}]}");
        w.flush();    	
    }
    
    private static String formatSMSString(String identificator, String writer, String date, String body, String type, String read) {
    	return "{\"id\":\""+identificator+"\",\"media\":\"false\",\"writer\":\""+writer+"\",\"time\":\""+date+"\",\"body\":\""+body+"\",\"type\":\""+type+"\",\"read\":\""+read+"\"},";
    }
    
    private static String formatMMSString(String identificator, String writer, String date, String body, String type, String link) {
    	return "{\"id\":\""+identificator+"\",\"media\":\"true\",\"writer\":\""+writer+"\",\"time\":\""+date+"\",\"body\":\""+body+"\",\"type\":\""+type+"\",\"link\":\""+link+"\"},";
    }
    
    private static String readMms(String id) {
    	String selectionPart = "mid=" + id;
    	Uri uri = Uri.parse("content://mms/part");
    	Cursor cPart = TzPreferences.mainCtx.getContentResolver().query(uri, null, selectionPart, null, null);
    	
    	String body = "";
    	String link = "";
    	String writer = getAddressNumber(id);
    	String date = getMmsDate(id);
    	
    	if (cPart.moveToFirst()) {
    	    do {
    	        String partId = cPart.getString(cPart.getColumnIndex("_id"));
    	        String type = cPart.getString(cPart.getColumnIndex("ct"));
    	        if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||"image/gif".equals(type) || "image/jpg".equals(type) || "image/png".equals(type)) {
    	        	
    	            link = partId;
    	        } else if ("text/plain".equals(type)) {
    	            String data = cPart.getString(cPart.getColumnIndex("_data"));

    	            if (data != null) {
    	                // implementation of this method below
    	                body += getMmsText(partId);
    	            } else {
    	                body += cPart.getString(cPart.getColumnIndex("text"));
    	            }
    	        }
    	    } while (cPart.moveToNext());
    	}    	
    	
    	cPart.close();
    	
    	return formatMMSString(id, writer, date, body, "1", link);
    }
    
    private static String getMmsDate(String id) {
    	Uri uri = Uri.parse("content://mms/");
    	String selection = "_id = " + id;
    	
    	Cursor cursor = TzPreferences.mainCtx.getContentResolver().query(uri, null, selection, null, null);   	
    	long date = 0l;
    	
    	if(cursor.moveToNext()){ 
    		date = cursor.getLong(cursor.getColumnIndex("date")); 
    	}
    	
    	cursor.close();
    	
    	return getFormattedDate(date);
    }
    
    private static String getMmsText(String id) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = TzPreferences.mainCtx.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
                
                isr.close();
                reader.close();
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }    
    
    private static String getAddressNumber(String id) {
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = TzPreferences.mainCtx.getContentResolver().query(uriAddress, null, selectionAdd, null, null);
        String name = null;
        
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return name;
    }    
    
	public static int getMessageCount() {
		ContentResolver cr = TzPreferences.mainCtx.getContentResolver();
		Cursor cur = cr.query(CpMessenger.CONTENT_URI, null, null, null, null);
		int numberOfContacts = cur.getCount();		
		cur.close();
		
		return numberOfContacts;
	}    
    
    @TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("InlinedApi")
	public static void SendSMS(String number, String content, PrintWriter w) {
		// SMS utility to send messages
		SmsManager manager = SmsManager.getDefault();
		
		// Split message in case it's too big
		ArrayList<String> divText = manager.divideMessage(content);
		
		// If message fits in one line, send plain text
		if (divText.size() == 1) manager.sendTextMessage(number, null, content, null, null);
		
		// Else, sent a table with a split table
		else if (divText.size() > 1) manager.sendMultipartTextMessage(number, null, divText, null, null);
		
		// Get current context and link to sent database
    	Context ctx = TzPreferences.mainCtx;
    	
    	Uri sms = CpMessenger.Sent.CONTENT_URI;
    	
    	// Create package
    	ContentValues values = new ContentValues();
    	values.put(CpMessenger.ADDRESS, number);
    	values.put(CpMessenger.BODY, content);  	
    	
    	// Insert sent SMS into database and fetch ID
    	long rowID = ContentUris.parseId(ctx.getContentResolver().insert(sms, values));
    	
    	// Confirm
    	w.println("{\"response\":\"ok\",\"id\":\"" + String.valueOf(rowID) + "\",\"time\":\"" + TzMessenger.getFormattedDate() + "\"}");
	
    	
    	w.flush();
    }
    
    public static String insertSMS(String identificator, String writer, String date, String body, String type, String read) {
    	ContentValues val = new ContentValues();
    	// val.put("_id", identificator);
    	val.put(CpMessenger.ADDRESS, writer);
    	val.put(CpMessenger.DATE, date);
    	val.put(CpMessenger.BODY, body);
    	val.put(CpMessenger.TYPE, type.equals("in") ? 1 : 0);
    	val.put(CpMessenger.READ, read);
    	
    	return String.valueOf(ContentUris.parseId(TzPreferences.mainCtx.getContentResolver().insert(CpMessenger.CONTENT_URI, val)));
    }
    
    // Changes the state of all SMS from a contact to "read" (seen)
    @Deprecated
	public static void DEPRECATED_ReadSMS(String number) {
    	// Main context, Uri to SMS database, and cursor with results
        Cursor cursor;
    	Context ctx = TzPreferences.mainCtx;
    	Uri sms = CpMessenger.CONTENT_URI;
        
        // Should we get all the sms or only those associated with a contact?
        if (number.equals(""))
        	cursor = ctx.getContentResolver().query(sms, new String[] { CpMessenger._ID }, CpMessenger.READ + " = ?", 
        		new String[]{"0"}, CpMessenger.DATE + " DESC, "+CpMessenger.PERSON+" DESC, "+CpMessenger.DATE+" DESC");
        else
        	cursor = ctx.getContentResolver().query(sms, new String[] { CpMessenger._ID }, CpMessenger.ADDRESS + " = ? AND "+CpMessenger.READ+" = ?", 
        		new String[]{number, "0"}, CpMessenger.DATE + " DESC");
	
    	
        // If something was returned
        if (cursor != null) 
        {
        	// Loop through all SMS
            for (int i = 0; i < cursor.getCount(); i++) 
            {
            	// Move to new position
                cursor.moveToPosition(i);

                // Get ID, and set sms to "read"
                String SmsMessageId = cursor.getString(cursor.getColumnIndex(CpMessenger._ID));
                ContentValues values = new ContentValues();
                values.put(CpMessenger.READ, 1);
            	values.put(CpMessenger.SEEN, 1);
            	
                ctx.getContentResolver().update(CpMessenger.CONTENT_URI, values, CpMessenger._ID + "=" + SmsMessageId, null);
            }      		
        }
        
        // Close cursor to database
        cursor.close(); 	
    }   
    
	public static void ReadSMS(String number) {
    	// Main context, Uri to SMS database, and cursor with results
    	Context ctx = TzPreferences.mainCtx;

        // Get ID, and set sms to "read"
        ContentValues values = new ContentValues();
        values.put(CpMessenger.READ, 1);
    	values.put(CpMessenger.SEEN, 1);
    	
        ctx.getContentResolver().update(CpMessenger.CONTENT_URI, values, CpMessenger.ADDRESS + "=" + number, null);	
    }      
    
    // Delete an SMS from database according to an id
    public static void deleteSMS(String id) {
    	Context ctx = TzPreferences.mainCtx;
    	Uri smsDB = CpMessenger.CONTENT_URI;
    	
    	String where = CpMessenger._ID + " = ?";
    	String[] selectionArgs = new String[] {id};
    	
    	ctx.getContentResolver().delete(smsDB, where, selectionArgs);
    }
    
    // Delete an entire conversation between the user and a contact
    public static void deleteConversation(String number) {
    	Context ctx = TzPreferences.mainCtx;
    	Uri smsDB = CpMessenger.CONTENT_URI;
    	
    	String where = CpMessenger.ADDRESS + " = ?";
    	String[] selectionArgs = new String[] {number};
    	
    	ctx.getContentResolver().delete(smsDB, where, selectionArgs);    	
    }
    
    public static void storeReceivedSMS(String number, String content) {
		// Get current context and link to sent database
    	Context ctx = TzPreferences.mainCtx;
    	Uri sms = CpMessenger.Inbox.CONTENT_URI;
    	
    	// Create package
    	ContentValues values = new ContentValues();
    	values.put(CpMessenger.ADDRESS, number);
    	values.put(CpMessenger.BODY, content); 
    	values.put(CpMessenger.READ, 1);
    	values.put(CpMessenger.SEEN, 1); 	
    	
    	// Insert sent SMS
    	ctx.getContentResolver().insert(sms, values);   	
    }
    
    // Generate a date string in a known format according to a milliseconds long
    @SuppressLint("SimpleDateFormat")
	public static String getFormattedDate(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return formatter.format(calendar.getTime());	
    }

    // Generate a date string in the provided format according to a milliseconds long
    @SuppressLint("SimpleDateFormat")
	public static String getFormattedDate(long timestamp, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return formatter.format(calendar.getTime());	
    }    
    
    // Generate a date string in a known format according to the current date and time
    @SuppressLint("SimpleDateFormat")
	public static String getFormattedDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return formatter.format(calendar.getTime());   	
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
		// Try to receive SMS
		try {
			// Read content of package
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			String message = "";
			String address = "";
			Boolean broadcasted = false;
			
			// If received bundle exists
			if (bundle != null) {
				// Build SMS array with received stuff
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
										
				// Contains messages and addresses
				HashMap<String, String> infos = new HashMap<String, String>();
										     
				// For each messages received, build message and store address
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
											   
					message += msgs[i].getMessageBody();
					infos.put("address", msgs[i].getOriginatingAddress());
					
					// Store received SMS into inbox
					address = msgs[i].getOriginatingAddress();
				}
									  
				// Add message to hashmap and notify on cellphone
				infos.put("message", message);
									  
				// Broadcast SMS reception on all threads 
				broadcasted = TzBroadcastGroup.broadcast(new TzBroadcastGroup.BroadcastedPackage(infos, TzBroadcastGroup.PackageType.sms));

				if (stopPropagation && broadcasted) { 
					TzMessenger.storeReceivedSMS(address, message);
					this.abortBroadcast();
				}
			}
		} catch (Exception ex) {
			
		}
	}
	
	// To be used soon
	private static class TzEmotsParser {
		private static TzEmotsParser _this = new TzEmotsParser();
		
		private TzEmotsParser() {}
		
		public static TzEmotsParser getParser() {
			return _this;
		}
		
		public String parse(String msg) {
			return msg;
		}
	}
	
	public ArrayList<ContactListRow> requestLastMessageList(Context ctx) {
		ArrayList<ContactListRow> list = new ArrayList<ContactListRow>();
		
    	// Query into inbox
        Cursor cursor = ctx.getContentResolver().query(
        	CpMessenger.CONTENT_URI, 
        	new String[] {CpMessenger._ID, CpMessenger.BODY, CpMessenger.ADDRESS}, "", new String[] {}, 
        	CpMessenger.DATE + " DESC" 
        );
		
        if (cursor.moveToFirst()){
        	do {
        		Log.i("ryk", cursor.getString(cursor.getColumnIndex(CpMessenger.BODY)));
        	} while (cursor.moveToNext());
        }
        
        cursor.close();
        
		return list;
	}
}