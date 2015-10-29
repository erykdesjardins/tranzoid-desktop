package com.ryk.customproviders;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;

public class CpMessenger {
	CpMessenger _this = new CpMessenger();
	public static Uri CONTENT_URI = Uri.parse("content://sms");
	
    public static int MESSAGE_TYPE_ALL    = 0;
    public static int MESSAGE_TYPE_INBOX  = 1;
    public static int MESSAGE_TYPE_SENT   = 2;
    public static int MESSAGE_TYPE_DRAFT  = 3;
    public static int MESSAGE_TYPE_OUTBOX = 4;
    public static int MESSAGE_TYPE_FAILED = 5;
    public static int MESSAGE_TYPE_QUEUED = 6;
	public static String _ID = "_id";
	public static String BODY = "body";
    public static String TYPE = "type";
    public static String THREAD_ID = "thread_id";
    public static String ADDRESS = "address";
    public static String DATE = "date";
    public static String DATE_SENT = "date_sent";
    public static String READ = "read";
    public static String SEEN = "seen";
    public static String STATUS = "status";
    public static String SUBJECT = "subject";
    public static String PERSON = "person";
    public static String PROTOCOL = "protocol";
    public static String REPLY_PATH_PRESENT = "reply_path_present";
    public static String SERVICE_CENTER = "service_center";
    public static String LOCKED = "locked";
    public static String ERROR_CODE = "error_code";    
    
    public static int STATUS_NONE = -1;
    public static int STATUS_COMPLETE = 0;
    public static int STATUS_PENDING = 32;
    public static int STATUS_FAILED = 64;	
    
    public static Boolean HasNewAPI = false;
    public static CpMessenger.Conversation conversation = new CpMessenger.Conversation();
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private CpMessenger() {
		if (Build.VERSION.SDK_INT >= 19) {
			CpMessenger.CONTENT_URI 		= Telephony.Sms.CONTENT_URI			;
			CpMessenger._ID					= Telephony.Sms._ID					;
			CpMessenger.BODY				= Telephony.Sms.BODY				;
	        CpMessenger.MESSAGE_TYPE_ALL    = Telephony.Sms.MESSAGE_TYPE_ALL	;
	        CpMessenger.MESSAGE_TYPE_INBOX  = Telephony.Sms.MESSAGE_TYPE_INBOX	;
	        CpMessenger.MESSAGE_TYPE_SENT   = Telephony.Sms.MESSAGE_TYPE_SENT	;
	        CpMessenger.MESSAGE_TYPE_DRAFT  = Telephony.Sms.MESSAGE_TYPE_DRAFT	;
	        CpMessenger.MESSAGE_TYPE_OUTBOX = Telephony.Sms.MESSAGE_TYPE_OUTBOX	;
	        CpMessenger.MESSAGE_TYPE_FAILED = Telephony.Sms.MESSAGE_TYPE_FAILED	;
	        CpMessenger.MESSAGE_TYPE_QUEUED = Telephony.Sms.MESSAGE_TYPE_QUEUED	;
	        CpMessenger.TYPE 				= Telephony.Sms.TYPE 				;	 		
	        CpMessenger.THREAD_ID 			= Telephony.Sms.THREAD_ID 			; 
	        CpMessenger.ADDRESS 			= Telephony.Sms.ADDRESS 			;	
	        CpMessenger.DATE 				= Telephony.Sms.DATE 				;	
	        CpMessenger.DATE_SENT			= Telephony.Sms.DATE_SENT 			; 
	        CpMessenger.READ 				= Telephony.Sms.READ 				; 		
	        CpMessenger.SEEN 				= Telephony.Sms.SEEN 				; 		
	        CpMessenger.STATUS 				= Telephony.Sms.STATUS 				; 	
	        CpMessenger.STATUS_NONE 		= Telephony.Sms.STATUS_NONE			; 	
	        CpMessenger.STATUS_COMPLETE 	= Telephony.Sms.STATUS_COMPLETE 	;
	        CpMessenger.STATUS_PENDING 		= Telephony.Sms.STATUS_PENDING 		;	
	        CpMessenger.STATUS_FAILED 		= Telephony.Sms.STATUS_FAILED 		;	
	        CpMessenger.SUBJECT 			= Telephony.Sms.SUBJECT				;
	        CpMessenger.PERSON 				= Telephony.Sms.PERSON 				;
	        CpMessenger.PROTOCOL 			= Telephony.Sms.PROTOCOL 			;
	        CpMessenger.REPLY_PATH_PRESENT 	= Telephony.Sms.REPLY_PATH_PRESENT 	;
	        CpMessenger.SERVICE_CENTER 		= Telephony.Sms.SERVICE_CENTER 		;
	        CpMessenger.LOCKED 				= Telephony.Sms.LOCKED 				;
	        CpMessenger.ERROR_CODE 			= Telephony.Sms.ERROR_CODE 			;
	        
	        CpMessenger.HasNewAPI = true;
		}
	}
	
	public static class Inbox {
		Inbox _this = new Inbox();
		public static Uri CONTENT_URI = Uri.parse("content://sms/inbox");
		
		@TargetApi(Build.VERSION_CODES.KITKAT)
		private Inbox() {
			if (Build.VERSION.SDK_INT >= 19) {
				Inbox.CONTENT_URI = Telephony.Sms.Inbox.CONTENT_URI;
			}
		}
	}
	
	public static class Sent {
		Sent _this = new Sent();
		public static Uri CONTENT_URI = Uri.parse("content://sms/sent");
		
		@TargetApi(Build.VERSION_CODES.KITKAT)
		private Sent() {
			if (Build.VERSION.SDK_INT >= 19) {
				Sent.CONTENT_URI = Telephony.Sms.Sent.CONTENT_URI;
			}
		}
	}	
	
	public static class Conversation {
		// TODO : FIX THIS OMG PLZ THIS IS HORRIBLE DUDE
		// IT'S NOT EVEN THE SAME CONTENT PROVIDER, PLZ 
		public Uri CONTENT_URI = Uri.parse("content://mms-sms/conversations?simple=true");
		public String RECIPIENT_IDS = "RECIPIENT_IDS";
		public String DATE = "DATE";
		public String SNIPPET = "SNIPPET";
		public String MESSAGE_COUNT = "MESSAGE_COUNT";
		
		@TargetApi(Build.VERSION_CODES.KITKAT) 
		public Conversation() {
			if (Build.VERSION.SDK_INT >= 19) {
				CONTENT_URI = Telephony.Sms.Conversations.CONTENT_URI;
				RECIPIENT_IDS = Telephony.Sms.Conversations.ADDRESS;
				DATE = Telephony.Sms.Conversations.DATE;
				SNIPPET = Telephony.Sms.Conversations.SNIPPET;
				MESSAGE_COUNT = Telephony.Sms.Conversations.MESSAGE_COUNT;
				
			}
		}
	}
}