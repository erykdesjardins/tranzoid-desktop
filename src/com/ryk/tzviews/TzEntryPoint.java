package com.ryk.tzviews;

//////////////////////////////////////////////////
//					     				        //
//   Tranzoid Desktop  |  Android Application   //
//					     				        //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//      ___           ___                    ___           ___           ___           ___           ___           ___           ___        //
//     /\  \         /\  \                  /\  \         /\  \         /\  \         /\__\         /\  \         /\  \         /\  \    	//
//     \ \  \        \ \  \                /  \  \       /  \  \       /  \  \       / /  /         \ \  \       /  \  \       /  \  \   	//
//      \ \  \        \ \  \              / /\ \  \     / /\ \  \     / /\ \  \     / /__/           \ \  \     / /\ \  \     / /\ \  \  	//
//      /  \  \        \ \  \            / /  \ \__\   /  \_\ \  \   _\ \_\ \  \   /  \__\____       /  \  \   / /  \ \  \   /  \_\ \  \ 	//
//     / /\ \__\ _______\ \__\          / /__/ \ |__| / /\ \ \ \__\ /\ \ \ \ \__\ / /\  __ \__\     / /\ \__\ / /__/ \ \__\ / /\ \ \ \__\	//
//    / /  \/__/ \   _____/__/          \ \  \ / /  / \ \_\ \ \/__/ \ \ \ \ \/__/ \/_| |  |        / /  \/__/ \ \  \ / /  / \/__\ \/ /  /	//
//   / /  /       \ \  \                 \ \  / /  /   \ \ \ \__\    \ \ \ \__\      | |  |       / /  /       \ \  / /  /       \  /  / 	//
//   \/__/         \ \  \                 \ \/ /  /     \ \ \/__/     \ \/ /  /      | |  |       \/__/         \ \/ /  /         \/__/  	//
//                  \ \__\                 \__/__/       \ \__\        \  /  /       | |  |                      \  /  /                 	//
//                   \/__/                                \/__/         \/__/         \|__|                       \/__/                  	//
//																																			//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
	Tranzoid Desktop is more of a website than an Android application.
	
	The application core represents a web server interpreting HTTP requests.
	Plain sockets are used to communicate with clients which are, most of the time, web browsers.
	Since web pages are used, Javascript and CSS is used to present the main system to users.
	The pages are stored in the assets folder in application root.
	
	AJAX from jQuery is used a lot to avoid postbacks and refreshes. 
	Since a lot of data is transfered through async requests, data is stringified in json formatting.
	
	The applications does many thing involving existing items on cellphone's disks.
	Current features are :
		* File browsing; downloads, uploads, creations and deletions
		* Contact management, creations and deletions
		* Photo gallery, live slideshow
		* Text messages in and out; send and receive SMS
		* Application customisation, wallpaper, etc.
		* Security, passwords, sessions

	The previous project is created so it can be uploaded on the Android Market for free.
	Tranzoid could generate revenue if ads are added to the main interface. 
	An ad-less version could be downloaded for a little fee. 
*/

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.ryk.facebook.FB;
import com.ryk.tzandroidutil.TzPrefDatabase;
import com.ryk.tzclientlib.TzActiveSessions;
import com.ryk.tzclientlib.TzPreferences;
import com.ryk.tzdesktop.TzMainSurface;
import com.ryk.tzdesktop.TzMainView;
import com.ryk.tzmarkup.TzVocab;
import com.ryk.tzmarkup.TzVocabRepository;
import com.facebook.*;

// Main activity, application's entry point
public class TzEntryPoint extends Activity {
    TzMainSurface glSurface;
    TzMainView mainView;
    
	// On construction, build communication object
	public TzEntryPoint() {
		
	}
	
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	// Create main application user interface
        super.onCreate(savedInstanceState);

    	// Set old content view
    	mainView = new TzMainView(this);
    	setContentView(mainView);
    	
    	// Remove title bar
    	getActionBar().hide();
        
        // Store current context statically
		TzPreferences.mainCtx = this.getApplicationContext();
		
		// Initialize language
        TzVocabRepository.getRepo().buildDico(
        	TzVocab.Languages.valueOf(
        		TzPrefDatabase.getDatabase().getValue("lang").toString()
        	)
        );
		
		// Initialize sessions container
		TzActiveSessions.init();
		
		// Connect Facebook
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				
			}
		});			
		
		// Init Facebook picture link
		FB.LoadUsersHash();
		
		// While debugging, start app automatically
		connectAndLog(null);
    }

    @SuppressWarnings("unused")
	private boolean hasGLES20() {
        ActivityManager am = (ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion >= 0x20000;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.ryk.tzdesktop.R.menu.tz_main, menu);
        return true;
    }
    
    public void connectAndLog(View v) {  
    	// On button press, start background service and start serving
    	Intent service = new Intent(this, com.ryk.tzcommlib.TzCommunication.class);
    	this.startService(service);  	
    }  
    
    @Override
    protected void onResume() {
        super.onResume();
        if (null != glSurface) glSurface.onResume();
    }    
    
    @Override
    protected void onPause() {
        super.onPause();
        if (null != glSurface) glSurface.onPause();
    }    
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	    	mainView.castMenu();
	        return true;
	    }
	    
	    return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}	
}
