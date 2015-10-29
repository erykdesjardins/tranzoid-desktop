package com.ryk.facebook;

import java.util.Hashtable;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

public abstract class FB {
	private static Hashtable<String, String> nameIdRelation = new Hashtable<String, String>();
	
	public static Boolean isConnected() {
		return Session.getActiveSession().getState().isOpened();
	}
	
	public static void castConnectionDialog(Activity act) {
		Session.openActiveSession(act, true, new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {

			}
		});			
	}
	
	public static void LoadUsersHash() {
		if (isConnected()) {
	        Bundle params = new Bundle();
	        params.putString("q", "SELECT username FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me())");

	        Request request = new Request(Session.getActiveSession(), "/fql", params, HttpMethod.GET,
	                new Request.Callback() {
	                    public void onCompleted(Response response) {
	                        Log.v("ryk", "Facebook response : " + response.toString());
	                    }
	                });
	        Request.executeBatchAsync(request);
		}		
	}
	
	public static String getFacebookPictureFromName(String name) {
		if (nameIdRelation.containsKey(name)) return "https://graph.facebook.com/" + nameIdRelation.get(name) + "/picture?type=normal";
		else return "";
	}
}
