package com.ryk.tzviews;

import com.ryk.tzandroidutil.TzMessenger;
import com.ryk.tzclientlib.TzPreferences;

import android.app.Activity;
import android.os.Bundle;

public class TzContactList extends Activity {
	public TzContactList() {        
		super();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Store current context statically
		TzPreferences.mainCtx = this.getApplicationContext();
		
		buildList();		
	}
	
	public void buildList() {
		TzMessenger messenger = new TzMessenger();
		messenger.requestLastMessageList(this);
	}
}